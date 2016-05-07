package com.lvack.MasterStats.Pages.ErrorPages;

import com.lvack.MasterStats.Pages.StaticPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * ServerErrorPageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * simple server error page displaying a static error message with contact information
 * use for page expired errors, access denied errors and internal errors
 */
public class ServerErrorPage extends StaticPage {
    public ServerErrorPage(PageParameters parameters) {
        super(parameters, "Server Error", null);
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }
}
