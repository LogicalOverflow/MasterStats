package com.lvack.MasterStats.Pages.SummonerPage;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import com.lvack.MasterStats.Db.DataClasses.SummonerStatisticItem;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Pages.BasePage;
import com.lvack.MasterStats.Util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * SummonerQueryResultClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * page which is never render
 * processes the query sent by the summoner search form in the navbar
 */
@Slf4j
@MountPath("/summoners/search")
public class SummonerQueryForwardPage extends BasePage {
    private String summonerName;
    private String regionName;

    public SummonerQueryForwardPage(PageParameters parameters) {
        super(parameters, "Summoner Search", null);
        // get summoner name and region sent by the form
        summonerName = parameters.get("summonerName").toString("");
        regionName = parameters.get("region").toString("").toUpperCase();

        // check if summoner name and region are valid, return 404 if not
        if (summonerName == null || summonerName.length() == 0) summonerNotFound();
        if (!EnumUtils.isValidEnum(RiotEndpoint.class, regionName)) summonerNotFound();
        // convert region name to RiotEndpoint object
        RiotEndpoint region = RiotEndpoint.valueOf(regionName);

        // generate the summoners statistic
        Pair<String, SummonerStatisticItem> summonerStatistic = PageDataProvider.generateSummonerStatistic(summonerName, region);
        // if statistic generation failed, return 404
        if (summonerStatistic == null) summonerNotFound();

        // forward to single summoner page with region and summoner key name
        throw new RestartResponseAtInterceptPageException(SingleSummonerPage.class, new PageParameters()
                .set(0, regionName).set(1, summonerStatistic.getKey()));
    }

    private void summonerNotFound() {
        throw new AbortWithHttpErrorCodeException(404, String.format("Summoner with the name '%s' not found in %s",
                summonerName, regionName));
    }
}
