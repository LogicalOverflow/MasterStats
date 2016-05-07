package com.lvack.MasterStats.Pages.SummonerPage;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import com.lvack.MasterStats.Db.DataClasses.SummonerStatisticItem;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Pages.BasePage;
import com.lvack.MasterStats.Util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * SummonerQueryForwardPageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * processes the query sent by the summoner search form in the navbar
 * if the query is invalid the page is rendered and shows an error
 */
@Slf4j
@MountPath("/summoners/search")
public class SummonerQueryForwardPage extends BasePage {

    public SummonerQueryForwardPage(PageParameters parameters) {
        super(parameters, "Summoner Search", null);
        // get summoner name and region sent by the form
        String summonerName = parameters.get("summonerName").toString("");
        String regionName = parameters.get("region").toString("").toUpperCase();

        // insert data to error page
        add(new Label("summoner_name", summonerName));
        add(new Label("region", regionName));

        // check if summoner name and region are valid, if not return to show error page
        if (summonerName == null || summonerName.length() == 0) return;
        if (!EnumUtils.isValidEnum(RiotEndpoint.class, regionName)) return;

        // convert region name to RiotEndpoint object
        RiotEndpoint region = RiotEndpoint.valueOf(regionName);

        // generate the summoners statistic
        Pair<String, SummonerStatisticItem> summonerStatistic = PageDataProvider.generateSummonerStatistic(summonerName, region);
        // if statistic generation failed, return to show error page
        if (summonerStatistic == null) return;

        // forward to single summoner page with region and summoner key name
        throw new RestartResponseAtInterceptPageException(SingleSummonerPage.class, new PageParameters()
                .set(0, regionName).set(1, summonerStatistic.getKey()));
    }
}
