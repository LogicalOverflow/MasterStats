package com.lvack.MasterStats.Pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * StatelessPageClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * sets stateless hint for pages. used by all champion pages, the home page and the imprint page.
 */
public class StaticPage extends BasePage {
    public StaticPage(PageParameters parameters, String subPageTitle, PageType activePage) {
        super(parameters, subPageTitle, activePage);
        setStatelessHint(true);
    }

    public StaticPage(PageParameters parameters, PageType activePage) {
        super(parameters, activePage);
        setStatelessHint(true);
    }
}
