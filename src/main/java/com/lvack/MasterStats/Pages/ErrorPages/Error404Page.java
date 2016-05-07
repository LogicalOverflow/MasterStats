package com.lvack.MasterStats.Pages.ErrorPages;

import com.lvack.MasterStats.Pages.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Error404PageClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * simple 404 not found error page
 */
public class Error404Page extends BasePage {
    public Error404Page(PageParameters parameters) {
        super(parameters, "404", null);
    }
}
