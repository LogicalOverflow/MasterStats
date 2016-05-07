package com.lvack.MasterStats.Pages;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BasePageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * basic page providing basic layout like header which sets page title (if provided)
 * and the active page (if provided) all other page inherit form this page
 */
public class BasePage extends WebPage {
    private String selectedRegion = RiotEndpoint.DEFAULT_ENFPOINT.name();

    public BasePage(PageParameters parameters, String subPageTitle, PageType activePage) {
        super(parameters);
        // set the page title if provided
        add(new Label("page_title", String.format("MasterStats - %s", subPageTitle)));
        loadNavbar(activePage);
    }

    public BasePage(PageParameters parameters, PageType activePage) {
        super(parameters);
        loadNavbar(activePage);
    }

    /**
     * generates the navbar and mark the active page
     * @param activePage page to be marked active (null for no active page)
     */
    private void loadNavbar(final PageType activePage) {
        // add an entry for each page type
        add(new ListView<PageType>("navbar_left_elements", Arrays.asList(PageType.values())) {
            @Override
            protected void populateItem(final ListItem<PageType> item) {
                final PageType type = item.getModelObject();
                // create a link for the page type
                BookmarkablePageLink<String> link = new BookmarkablePageLink<>("navbar_left_element_link", type.getPageClass());
                link.add(new Label("navbar_left_element_text", type.getLinkText()));
                // if the type is same as active page, mark it as active
                if (type.equals(activePage)) link.add(new AttributeAppender("class", "active", " "));
                item.add(link);
            }
        });

        // create the summoner search region drop down menu
        // get all valid entries
        List<String> endpoints = Arrays.asList(RiotEndpoint.PLAYABLE_ENDPOINTS)
                .stream().map(RiotEndpoint::name).collect(Collectors.toList());
        // add the options to the drop down menu
        DropDownChoice<String> dropDownChoice = new DropDownChoice<>("navbar_form_regions_select",
                new PropertyModel<>(this, "selectedRegion"), endpoints, new StringChoiceRenderer());
        dropDownChoice.add(new AttributeModifier("name", "region"));
        add(dropDownChoice);
    }

    public String getSelectedRegion() {
        return selectedRegion;
    }

    /**
     * simple choice renderer returning the string as key and id
     */
    private class StringChoiceRenderer implements IChoiceRenderer<String> {

        @Override
        public Object getDisplayValue(String object) {
            return object;
        }

        @Override
        public String getIdValue(String object, int index) {
            return object;
        }

        @Override
        public String getObject(String id, IModel<? extends List<? extends String>> choices) {
            return id;
        }
    }
}
