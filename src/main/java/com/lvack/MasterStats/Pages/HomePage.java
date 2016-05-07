package com.lvack.MasterStats.Pages;

import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.wicket7.highcharts.Chart;
import com.lvack.MasterStats.Api.StaticData.RankedTier;
import com.lvack.MasterStats.PageData.PageDataProvider;
import com.lvack.MasterStats.Util.NumberFormatter;
import com.lvack.MasterStats.Util.TierComparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * HomePageClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * the landing page
 */
@Slf4j
@MountPath("/")
public class HomePage extends StaticPage {

    public HomePage(final PageParameters parameters) {
        super(parameters, "Home", PageType.HOME);

        // create the players analyzed chart on the landing page
        Options playerOptions = new Options();
        playerOptions.setTooltip(new Tooltip());
        playerOptions.setTitle(new Title("Players analyzed"));

        // make chart a pie chart
        playerOptions.setChartOptions(new ChartOptions().setType(SeriesType.PIE).setHeight(800));

        // disable shadows for the chart
        playerOptions.setPlotOptions(new PlotOptionsChoice().setPie(new PlotOptions().setShadow(false)));

        // create data points for regions
        PointSeries regionSeries = new PointSeries();
        // iterate over all summoner counts and create points accordingly
        // then add all points to the series
        PageDataProvider.overallSummonerStatisticItem.getSummonerCounts().entrySet().stream()
                .map(e -> new Point(e.getKey(), e.getValue())).forEach(regionSeries::addPoint);
        // set title and size of pie chart
        regionSeries.setName("Players");
        regionSeries.setSize(new PixelOrPercent(80, PixelOrPercent.Unit.PERCENT));
        // position labels and disable their shadows
        regionSeries.setDataLabels(new DataLabels().setDistance(-30).setShadow(false));
        // add series to chart
        playerOptions.addSeries(regionSeries);

        // create data points for tiers
        PointSeries tierSeries = new PointSeries();
        // iterate over all tier counts and create points accordingly
        PageDataProvider.overallSummonerStatisticItem.getTierCounts().entrySet().stream()
                // iterate over all regions and get their tier counts in on stream
                .flatMap(e -> e.getValue().entrySet().stream()
                        // sort points by tier name within a region
                        .sorted((e1, e2) -> TierComparator.staticCompare(e1.getKey(), e2.getKey()))
                        // create a point stream using [region name]-[tier name] as labels and the player counts as values
                        .map(i -> new Point(e.getKey() + "-" + RankedTier.getTierByName(i.getKey()).name(),
                                i.getValue(), RankedTier.getTierByName(i.getKey()).getColor())))
                // add all points to the series
                .forEach(tierSeries::addPoint);
        // set title size of donut chart
        tierSeries.setName("Players");
        tierSeries.setInnerSize(new PixelOrPercent(80, PixelOrPercent.Unit.PERCENT));
        tierSeries.setSize(new PixelOrPercent(100, PixelOrPercent.Unit.PERCENT));
        // disable label shadows
        tierSeries.setDataLabels(new DataLabels().setShadow(false));
        // add series to chart
        playerOptions.addSeries(tierSeries);

        // add the chart to the page
        add(new Chart("player_chart", playerOptions));

        // set player count in text
        add(new Label("player_count", NumberFormatter.formatLong(PageDataProvider.overallSummonerStatisticItem
                .getSummonerCounts().values().stream().mapToInt(i -> i).sum())));
    }

}
