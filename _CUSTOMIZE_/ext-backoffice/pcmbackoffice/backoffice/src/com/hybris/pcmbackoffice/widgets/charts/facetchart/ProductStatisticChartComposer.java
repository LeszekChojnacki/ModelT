/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.pcmbackoffice.widgets.charts.facetchart;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.chart.Charts;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.plotOptions.DataLabels;
import org.zkoss.chart.plotOptions.PiePlotOptions;
import org.zkoss.util.resource.Labels;


public class ProductStatisticChartComposer implements FacetChartComposer
{

	private static final Integer CHART_DATA_LABEL_PLACEMENT_BAUNDARY = 5;
	protected static final Integer CHART_DATA_LABEL_INSIDE_POSITION = -42;
	protected static final Integer CHART_DATA_LABEL_OUTSIDE_POSITION = 30;

	@Override
	public void composeChart(final Charts charts)
	{
		charts.getChart().setType(Charts.PIE);
		charts.getLegend().setEnabled(false);

		final PiePlotOptions plotOptions = charts.getPlotOptions().getPie();
		plotOptions.setAllowPointSelect(true);
		plotOptions.setCursor("pointer");
		plotOptions.setShadow(false);
		plotOptions.setInnerSize("50%");
		plotOptions.setShowInLegend(false);

		final DataLabels dataLabels = plotOptions.getDataLabels();

		dataLabels.setFormat("{point.percentage:.1f} %");
		dataLabels.setDistance(CHART_DATA_LABEL_INSIDE_POSITION);
		dataLabels.setColor("white");

		charts.getXAxis().setType("category");
		charts.getXAxis().setTitle(getXAxisLabel());
		charts.getYAxis().setTitle(getYAxisLabel());
	}

	@Override
	public void composeSeries(final Series series)
	{
		series.getTooltip().setHeaderFormat("<span style=\"color:{point.color}\">●</span> {point.key}: <b>{point.y}</b>");
		series.getTooltip().setPointFormat(StringUtils.EMPTY);

		final List<Point> points = series.getData();
		if (points == null)
		{
			return;
		}

		final Integer totalPointsValue = points.stream() //
				.mapToInt(p -> p.getY().intValue()) //
				.sum();

		points.stream() //
				.filter(p -> ((p.getY().doubleValue() / totalPointsValue) * 100) < CHART_DATA_LABEL_PLACEMENT_BAUNDARY) //
				.forEach(p -> {
					p.getDataLabels().setDistance(CHART_DATA_LABEL_OUTSIDE_POSITION);
					p.getDataLabels().setColor("black");
				});
	}

	protected String getXAxisLabel()
	{
		return Labels.getLabel("solrchart.productstats.xaxis");
	}

	protected String getYAxisLabel()
	{
		return Labels.getLabel("solrchart.productstats.yaxis");
	}
}
