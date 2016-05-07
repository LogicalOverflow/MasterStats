package com.lvack.MasterStats.Pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * ImprintClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * simple static imprint
 */
@MountPath("/imprint")
public class ImprintPage extends StaticPage {
    public ImprintPage(PageParameters parameters) {
        super(parameters, "Imprint", PageType.IMPRINT);
    }
}
