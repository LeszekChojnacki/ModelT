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


import org.zkoss.chart.Charts;
import org.zkoss.chart.Series;
import org.zkoss.chart.plotOptions.ColumnPlotOptions;
import org.zkoss.chart.plotOptions.DataLabels;
import org.zkoss.util.resource.Labels;


public class DataQualityFacetChartComposer implements FacetChartComposer
{
	@Override
	public void composeChart(final Charts charts)
	{
		charts.getChart().setType(Charts.COLUMN);
		charts.getLegend().setEnabled(false);

		final ColumnPlotOptions plotOptions = charts.getPlotOptions().getColumn();
		plotOptions.setAllowPointSelect(true);
		plotOptions.setCursor("pointer");
		plotOptions.setColorByPoint(true);
		plotOptions.setShadow(true);

		final DataLabels dataLabels = plotOptions.getDataLabels();
		dataLabels.setEnabled(true);
		dataLabels.setEnabled(true);
		dataLabels.setFormat("{point.value}");
		dataLabels.setColor("#000000");
		dataLabels.setAlign("center");
		dataLabels.setShadow(false);

		charts.getXAxis().setType("category");
		charts.getXAxis().setTitle(getXAxisLabel());
		charts.getYAxis().setTitle(getYAxisLabel());
	}

	@Override
	public void composeSeries(final Series series)
	{
		series.setType("column");
		series.getTooltip().setHeaderFormat("<span style=\"color:{point.color}\">●</span> {point.key}: <b>{point.y}</b><br/>");
		series.getTooltip().setPointFormat("{series.name}");
	}

	protected String getXAxisLabel()
	{
		return Labels.getLabel("solrchart.dataquality.xaxis");
	}

	protected String getYAxisLabel()
	{
		return Labels.getLabel("solrchart.dataquality.yaxis");
	}
}
