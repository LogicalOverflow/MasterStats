package com.lvack.MasterStats.Pages;

import com.lvack.MasterStats.Pages.ChampionPages.ChampionsPage;
import org.apache.wicket.markup.html.WebPage;

/**
 * PageTypeClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * simple enum of pages in the navbar
 */
public enum PageType {
    HOME(HomePage.class, "Home"),
    CHAMPIONS(ChampionsPage.class, "Champions"),
    IMPRINT(ImprintPage.class, "Imprint");
    private final Class<? extends WebPage> pageClass;
    private final String linkText;

    PageType(Class<? extends WebPage> pageClass, String linkText) {
        this.pageClass = pageClass;
        this.linkText = linkText;
    }

    public Class<? extends WebPage> getPageClass() {
        return pageClass;
    }

    public String getLinkText() {
        return linkText;
    }
}
