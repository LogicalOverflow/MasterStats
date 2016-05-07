package com.lvack.MasterStats.Pages.ErrorPages;

import com.lvack.MasterStats.Pages.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * ServerErrorPageClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * simple server error page displaying a static error message with contact information
 * use for page expired errors, access denied errors and internal errors
 */
public class ServerErrorPage extends BasePage {
    public ServerErrorPage(PageParameters parameters) {
        super(parameters, "Server Error", null);
    }
}
