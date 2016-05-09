package com.lvack.MasterStats.Jobs;

import com.lvack.MasterStats.Api.ResponseClasses.MatchDetail;
import com.lvack.MasterStats.Api.ResponseClasses.MatchList;
import com.lvack.MasterStats.Api.ResponseClasses.MatchReference;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiFactory;
import com.lvack.MasterStats.Api.RiotApiResponse;
import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import com.lvack.MasterStats.Db.DataClasses.SummonerItem;
import com.lvack.MasterStats.Db.DataManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lvack.MasterStats.Util.SummonerKeyUtils.summonerKeyToIdRegion;

/**
 * SummonerCrawlRunnableClass for MasterStats
 *
 * @author Leon Vack
 */

@Slf4j
public class SummonerCrawlRunnable implements Runnable {
    private static boolean running = true;

    /**
     * sets running to false to break run loop once the next iteration is started
     */
    public void terminate() {
        running = false;
    }

    /**
     * collects new summoners by getting the 20 oldest (longest time not updated) summoners for the db
     * with a random mastery score and then add all summoners from their last 10 games
     * if a summoner does not have any games in their match history they are deleted from the db
     */
    @Override
    public void run() {
        // while loop that breaks once terminate was called
        while (running) {
            // try-catch to keep thread running even if something goes wrong
            try {
                // get summoners to update
                log.info("Updating next batch of summoners");
                HashMap<RiotEndpoint, List<SummonerItem>> nextUpdateSummoners = DataManager.getNextUpdateSummoners(20);
                log.info(String.format("Got data of %d summoners", nextUpdateSummoners.size()));

                // iterate over all regions
                nextUpdateSummoners.entrySet().forEach(e -> {
                    // if no summoners were found in this region continue to the next
                    if (e.getValue().size() == 0) return;

                    // get matching riot api instance
                    RiotEndpoint endpoint = e.getKey();
                    RiotApi api = RiotApiFactory.getApi(endpoint);

                    // create list of summoners to add and delete
                    List<SummonerItem> deleteList = new ArrayList<>();
                    List<Long> summonerIds = new ArrayList<>();

                    // iterate over all summoners in the region
                    e.getValue().forEach(item -> {
                        long summonerId = summonerKeyToIdRegion(item.getSummonerKey()).getId();
                        // request summoners latest 10 matches from their match history
                        RiotApiResponse<MatchList> matchListResponse = api.getMatchListApi().getMatchListBySummoner(summonerId, 0, 10);
                        MatchList matchList = matchListResponse.get();
                        if (matchList != null && matchList.getEndIndex() > 0) {
                            // if matches were found, schedule summoner for update
                            summonerIds.add(summonerId);
                            // iterate over all matches returned by the api
                            for (int m = 0; m < matchList.getEndIndex(); m++) {
                                // request current match data
                                MatchReference matchReference = matchList.getMatches().get(m);
                                long matchId = matchReference.getMatchId();
                                RiotApiResponse<MatchDetail> matchRequest = api.getMatchApi().getMatchById(matchId);
                                MatchDetail matchDetail = matchRequest.get();
                                // filter failed requests
                                if (matchDetail != null) {
                                    // schedule all summoners in the game for updating
                                    matchDetail.getParticipantIdentities().stream()
                                            .map(i -> i.getPlayer().getSummonerId())
                                            .filter(i -> i != summonerId)
                                            .forEach(summonerIds::add);
                                }
                            }
                        } else {
                            // if no matches or no match history were found, schedule summoner for deletion
                            if (matchListResponse.getResponse().getStatus() == 404 ||
                                    matchList != null && matchList.getEndIndex() == 0)
                                deleteList.add(item);
                        }

                    });

                    log.info(String.format("Processed summoners in '%s'. Saving %d and deleting %d",
                            endpoint.name(), summonerIds.size(), deleteList.size()));

                    // save collected summoners to db
                    while (summonerIds.size() > 0) {
                        Long[] longs = new Long[Math.min(summonerIds.size(), 10)];
                        for (int l = 0; l < longs.length; l++) {
                            longs[l] = summonerIds.remove(0);
                        }
                        DataManager.saveSummonersToDb(endpoint, longs);
                    }

                    // remove summoners without a match history form the db
                    while (deleteList.size() > 0) {
                        SummonerItem[] items = new SummonerItem[Math.min(deleteList.size(), 10)];
                        for (int l = 0; l < items.length; l++) {
                            items[l] = deleteList.remove(0);
                        }
                        DataManager.deleteSummonersFromDb(items);
                    }
                });
            } catch (Exception e) {
                // if something goes wrong, print the stacktrace for later investigation
                e.printStackTrace();
            }
        }
    }
}
