package com.lvack.MasterStats.Db;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.google.common.util.concurrent.RateLimiter;
import com.lvack.MasterStats.Api.ResponseClasses.*;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiFactory;
import com.lvack.MasterStats.Api.RiotApiResponse;
import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import com.lvack.MasterStats.Db.DataClasses.*;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Util.Pair;
import com.lvack.MasterStats.Util.SummonerKey;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lvack.MasterStats.Util.SummonerKeyUtils.summonerIdRegionToKey;
import static com.lvack.MasterStats.Util.SummonerKeyUtils.summonerKeyToIdRegion;

/**
 * DataManagerClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * class holding static functions to access the db
 */
@Slf4j
public class DataManager {
    public static final int UP_TO_DATE_DURATION = 60 * 60 * 1000;

    /**
     * Requests the required information (mastery score, league data, ...) from the riot api,
     * generates SummonerItems and championMasteryItems from this data and stores the summoners in the db
     *
     * @param endpoint    the endpoint the summoner ids are taken from
     * @param summonerIds the summoner ids of the summoners to be saved to the db
     */
    public static void saveSummonersToDb(RiotEndpoint endpoint, Long... summonerIds) {
        // get the riotApi for the given endpoint
        RiotApi riotApi = RiotApiFactory.getApi(endpoint);
        // request summoner data from the riot api
        Map<String, SummonerDto> stringSummonerDtoMap = riotApi.getSummonerApi().getSummonersByIds(summonerIds).get();

        // request league data for the summoners
        RiotApiResponse<HashMap<String, List<LeagueDto>>> leaguesResponse = riotApi.getLeagueApi().getLeagueBySummoner(summonerIds);
        HashMap<String, List<LeagueDto>> leagues = leaguesResponse.get();

        // cancel if summoner data request did not return any data (none of the summoners
        // given exits or the api is currently unavailable)
        if (stringSummonerDtoMap == null) return;

        // check if league data request was successful, if it was unsuccessful and the status code
        // is not 404 (none of the summoners given has a rank) abort, if the status code is 404
        // replace the league data map with an empty hashMap
        int status = leaguesResponse.getResponse().getStatus();
        if (status != 200 && status != 404) return;
        if (leagues == null) leagues = new HashMap<>();

        // create a list of all summoner ids of summoners that are
        // not level 30 and remove them from the map of summoner data
        List<String> removeKeys = stringSummonerDtoMap.entrySet().stream()
                .filter(e -> e.getValue().getSummonerLevel() != 30)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        removeKeys.forEach(stringSummonerDtoMap::remove);

        // if no summoners remain, abort
        if (stringSummonerDtoMap.size() == 0) return;


        HashMap<String, List<LeagueDto>> finalLeagues = leagues;

        // iterate over all summoners and generate summoner items to be written to the db
        List<SummonerItem> summonerItems = stringSummonerDtoMap.values().stream().map(s -> {
            // request and save league data
            String tier = "null";
            String division = "null";
            long id = s.getId();
            if (finalLeagues.containsKey(String.valueOf(id))) {
                List<LeagueDto> leagueDtos = finalLeagues.get(String.valueOf(id));
                LeagueDto leagueDto = leagueDtos.stream()
                        .filter(l -> "RANKED_SOLO_5x5".equals(l.getQueue())).findFirst().orElse(null);
                if (leagueDto != null) {
                    tier = leagueDto.getTier();
                    if (leagueDto.getEntries() != null)
                        division = leagueDto.getEntries().stream()
                                .filter(l -> String.valueOf(id).equals(l.getPlayerOrTeamId()))
                                .map(LeagueEntryDto::getDivision).findFirst().orElse("null");
                }
            }

            Integer score = riotApi.getChampionMasteryApi().getSummonersChampionMasteryScore(id).get();
            if (score == null) score = 0;

            // create new SummonerItem and add data
            SummonerItem summonerItem = new SummonerItem();
            summonerItem.setSummonerKey(summonerIdRegionToKey(id, endpoint));
            summonerItem.setSummonerName(s.getName());
            summonerItem.setDivision(division);
            summonerItem.setTier(tier);
            summonerItem.setMasteryScore(score);
            summonerItem.setLastUpdated(System.currentTimeMillis());
            summonerItem.setProfileIconId(s.getProfileIconId());
            summonerItem.setRevisionDate(s.getRevisionDate());
            summonerItem.setSummonerLevel(s.getSummonerLevel());
            return summonerItem;
        }).collect(Collectors.toList());

        // iterate over all summoners sets and generate champion mastery items to be written to the db
        List<ChampionMasteryItem> masteryItems = stringSummonerDtoMap.values().stream().map(SummonerDto::getId)
                // get each summoner's id and request champion mastery data
                .map(summonerId -> riotApi.getChampionMasteryApi().getSummonersChampionMastery(summonerId).get())
                // remove empty and null responses
                .filter(ms -> ms != null)
                .filter(ms -> ms.size() > 0)
                // create a stream for each summoner of their champion mastery data and merge them to one steam using flatMap
                .flatMap(ms -> ms.stream().map(m -> {
                            // save mastery data to a new championMasteryItem
                            String highestGrade = m.getHighestGrade();
                            ChampionMasteryItem championMasteryItem = new ChampionMasteryItem();
                            championMasteryItem.setSummonerKey(summonerIdRegionToKey(m.getPlayerId(), endpoint));
                            championMasteryItem.setChampionId(m.getChampionId());
                            championMasteryItem.setChampionPoints(m.getChampionPoints());
                            championMasteryItem.setChestGranted(m.isChestGranted() ? 1 : 0);
                            championMasteryItem.setChampionLevel(m.getChampionLevel());
                            championMasteryItem.setChampionPointsSinceLastLevel(m.getChampionPointsSinceLastLevel());
                            championMasteryItem.setChampionPointsUntilNextLevel(m.getChampionPointsUntilNextLevel());
                            championMasteryItem.setHighestGrade(highestGrade == null ? "null" : highestGrade);
                            championMasteryItem.setLastPlayTime(m.getLastPlayTime());
                            return championMasteryItem;
                        }
                        )
                ).collect(Collectors.toList());

        log.info(String.format("Adding %d summoners with champion mastery information (%d) to the database",
                summonerItems.size(), masteryItems.size()));

        // write all summoner and champion mastery items to the db
        // the summoners in an external thread and champion mastery in main thread to use
        // the write capacity of both tables
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        Thread summonerWrite = new Thread(() ->
                summonerItems.forEach(i -> {
                    DBTable.SUMMONER.getWriteLimiter().acquire();
                    dynamoDBMapper.save(i);
                }));
        summonerWrite.setName("summonerToDbSummonerWriteThread");
        summonerWrite.start();

        masteryItems.forEach(i -> {
            DBTable.CHAMPION_MASTERY.getWriteLimiter().acquire();
            dynamoDBMapper.save(i);
        });

        // wait for the summoner thread to finish
        try {
            summonerWrite.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request the list of all champions from the riot api and add missing champions to the db
     */
    public static void updateChampions() {
        // request list of all champions
        RiotApi riotApi = RiotApiFactory.getApi(RiotEndpoint.GLOBAL);
        ChampionListDto championListDto;
        RiotApiResponse<ChampionListDto> champions = riotApi.getStaticDataApi().getChampions();

        // if list of all champions is null, abort (api probably unavailable at the moment)
        if ((championListDto = champions.get()) == null) return;
        String version = championListDto.getVersion();

        // save version to local cache
        log.info(String.format("Saving version %s as current version", version));
        PageDataProvider.version = version;

        // create a list of all champions already stored in the db with identical data
        List<String> deleteKeys = new ArrayList<>();
        for (ChampionItem championItem : getChampions()) {
            String keyName = championItem.getKeyName();
            if (championListDto.getData().containsKey(keyName)) {
                ChampionDto championDto = championListDto.getData().get(keyName);
                if (championDto.getKey().equals(keyName) &&
                        championDto.getId() == championItem.getChampionId() &&
                        championDto.getName().equals(championItem.getChampionName()) &&
                        championDto.getTitle().equals(championItem.getChampionTitle()) &&
                        String.format("http://ddragon.leagueoflegends.com/cdn/%s/img/champion/%s",
                                version, championDto.getImage().getFull())
                                .equals(championItem.getPortraitUrl()))
                    deleteKeys.add(keyName);
            }
        }

        // remove all champions from the list that are already up-to-date in the db
        deleteKeys.forEach(championListDto.getData()::remove);

        // if no champions need to be updated, return
        if (championListDto.getData().size() == 0) {
            log.info("All champions already in the database");
            return;
        }

        // generate champion items for the champions which need updating
        List<ChampionItem> championItems = championListDto.getData().values().stream()
                .map(c -> {
                    // create a new champion item and save data
                    ChampionItem championItem = new ChampionItem();
                    championItem.setKeyName(c.getKey());
                    championItem.setChampionId(c.getId());
                    championItem.setChampionName(c.getName());
                    championItem.setChampionTitle(c.getTitle());
                    championItem.setPortraitUrl(String.format("http://ddragon.leagueoflegends.com/cdn/%s/img/champion/%s",
                            version, c.getImage().getFull()));
                    return championItem;
                })
                .collect(Collectors.toList());

        // write all champions to the db
        log.info(String.format("Adding %d champions to the database (%s)", championItems.size(),
                championItems.stream().map(ChampionItem::getKeyName)
                        .collect(Collectors.joining("', '", "'", "'"))));
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        championItems.forEach(i -> {
            DBTable.CHAMPION.getWriteLimiter().acquire();
            dynamoDBMapper.save(i);
        });
    }

    /**
     * Returns a map of summoners to be updated next for each region
     *
     * @param batchSize the number of summoners to search
     * @return a map of summoners for each region which should be updated next
     */
    public static HashMap<RiotEndpoint, List<SummonerItem>> getNextUpdateSummoners(int batchSize) {
        log.info("Acquire summoner reads to update");

        DBTable.SUMMONER.getReadLimiter().acquire(batchSize);

        log.info(String.format("Loading %d summoners to update from dynamoDB", batchSize));

        // generate a query request which orders ascending lastUpdated and searches for summoners with a random
        // generated mastery score (0-999). only the first batchSize elements are returned (through ordering
        // those with the updates laying furthest in the past
        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":ms", new AttributeValue().withN(String.valueOf(new Random().nextInt(1000))));

        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        DynamoDBQueryExpression<SummonerItem> queryExpression = new DynamoDBQueryExpression<SummonerItem>()
                .withIndexName("masteryScore-lastUpdated-index")
                .withKeyConditionExpression("masteryScore = :ms")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConsistentRead(false)
                .withLimit(batchSize);

        // execute query request
        PaginatedQueryList<SummonerItem> query = dynamoDBMapper.query(SummonerItem.class, queryExpression);

        log.info(String.format("Found %d summoners to update", query.size()));

        // get summoner id and region of each summoner found and add them to the return map
        HashMap<RiotEndpoint, List<SummonerItem>> summoners = new HashMap<>();
        query.forEach(s -> {
            SummonerKey summonerKey = summonerKeyToIdRegion(s.getSummonerKey());
            RiotEndpoint region = summonerKey.getRegion();
            if (!summoners.containsKey(region)) summoners.put(region, new ArrayList<>());
            summoners.get(region).add(s);
        });

        return summoners;
    }

    /**
     * deletes the summoners given from the db
     *
     * @param items the summoner items to delete
     */
    public static void deleteSummonersFromDb(SummonerItem... items) {
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        // iterate over all summoner items and delete the summoner form the db
        Arrays.asList(items).forEach(i -> {
            DBTable.SUMMONER.getWriteLimiter().acquire();
            dynamoDBMapper.delete(i);
        });

    }

    /**
     * read the champion statistics from the db to the local cache
     */
    public static void loadChampionData() {
        // create maps to be stored in the local cache
        Map<String, ChampionStatisticItem> statistics = new HashMap<>();
        Map<Long, String> idKeyMap = new HashMap<>();

        // iterate over all champion statistics in the db and save them to the local cache maps
        // only get one statistic at a time, to ensure rate limits are not exceeded, as objects are large
        scanPages(ChampionStatisticItem.class, new DynamoDBScanExpression().withLimit(1),
                DBTable.CHAMPION_STATISTIC.getReadLimiter(), c -> statistics.put(c.getKeyName().toLowerCase(), c));

        scanPages(ChampionItem.class, new DynamoDBScanExpression(), DBTable.CHAMPION.getReadLimiter(),
                c -> idKeyMap.put(c.getChampionId(), c.getKeyName()));

        // store maps in local cache
        PageDataProvider.championStatisticMap = statistics;
        PageDataProvider.championIdKeyNameMap = idKeyMap;
    }

    /**
     * generates the championStatistics for all champions and stores it in the db and the local cache
     */
    public static void generateChampionStatistics() {

        log.info("Updating champion statistics");


        // create and initialize championStatisticItems to write to dynamoDB
        HashMap<Long, ChampionStatisticItem> championStatistics = new HashMap<>();
        getChampions().forEach(championItem -> {
                    ChampionStatisticItem championStatisticItem = new ChampionStatisticItem();
                    championStatisticItem.setChampionId(championItem.getChampionId());
                    championStatisticItem.setKeyName(championItem.getKeyName());
                    championStatisticItem.setChampionName(championItem.getChampionName());
                    championStatisticItem.setChampionTitle(championItem.getChampionTitle());
                    championStatisticItem.setPortraitUrl(championItem.getPortraitUrl());
                    championStatisticItem.setAvgMasteryPoints(0);
                    championStatisticItem.setSumMasteryPoints(0);
                    championStatisticItem.setThresholdMasteryPoints(0);
                    championStatisticItem.setTopSummoners(new ArrayList<>());
                    championStatisticItem.setHighestGradeCounts(new HashMap<>());
                    championStatisticItem.setLevelCounts(new HashMap<>());
                    championStatisticItem.setChestsGranted(new HashMap<>());
                    championStatisticItem.setPlayerCount(new HashMap<>());
                    championStatisticItem.setScoreDistribution(new HashMap<>());
                    championStatistics.put(championItem.getChampionId(), championStatisticItem);
                }
        );

        Set<String> regionNames = new HashSet<>(Arrays.asList(RiotEndpoint.values()).stream()
                .map(RiotEndpoint::name).collect(Collectors.toList()));

        log.info("Scanning championMastery table for entries");

        // prepare scan and execute
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        ScanResultPage<ChampionMasteryItem> pageScan;
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        // define variables for reading statistics
        int totalScanned = 0;
        int totalMatched = 0;
        int totalSearches = 0;
        double totalReadsConsumed = 0;
        int permitsToConsume = 1;

        do {
            DBTable.CHAMPION_MASTERY.getReadLimiter().acquire(permitsToConsume);

            pageScan = dynamoDBMapper.scanPage(ChampionMasteryItem.class, scanExpression);

            for (ChampionMasteryItem item : pageScan.getResults()) {
                // transfer item properties to local variables for easier access
                long championId = item.getChampionId();
                String summonerKey = item.getSummonerKey();
                int championLevel = item.getChampionLevel();
                long championPoints = item.getChampionPoints();
                int chestGranted = item.getChestGranted();
                String highestGrade = item.getHighestGrade();

                // get championStatisticItem for current champion
                ChampionStatisticItem championStatisticItem = championStatistics.get(championId);

                // extract region from summonerKey
                String region = summonerKeyToIdRegion(summonerKey).getRegion().name();
                if (!regionNames.contains(region)) {
                    log.warn(String.format("summoner '%s' does not have a valid region (region is '%s').",
                            summonerKey, region));
                    continue;
                }

                // increment player count (and chest granted count if chest was already granted)
                Map<String, Integer> playerCount = championStatisticItem.getPlayerCount();
                championStatisticItem.getPlayerCount().put(region, playerCount.getOrDefault(region, 0) + 1);
                if (chestGranted != 0) {
                    Map<String, Integer> chestsGranted = championStatisticItem.getChestsGranted();
                    chestsGranted.put(region, chestsGranted.getOrDefault(region, 0) + 1);
                }

                // check which score distribution step the mastery score is part of and increment matching step
                // if it is the first score distribution entry for this region also create the matching entry
                int stepId = (int) (championPoints / ChampionStatisticItem.SCORE_DISTRIBUTION_STEP_SIZE);
                if (stepId < ChampionStatisticItem.CHAMPION_SCORE_STEP_COUNT) {
                    Map<String, Map<Integer, Integer>> scoreDistribution = championStatisticItem.getScoreDistribution();
                    if (!scoreDistribution.containsKey(region)) {
                        HashMap<Integer, Integer> scoreDist = new HashMap<>();
                        IntStream.range(0, ChampionStatisticItem.CHAMPION_SCORE_STEP_COUNT)
                                .forEach(i -> scoreDist.put(i, 0));
                        scoreDistribution.put(region, scoreDist);
                    }

                    Map<Integer, Integer> scores = scoreDistribution.get(region);
                    scores.put(stepId, scores.get(stepId) + 1);
                }

                // check if mastery score is a top summoner
                if (championPoints > championStatisticItem.getThresholdMasteryPoints()) {
                    // update mastery score max and summoner
                    /* championStatisticItem.setMaxMasteryPoints(championPoints);
                    championStatisticItem.setMaxPointsSummonerNameKey(summonerKey); */

                    List<Pair<SummonerItem, ChampionMasteryItem>> topSummoners = championStatisticItem.getTopSummoners();
                    topSummoners.add(new Pair<>(new SummonerItem(), item));
                    topSummoners = topSummoners.stream().sorted((e1, e2) -> e2.getValue().getChampionPoints() -
                            e1.getValue().getChampionPoints()).limit(ChampionStatisticItem.TOP_SUMMONER_COUNT).collect(Collectors.toList());
                    if (ChampionStatisticItem.TOP_SUMMONER_COUNT <= topSummoners.size())
                        // update threshold
                        championStatisticItem.setThresholdMasteryPoints(topSummoners.get(0).getValue().getChampionPoints());
                }
                // add score to sum of all scores
                championStatisticItem.setSumMasteryPoints(championStatisticItem.getSumMasteryPoints() + championPoints);

                // check if highest grade is valid and increment matching grade count
                // if it is the first grade entry for this region also create the matching entry
                if (!ChampionStatisticItem.GRADES.contains(highestGrade)) {
                    log.warn(String.format("Summoner '%s' does not have a valid highest grade " +
                                    "on champion '%s' (grade is '%s').",
                            summonerKey, championStatisticItem.getKeyName(), highestGrade));
                } else {
                    Map<String, Map<String, Integer>> gradeCounts = championStatisticItem.getHighestGradeCounts();
                    if (!gradeCounts.containsKey(region)) {
                        Map<String, Integer> grades = new HashMap<>();
                        ChampionStatisticItem.GRADES.forEach(g -> grades.put(g, 0));
                        gradeCounts.put(region, grades);
                    }

                    Map<String, Integer> grades = gradeCounts.get(region);
                    grades.put(highestGrade, grades.get(highestGrade) + 1);
                }

                // check if champion level is valid and increment matching level count
                // if it is the first champion level entry for this region also create the matching entry
                if (ChampionStatisticItem.MIN_CHAMPION_LEVEL <= championLevel &&
                        championLevel <= ChampionStatisticItem.MAX_CHAMPION_LEVEL) {
                    Map<String, Map<Integer, Integer>> levelCounts = championStatisticItem.getLevelCounts();
                    if (!levelCounts.containsKey(region)) {
                        HashMap<Integer, Integer> levels = new HashMap<>();
                        IntStream.range(ChampionStatisticItem.MIN_CHAMPION_LEVEL, ChampionStatisticItem.MAX_CHAMPION_LEVEL + 1)
                                .forEach(i -> levels.put(i, 0));
                        levelCounts.put(region, levels);
                    }

                    Map<Integer, Integer> levels = levelCounts.get(region);
                    levels.put(championLevel, levels.get(championLevel) + 1);
                } else {
                    log.warn(String.format("Summoner '%s' does not have a valid champion level " +
                                    "on champion '%s' (level is '%d').",
                            summonerKey, championStatisticItem.getKeyName(), championLevel));
                }
            }

            // update scan statistics and calculate permits to consume
            totalMatched += pageScan.getCount();
            totalScanned += pageScan.getScannedCount();
            totalSearches += 1;
            Double capacityUnits = pageScan.getConsumedCapacity().getCapacityUnits();

            scanExpression.setExclusiveStartKey(pageScan.getLastEvaluatedKey());

            totalReadsConsumed += capacityUnits;
            permitsToConsume = (int) (capacityUnits - 1);
            if (permitsToConsume <= 0) permitsToConsume = 1;
        } while (pageScan.getLastEvaluatedKey() != null);

        // calculate total player count as sum of all player counts and the average mastery score
        log.info("Calculation average mastery points");
        championStatistics.values().forEach(e -> {
            int totalPlayerCount = e.getPlayerCount().values().stream().mapToInt(i -> i).sum();
            if (totalPlayerCount != 0) e.setAvgMasteryPoints((double) e.getSumMasteryPoints() / totalPlayerCount);
        });

        // set summoner key name and summoner region of the top summoner in for every statistic
        log.info("Collection summoner items for top summoners");
        championStatistics.values().forEach(e -> {
            // add summoner item to top summoners
            List<Pair<SummonerItem, ChampionMasteryItem>> topSummoners = new ArrayList<>();
            e.getTopSummoners().forEach(s -> {
                DBTable.SUMMONER.getReadLimiter().acquire();

                ChampionMasteryItem masteryItem = s.getValue();
                String summonerKey = masteryItem.getSummonerKey();

                HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                expressionAttributeValues.put(":sk", new AttributeValue().withS(summonerKey));
                PaginatedQueryList<SummonerItem> query = dynamoDBMapper.query(SummonerItem.class,
                        new DynamoDBQueryExpression<SummonerItem>().withKeyConditionExpression("summonerKey = :sk")
                                .withExpressionAttributeValues(expressionAttributeValues));
                // if the summoner was not found, do no add them to the new top summoners
                if (query.size() == 0) return;
                SummonerItem summonerItem = query.get(0);

                topSummoners.add(new Pair<>(summonerItem, masteryItem));
            });
            e.setTopSummoners(topSummoners);
        });

        // save ChampionStatisticItem instance to dynamoDB and local statistics cache
        // dynamoDBMapper.save(championStatisticItem);
        championStatistics.values().forEach(e -> {
            DBTable.CHAMPION_STATISTIC.getWriteLimiter().acquire();
            dynamoDBMapper.save(e);
            PageDataProvider.championStatisticMap.put(e.getKeyName().toLowerCase(), e);
        });

        log.info(String.format("Generated and saved champion statistics for %d champions" +
                        "(%d/%d entries were used, %d searches were needed, %f read capacity units were consumed)",
                championStatistics.size(), totalMatched, totalScanned, totalSearches, totalReadsConsumed));
    }

    /**
     * read all champion items from the db and return them
     *
     * @return a list of all champions stored in the db
     */
    public static List<ChampionItem> getChampions() {
        // initialize list of all champions
        List<ChampionItem> items = new ArrayList<>();

        // iterate over all champions in the db and add them to the list of champions
        scanPages(ChampionItem.class, new DynamoDBScanExpression(), DBTable.CHAMPION.getReadLimiter(), items::add);

        return items;
    }

    /**
     * generates an overall statistic on the summoners in the db and store it in the db and the local cache
     */
    public static void generateOverallSummonerStatistic() {
        final HashMap<String, Integer> summonerCounts = new HashMap<>();
        final HashMap<String, HashMap<Integer, Integer>> masteryScoreCounts = new HashMap<>();
        final HashMap<String, HashMap<String, Integer>> tierCounts = new HashMap<>();

        log.info("Generating a new overall summoner statistic");

        // iterate over all summoners in the db and analyze the data
        scanPages(SummonerItem.class, new DynamoDBScanExpression(), DBTable.SUMMONER.getReadLimiter(), (s) -> {
            // get summoners region
            String region = summonerKeyToIdRegion(s.getSummonerKey()).getRegion().name();

            // increment summoner count for the region and initialize the count with 0 if it does not exist
            summonerCounts.put(region, summonerCounts.getOrDefault(region, 0) + 1);

            // get the scores hash map for the region or create a new one if needed
            if (!masteryScoreCounts.containsKey(region)) masteryScoreCounts.put(region, new HashMap<>());
            HashMap<Integer, Integer> scores = masteryScoreCounts.get(region);
            int masteryScore = s.getMasteryScore();
            // initialize all score counts lower or equal to the current score with 0 if needed
            // (to have no gaps in the keys)
            while (scores.size() <= masteryScore) scores.put(scores.size(), 0);
            // increment the score count for the given region
            scores.put(masteryScore, scores.get(masteryScore) + 1);

            // get the tiers hash map for the region or create a new one if needed
            if (!tierCounts.containsKey(region)) tierCounts.put(region, new HashMap<>());
            HashMap<String, Integer> tiers = tierCounts.get(region);
            String tier = s.getTier();
            // increment the count for the summoners tier and initialize the count with 0 if it does not exist
            tiers.put(tier, tiers.getOrDefault(tier, 0) + 1);
        });

        // create overall summoner statistic item and fill it with data
        OverallSummonerStatisticItem item = new OverallSummonerStatisticItem();
        item.setSummonerCounts(summonerCounts);
        item.setMasteryScoreCounts(masteryScoreCounts);
        item.setTierCounts(tierCounts);

        // save the statistic to the db
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        DBTable.SUMMONER_STATISTIC.getWriteLimiter().acquire();
        dynamoDBMapper.save(item);

        PageDataProvider.overallSummonerStatisticItem = item;

        log.info("Generated a new overall summoner statistic and stored it in the db and the local cache");
    }

    /**
     * loads the overall summoner statistic from the db and stores it in the local cache
     */
    public static void loadOverallSummonerStatistic() {
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();

        // read overall summoner statistic form the db and store it in the local cache
        DBTable.SUMMONER_STATISTIC.getReadLimiter().acquire();
        String overallKey = OverallSummonerStatisticItem.OVERALL_KEY;
        PageDataProvider.overallSummonerStatisticItem =
                dynamoDBMapper.load(OverallSummonerStatisticItem.class, overallKey, overallKey);
    }

    /**
     * executes a page scan with scanExpression, limited by limiter for objects of the type T with the class clazz
     * and calls action for each object found
     *
     * @param clazz          class of the objects
     * @param scanExpression expression for the scan
     * @param limiter        the read limiter limiting the amount of requests
     * @param action         the function to call for each object
     * @param <T>            the type of the objects
     */
    private static <T> void scanPages(Class<T> clazz, DynamoDBScanExpression scanExpression, RateLimiter limiter, Consumer<? super T> action) {
        // define pageScan and add consumed capacity to scan expression
        ScanResultPage<T> pageScan;
        scanExpression.setReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        // initialize counter, permits and mapper
        int permitsToConsume = 1;
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        int scanned = 0;
        int count = 0;
        do {
            // acquire permits and scan
            limiter.acquire(permitsToConsume);
            pageScan = dynamoDBMapper.scanPage(clazz, scanExpression);

            // update page scan
            scanExpression.setExclusiveStartKey(pageScan.getLastEvaluatedKey());

            // update stats variables
            scanned += pageScan.getScannedCount();
            count += pageScan.getCount();

            // call the action on each result
            pageScan.getResults().forEach(action);

            // calculate permits for next scan
            Double capacityUnits = pageScan.getConsumedCapacity().getCapacityUnits();
            permitsToConsume = (int) (capacityUnits - 1);
            if (permitsToConsume <= 0) permitsToConsume = 1;

            log.info(String.format("Scanned a page for class %s. Results: %d/%d (%d/%d total). Capacity units consumed: %f",
                    clazz.getSimpleName(), pageScan.getCount(), pageScan.getScannedCount(), count, scanned, capacityUnits));
        } while (pageScan.getLastEvaluatedKey() != null);
    }

    /**
     * generates the championStatistics for the given summoners, stores it in the db and returns it
     *
     * @param summonerDto summonerDto with the summoners id
     * @param region      the summoners region
     * @return the generated statistic
     */
    public static SummonerStatisticItem getSummonerStatistic(SummonerDto summonerDto, RiotEndpoint region) {
        // get summoner key from region an and create the expression attribute values for the db queries
        String summonerKey = summonerIdRegionToKey(summonerDto.getId(), region);
        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":sk", new AttributeValue(summonerKey));

        // execute query to check if a generated statistic is already stored in the db
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        PaginatedQueryList<SummonerStatisticItem> statisticItemQuery = dynamoDBMapper.query(SummonerStatisticItem.class,
                new DynamoDBQueryExpression<SummonerStatisticItem>()
                        .withKeyConditionExpression("summonerKey = :sk")
                        .withExpressionAttributeValues(expressionAttributeValues));

        // if one is found which is not older than the up-to-date duration it is returned
        SummonerStatisticItem summonerStatisticItem = statisticItemQuery.stream().findFirst().orElse(null);
        if (summonerStatisticItem != null &&
                System.currentTimeMillis() - summonerStatisticItem.getLastUpdated() > UP_TO_DATE_DURATION) {
            return summonerStatisticItem;
        }

        // otherwise the summoner table is queried for the matching summoner item
        SummonerItem summonerItem;
        PaginatedQueryList<SummonerItem> summonerItemQuery = dynamoDBMapper.query(SummonerItem.class,
                new DynamoDBQueryExpression<SummonerItem>()
                        .withKeyConditionExpression("summonerKey = :sk")
                        .withExpressionAttributeValues(expressionAttributeValues));


        // if the the summoner data is older than the up-to-date duration or non-existent they and
        // their champion mastery data are added to the db and loaded
        summonerItem = summonerItemQuery.stream().findFirst().orElse(null);
        if (summonerItem == null ||
                System.currentTimeMillis() - summonerItem.getLastUpdated() > UP_TO_DATE_DURATION) {
            saveSummonersToDb(region, summonerDto.getId());
        }

        if (summonerItem == null) summonerItem = summonerItemQuery.stream().findFirst().orElse(null);
        // if adding the summoner failed (api or db is unavailable or summoner does not exists)return null
        if (summonerItem == null) return null;

        // create new summoner statistic and initialize it with basic data
        summonerStatisticItem = new SummonerStatisticItem();

        summonerStatisticItem.setSummonerKey(summonerItem.getSummonerKey());
        summonerStatisticItem.setSummonerName(summonerItem.getSummonerName());
        summonerStatisticItem.setChampionMasteries(new ArrayList<>());
        summonerStatisticItem.setSummonerItem(summonerItem);

        // prepare a query for the summoners champion mastery data
        DynamoDBQueryExpression<ChampionMasteryItem> masteryScan = new DynamoDBQueryExpression<ChampionMasteryItem>()
                .withKeyConditionExpression("summonerKey = :sk")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        QueryResultPage<ChampionMasteryItem> pageQuery;

        int permitsToConsume = 1;
        do {
            DBTable.CHAMPION_MASTERY.getReadLimiter().acquire(permitsToConsume);

            // execute query page by page and update page-start as it goes
            pageQuery = dynamoDBMapper.queryPage(ChampionMasteryItem.class, masteryScan);
            masteryScan.setExclusiveStartKey(pageQuery.getLastEvaluatedKey());

            // add champion mastery items to summoner statistic
            summonerStatisticItem.getChampionMasteries().addAll(pageQuery.getResults());

            // calculate permits for next query
            Double capacityUnits = pageQuery.getConsumedCapacity().getCapacityUnits();
            permitsToConsume = (int) (capacityUnits - 1);
            if (permitsToConsume <= 0) permitsToConsume = 1;
        } while (pageQuery.getLastEvaluatedKey() != null);

        // set last updated of summoner statistic to current time
        summonerStatisticItem.setLastUpdated(System.currentTimeMillis());

        // save summoner statistic to db and return it
        dynamoDBMapper.save(summonerStatisticItem);
        return summonerStatisticItem;
    }

    /**
     * deletes all summoner statistics older than the up-to-date duration from the database
     */
    public static void clearSummonerStatistics() {
        // create list of to delete items
        List<SummonerStatisticItem> toDelete = new ArrayList<>();
        // scan the summoner statistics in the db and add all statistics
        // older than the up-to-date duration to the to delete list
        scanPages(SummonerStatisticItem.class, new DynamoDBScanExpression(), DBTable.CHAMPION_STATISTIC.getReadLimiter(),
                (s) -> {
                    if (System.currentTimeMillis() - s.getLastUpdated() > UP_TO_DATE_DURATION) toDelete.add(s);
                });

        // iterate over all statistics to be deleted and delete them
        DynamoDBMapper dynamoDBMapper = DBConnector.getInstance().getDynamoDBMapper();
        toDelete.forEach(s -> {
            DBTable.CHAMPION_STATISTIC.getWriteLimiter().acquire();
            dynamoDBMapper.delete(s);
        });
    }
}
