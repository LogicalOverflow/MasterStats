package com.lvack.MasterStats.Pages.ChampionPages;

import com.lvack.MasterStats.Db.DataClasses.ChampionStatisticItem;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Pages.PageType;
import com.lvack.MasterStats.Pages.StaticPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ChampionsPageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * page showing a list of all champions in the statistics cache with links to the individual statistics
 */
@MountPath("/champions")
public class ChampionsPage extends StaticPage {
    public ChampionsPage(PageParameters parameters) {
        super(parameters, "Champions", PageType.CHAMPIONS);

        // get a list of all champions, order them alphabetically and create their entry on the page
        add(new ListView<Map.Entry<String, ChampionStatisticItem>>("champions_list_view", PageDataProvider.championStatisticMap
                .entrySet().stream().sorted((e1, e2) -> e1.getKey().compareToIgnoreCase(e2.getKey()))
                .collect(Collectors.toList())) {
            @Override
            protected void populateItem(ListItem<Map.Entry<String, ChampionStatisticItem>> item) {
                // get data from item
                Map.Entry<String, ChampionStatisticItem> championEntry = item.getModelObject();
                ChampionStatisticItem championStatisticItem = championEntry.getValue();

                // create div
                WebMarkupContainer div = new WebMarkupContainer("champion_entry");

                // create link to champion page
                PageParameters linkParameters = new PageParameters();
                linkParameters.set(0, championStatisticItem.getKeyName());
                BookmarkablePageLink<String> link = new BookmarkablePageLink<>("champion_link",
                        SingleChampionPage.class, linkParameters);
                div.add(link);

                // set champion portrait and name
                link.add(new ExternalImage("champion_portrait", championStatisticItem.getPortraitUrl()));
                link.add(new Label("champion_name", championStatisticItem.getChampionName()));

                item.add(div);
            }
        });
    }
}
