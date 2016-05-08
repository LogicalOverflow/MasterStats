package com.lvack.MasterStats.Pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * StaticPageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * sets stateless hint for pages. used by all champion pages, the home page and the imprint page.
 */
public class StaticPage extends BasePage {
    protected StaticPage(PageParameters parameters, String subPageTitle, PageType activePage) {
        super(parameters, subPageTitle, activePage);
        setStatelessHint(true);
    }

    protected StaticPage(PageParameters parameters, PageType activePage) {
        super(parameters, activePage);
        setStatelessHint(true);
    }
}
