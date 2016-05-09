package com.lvack.MasterStats.Pages.ChampionPages;

import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import com.googlecode.wickedcharts.wicket7.highcharts.Chart;
import com.lvack.MasterStats.Db.DataClasses.ChampionMasteryItem;
import com.lvack.MasterStats.Db.DataClasses.ChampionStatisticItem;
import com.lvack.MasterStats.Db.DataClasses.SummonerItem;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Pages.StaticPage;
import com.lvack.MasterStats.Pages.SummonerPage.SingleSummonerPage;
import com.lvack.MasterStats.Pages.SummonerPage.SummonerQueryForwardPage;
import com.lvack.MasterStats.Util.GradeComparator;
import com.lvack.MasterStats.Util.NumberFormatter;
import com.lvack.MasterStats.Util.Pair;
import com.lvack.MasterStats.Util.SummonerKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.wicketstuff.annotation.mount.MountPath;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lvack.MasterStats.Util.SummonerKeyUtils.summonerKeyToIdRegion;

/**
 * SingleChampionPageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * page showing in-depth statistics for a single champion
 */
@Slf4j
@MountPath("/champion")
public class SingleChampionPage extends StaticPage {

    public SingleChampionPage(PageParameters parameters) {
        super(parameters, null);

        // get champion name from url and convert to lower case as cache key are lowercase
        StringValue championKeyValue = parameters.get(0);
        String championKey = championKeyValue.toString().toLowerCase();

        // check if champion exits in the cache and raise a 404 if not
        if (!PageDataProvider.championStatisticMap.containsKey(championKey))
            throw new AbortWithHttpErrorCodeException(404, "Champion not found!");

        // get champion item from cache and update page title and content with champion name, title and portrait
        ChampionStatisticItem championStatisticItem = PageDataProvider.championStatisticMap.get(championKey);
        super.add(new Label("page_title", String.format("MasterStats - %s", championStatisticItem.getChampionName())));
        add(new Label("champion_name", championStatisticItem.getChampionName()));
        add(new Label("champion_title", championStatisticItem.getChampionTitle()));
        add(new ExternalImage("champion_portrait", championStatisticItem.getPortraitUrl()));

        List<Pair<SummonerItem, ChampionMasteryItem>> topSummoners = championStatisticItem.getTopSummoners();
        if (topSummoners == null) topSummoners = new ArrayList<>();
        // get the three highest scoring summoners in descending order
        topSummoners = topSummoners.stream().sorted((e1, e2) -> e2.getValue().getChampionPoints() -
                e1.getValue().getChampionPoints()).limit(3).collect(Collectors.toList());
        // add those top summoners to the page
        add(new ListView<Pair<SummonerItem, ChampionMasteryItem>>("top_summoners", topSummoners) {
            @Override
            protected void populateItem(ListItem<Pair<SummonerItem, ChampionMasteryItem>> item) {
                Pair<SummonerItem, ChampionMasteryItem> pair = item.getModelObject();
                // create a link to the summoner
                SummonerKey summonerKey = summonerKeyToIdRegion(pair.getKey().getSummonerKey());
                PageParameters linkParameters = new PageParameters();
                linkParameters.set("summonerName", pair.getKey().getSummonerName());
                linkParameters.set("region", summonerKey.getRegion().name());
                BookmarkablePageLink<String> link = new BookmarkablePageLink<>("summoner_link",
                        SummonerQueryForwardPage.class, linkParameters);

                item.add(link);

                // insert the summoner icon
                link.add(new ExternalImage("summoner_icon", String.format(
                        "http://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png", PageDataProvider.getVersion(),
                        pair.getKey().getProfileIconId())));
                // insert the summoner name
                link.add(new Label("summoner_name", pair.getKey().getSummonerName()));
                // insert mastery points and champion level
                link.add(new Label("mastery_stats", String.format("%s - Level %d", NumberFormatter.formatLong(
                        pair.getValue().getChampionPoints()), pair.getValue().getChampionLevel())));
            }
        });

        // create player distribution chart
        Options playerOptions = new Options();
        playerOptions.setTooltip(new Tooltip());
        playerOptions.setTitle(new Title("Player Regions"));

        // make chart a pie chart
        playerOptions.setChartOptions(new ChartOptions().setType(SeriesType.PIE));

        // disable chart shadows
        playerOptions.setPlotOptions(new PlotOptionsChoice().setPie(new PlotOptions().setShadow(false)));

        // enable legend
        playerOptions.setLegend(new Legend().setReversed(true));

        // create data series
        PointSeries playerRegionSeries = new PointSeries();
        // iterate over all player counts and add a point to the series accordingly
        championStatisticItem.getPlayerCount().entrySet().stream()
                .map(e -> new Point(e.getKey(), e.getValue()))
                .forEach(playerRegionSeries::addPoint);
        // set series name and size
        playerRegionSeries.setName("Players");
        playerRegionSeries.setSize(new PixelOrPercent(100, PixelOrPercent.Unit.PERCENT));
        // disable labels
        playerRegionSeries.setDataLabels(new DataLabels().setEnabled(false));
        // add series to legend
        playerRegionSeries.setShowInLegend(true);

        // add series to chart
        playerOptions.addSeries(playerRegionSeries);

        // add chart to site
        add(new Chart("player_chart", playerOptions));

        // create grade distribution chart
        Options gradeOptions = new Options();
        // set tooltip formatting
        gradeOptions.setTooltip(new Tooltip()
                .setFormatter(new Function("return this.series.name + ' - ' + this.x + ': ' + this.y;")));
        gradeOptions.setTitle(new Title("Highest Grades"));

        // make chart a column chart
        gradeOptions.setChartOptions(new ChartOptions().setType(SeriesType.COLUMN));

        // make columns stacked
        gradeOptions.setPlotOptions(new PlotOptionsChoice().setSeries(new PlotOptions().setStacking(Stacking.NORMAL)));

        // set categories on x-axis to the grade in correct order (excluding "null" - no grade)
        gradeOptions.setxAxis(new Axis().setCategories(ChampionStatisticItem.GRADES.stream()
                .filter(g -> !"null".equals(g)).sorted(new GradeComparator())
                .collect(Collectors.toList())));
        // set minimum and title for the y-axis
        gradeOptions.setyAxis(new Axis().setMin(0).setTitle(new Title("Grade Count")));

        // add legend and reverse order of elements
        gradeOptions.setLegend(new Legend().setReversed(true));

        // create a series for each region containing the grade counts and add all series to the chart
        championStatisticItem.getHighestGradeCounts().entrySet().stream()
                // create a series for each region with the regions name as series name
                .map(e -> new SimpleSeries().setName(e.getKey()).setData(e.getValue().entrySet().stream()
                        // remove grade "null" - no grade yet
                        .filter(i -> !"null".equals(i.getKey()))
                        // order by grade
                        .sorted((e1, e2) -> GradeComparator.staticCompare(e1.getKey(), e2.getKey()))
                        // get grade counts as series values
                        .map(Map.Entry::getValue).collect(Collectors.toList())))
                // add all series to the chart
                .forEach(gradeOptions::addSeries);

        // add chart to site
        add(new Chart("grade_chart", gradeOptions));

        // create level distribution chart
        Options levelOptions = new Options();
        // set tooltip formatting
        levelOptions.setTooltip(new Tooltip().setFormatter(new Function()
                .setFunction("return this.series.name + ' - ' + this.x + ': ' + this.y")));
        levelOptions.setTitle(new Title("Champion Levels"));

        // make chart a zoomable area chart
        levelOptions.setChartOptions(new ChartOptions().setType(SeriesType.AREA).setZoomType(ZoomType.XY));

        // make chart stacked, with 1 px wide lines and with circles as markers which
        // are only shown when the mouse hovers over the data point
        levelOptions.setPlotOptions(new PlotOptionsChoice().setArea(new PlotOptions()
                .setStacking(Stacking.NORMAL).setLineWidth(1)
                .setMarker(new Marker().setEnabled(false).setSymbol(new Symbol(Symbol.PredefinedSymbol.CIRCLE)))));

        // add legend and reverse order of elements
        levelOptions.setLegend(new Legend().setReversed(true));

        // set categories on x-axis to the levels in correct order
        levelOptions.setxAxis(new Axis().setTitle(new Title("Levels"))
                .setCategories(IntStream.rangeClosed(ChampionStatisticItem.MIN_CHAMPION_LEVEL,
                        ChampionStatisticItem.MAX_CHAMPION_LEVEL).mapToObj(String::valueOf)
                        .collect(Collectors.toList())));
        // set minimum and title for the y-axis
        levelOptions.setyAxis(new Axis().setMin(0).setTitle(new Title("Player Count")));

        // create a series for each region containing the level counts and add all series to the chart
        championStatisticItem.getLevelCounts().entrySet().stream()
                // create a series for each region with the regions name as series name
                .map(e -> new SimpleSeries().setName(e.getKey()).setData(e.getValue().entrySet().stream()
                        // sort level by level number and use the player counts as data for the series
                        .sorted((e1, e2) -> e1.getKey() - e2.getKey()).map(Map.Entry::getValue)
                        .collect(Collectors.toList())))
                // add all series to the chart
                .forEach(levelOptions::addSeries);

        // add the chart to the site
        add(new Chart("level_chart", levelOptions));

        // create the score distribution chart
        Options scoreOptions = new Options();
        // set tooltip formatting
        scoreOptions.setTooltip(new Tooltip().setFormatter(new Function()
                .setFunction("return this.series.name + ' - ' + this.x + ': ' + this.y")));
        scoreOptions.setTitle(new Title("Player Scores"));

        // make chart a zoomable area chart
        scoreOptions.setChartOptions(new ChartOptions().setType(SeriesType.AREA).setZoomType(ZoomType.XY));

        // make chart stacked, with 1 px wide lines and with circles as markers which
        // are only shown when the mouse hovers over the data point
        scoreOptions.setPlotOptions(new PlotOptionsChoice().setArea(
                new PlotOptions().setStacking(Stacking.NORMAL).setLineWidth(1)
                        .setMarker(new Marker().setEnabled(false).setSymbol(new Symbol(Symbol.PredefinedSymbol.CIRCLE)))));

        // add legend and reverse order of elements
        scoreOptions.setLegend(new Legend().setReversed(true));

        // set title for the x-axis and add a plot line for the average champion score
        scoreOptions.setxAxis(new Axis().setTitle(new Title("Scores"))
                .addPlotLine(new PlotLine().setValue((float) championStatisticItem.getAvgMasteryPoints())
                        // make the plot line 2 px wide, dashed and gray
                        .setWidth(2).setColor(Color.GRAY).setDashStyle(GridLineDashStyle.DOT)
                        // label it with the average champion score
                        .setLabel(new Labels(String.format("Average Champion Score - %d",
                                (int) championStatisticItem.getAvgMasteryPoints())))));
        // set minimum and title for the y-axis
        scoreOptions.setyAxis(new Axis().setMin(0).setTitle(new Title("Player Count")));

        // create a series for each region containing the score counts and add all series to the chart
        championStatisticItem.getScoreDistribution().entrySet().stream()
                // create a series for each region with the regions name as series name
                .map(e -> new SimpleSeries().setName(e.getKey())
                        // use the score distribution step size as point interval
                        .setPointInterval(ChampionStatisticItem.SCORE_DISTRIBUTION_STEP_SIZE)
                        // get all champion score counts
                        .setData(e.getValue().entrySet().stream()
                                // remove all with an step index to high
                                .filter(i -> i.getKey() < ChampionStatisticItem.CHAMPION_SCORE_STEP_COUNT)
                                // order them by their step index and get the player count
                                .sorted((e1, e2) -> e1.getKey() - e2.getKey()).map(Map.Entry::getValue)
                                // use the player counts as data for the series
                                .collect(Collectors.toList())))
                // add all series to the chart
                .forEach(scoreOptions::addSeries);

        // add chart to site
        add(new Chart("score_chart", scoreOptions));
    }
}
