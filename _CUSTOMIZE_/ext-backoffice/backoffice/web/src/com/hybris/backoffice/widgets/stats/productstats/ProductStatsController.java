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
package com.hybris.backoffice.widgets.stats.productstats;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.chart.Chart;
import org.zkoss.chart.Charts;
import org.zkoss.chart.Color;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.options3D.Options3D;
import org.zkoss.chart.plotOptions.ColumnPlotOptions;
import org.zkoss.chart.plotOptions.DataLabels;
import org.zkoss.chart.plotOptions.PiePlotOptions;
import org.zkoss.chart.plotOptions.PlotOptions;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.util.DefaultWidgetController;


public class ProductStatsController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductStatsController.class);

	private static final String SETTING_TITLE = "chartTitle";
	private static final String SETTING_SHOW_TOOLTIP = "showTooltip";
	private static final String SETTING_TOOLTIP_FORMAT = "tooltipFormat";
	private static final String SETTING_ENABLE_EXPORT = "enableExport";
	private static final String SETTING_CHART_TYPE = "chartType";
	private static final String SETTING_PLOT_BORDER_WIDTH = "plotBorderWidth";
	private static final String SETTING_PLOT_SHADOW = "plotShadow";
	private static final String SETTING_ALLOW_POINT_SELECT = "allowPointSelect";
	private static final String SETTING_CURSOR_TYPE = "cursorType";
	private static final String SETTING_INNER_SIZE = "innerSize";
	private static final String SETTING_SHOW_LEGEND = "showLegend";
	private static final String SETTING_DEPTH = "depth";
	private static final String SETTING_ENABLE_3D = "enable3d";
	private static final String SETTING_ALPHA_3D_ANGLE = "alpha3dangle";
	private static final String SETTING_BETA_3D_ANGLE = "beta3dangle";
	private static final String SETTING_ENABLE_DATA_LABELS = "enableDataLabels";
	private static final String SETTING_DATA_LABELS_FORMAT = "dataLabelsFormat";
	private static final String SETTING_CHART_COLOR_PREFIX = "chartColor%d";
	private static final String SETTING_CHART_COLOR_1 = String.format(SETTING_CHART_COLOR_PREFIX, 1);
	private static final String SETTING_CHART_COLOR_2 = String.format(SETTING_CHART_COLOR_PREFIX, 2);
	private static final String SETTING_CHART_COLOR_3 = String.format(SETTING_CHART_COLOR_PREFIX, 3);

	@WireVariable
	private transient BackofficeProductCounter backofficeProductCounter;

	@Wire("charts")
	private Charts charts;

	@Wire
	private Label productsTotalCountValue;

	@Wire
	private Label productsUnapprovedCountValue;

	@Wire
	private Label productsCheckCountValue;

	@Wire
	private Label productsApprovedCountValue;

	@Wire
	private Button productsLinkBrowseAll;

	@Wire
	private Button productsLinkCreateNew;


	@Override
	public void initialize(@Nonnull final Component comp)
	{
		super.initialize(comp);
		initializeProductsCounts();
		initializeChartConfiguration();
		initializeChartSeries();
	}

	protected void initializeProductsCounts()
	{
		getProductsTotalCountValue().setValue(String.valueOf(countProducts()));
		getProductsUnapprovedCountValue().setValue(String.valueOf(countProducts(ArticleApprovalStatus.UNAPPROVED)));
		getProductsCheckCountValue().setValue(String.valueOf(countProducts(ArticleApprovalStatus.CHECK)));
		getProductsApprovedCountValue().setValue(String.valueOf(countProducts(ArticleApprovalStatus.APPROVED)));

	}

	protected void initializeChartConfiguration()
	{
		getCharts().setTitle(Labels.getLabel(getWidgetSettingString(SETTING_TITLE)));
		getCharts().getTooltip().setEnabled(getWidgetSettingBoolean(SETTING_SHOW_TOOLTIP));
		getCharts().getTooltip().setPointFormat(getWidgetSettingString(SETTING_TOOLTIP_FORMAT));
		getCharts().getExporting().setEnabled(getWidgetSettingBoolean(SETTING_ENABLE_EXPORT));
		initializeChartOptions();
		initializePlotOptions();
	}

	protected void initializeChartOptions()
	{
		final Chart chartOptions = getCharts().getChart();
		chartOptions.setType(getWidgetSettingString(SETTING_CHART_TYPE));
		chartOptions.setPlotBorderWidth(getWidgetSettingInt(SETTING_PLOT_BORDER_WIDTH));
		chartOptions.setPlotShadow(getWidgetSettingBoolean(SETTING_PLOT_SHADOW));
	}

	protected void initializePlotOptions()
	{
		final PlotOptions plotOptions = getCharts().getPlotOptions();

		switch (getCharts().getType())
		{
			case Charts.PIE:
				initializePiePlotOptions(plotOptions.getPie());
				break;
			case Charts.BAR:
				initializeColumnPlotOptions(plotOptions.getBar());
				break;
			case Charts.COLUMN:
				initializeColumnPlotOptions(plotOptions.getColumn());
				break;
			default:
				warnAboutUnsupportedChartType();
				break;
		}
	}

	protected void warnAboutUnsupportedChartType()
	{
		LOG.warn("unsupported chart type was chosen, neglecting plot options");
	}

	protected void initializePiePlotOptions(@Nonnull final PiePlotOptions plotOptions)
	{
		plotOptions.setAllowPointSelect(getWidgetSettingBoolean(SETTING_ALLOW_POINT_SELECT));
		plotOptions.setCursor(getWidgetSettingString(SETTING_CURSOR_TYPE));
		plotOptions.setInnerSize(getWidgetSettingString(SETTING_INNER_SIZE));
		plotOptions.setShowInLegend(getWidgetSettingBoolean(SETTING_SHOW_LEGEND));
		plotOptions.setDepth(getWidgetSettingInt(SETTING_DEPTH));
		plotOptions.setColors(createChartColors());
		initializeDataLabels(plotOptions.getDataLabels());
		initializeOptions3d();
	}

	protected void initializeColumnPlotOptions(@Nonnull final ColumnPlotOptions plotOptions)
	{
		plotOptions.setAllowPointSelect(getWidgetSettingBoolean(SETTING_ALLOW_POINT_SELECT));
		plotOptions.setCursor(getWidgetSettingString(SETTING_CURSOR_TYPE));
		plotOptions.setShowInLegend(getWidgetSettingBoolean(SETTING_SHOW_LEGEND));
		plotOptions.setDepth(getWidgetSettingInt(SETTING_DEPTH));
		plotOptions.setColors(createChartColors());
		initializeDataLabels(plotOptions.getDataLabels());
		initializeOptions3d();
	}

	protected void initializeDataLabels(@Nonnull final DataLabels dataLabels)
	{
		dataLabels.setEnabled(getWidgetSettingBoolean(SETTING_ENABLE_DATA_LABELS));
		dataLabels.setFormat(getWidgetSettingString(SETTING_DATA_LABELS_FORMAT));
	}

	protected List<Color> createChartColors()
	{
		final List<Color> colors = new ArrayList<>();
		colors.add(new Color(getWidgetSettingString(SETTING_CHART_COLOR_1)));
		colors.add(new Color(getWidgetSettingString(SETTING_CHART_COLOR_2)));
		colors.add(new Color(getWidgetSettingString(SETTING_CHART_COLOR_3)));
		return colors;
	}

	protected void initializeOptions3d()
	{
		final Options3D options3d = getCharts().getOptions3D();
		options3d.setEnabled(getWidgetSettingBoolean(SETTING_ENABLE_3D));
		options3d.setAlpha(getWidgetSettingInt(SETTING_ALPHA_3D_ANGLE));
		options3d.setBeta(getWidgetSettingInt(SETTING_BETA_3D_ANGLE));
	}

	protected void initializeChartSeries()
	{
		final Series series = getCharts().getSeries();
		createChartPoints().forEach(series::addPoint);
	}

	protected List<Point> createChartPoints()
	{
		final long allProductsSize = countProducts();
		final List<Point> points = new ArrayList<>();
		final BiFunction<ArticleApprovalStatus, Long, Double> percent = (s, a) -> ((double) countProducts(s) / (double) a) * 100;

		final double unapprovedProductsPercent = percent.apply(ArticleApprovalStatus.UNAPPROVED, allProductsSize);
		final double checkProductsPercent = percent.apply(ArticleApprovalStatus.CHECK, allProductsSize);
		final double approvedProductsPercent = percent.apply(ArticleApprovalStatus.APPROVED, allProductsSize);

		points.add(new Point(ArticleApprovalStatus.UNAPPROVED.name(), unapprovedProductsPercent));
		points.add(new Point(ArticleApprovalStatus.CHECK.name(), checkProductsPercent));
		points.add(new Point(ArticleApprovalStatus.APPROVED.name(), approvedProductsPercent));
		return points;
	}

	protected long countProducts()
	{
		return getBackofficeProductCounter().countProducts();
	}

	protected long countProducts(@Nonnull final ArticleApprovalStatus status)
	{
		return getBackofficeProductCounter().countProducts(status);
	}

	protected String getWidgetSettingString(final String key)
	{
		return getWidgetSettings().getString(key);
	}

	protected int getWidgetSettingInt(final String key)
	{
		return getWidgetSettings().getInt(key);
	}

	protected boolean getWidgetSettingBoolean(final String key)
	{
		return getWidgetSettings().getBoolean(key);
	}

	public Charts getCharts()
	{
		return charts;
	}

	public void setCharts(final Charts chart)
	{
		this.charts = chart;
	}

	public BackofficeProductCounter getBackofficeProductCounter()
	{
		return backofficeProductCounter;
	}

	public void setBackofficeProductCounter(final BackofficeProductCounter backofficeProductCounter)
	{
		this.backofficeProductCounter = backofficeProductCounter;
	}

	public Label getProductsTotalCountValue()
	{
		return productsTotalCountValue;
	}

	public Label getProductsUnapprovedCountValue()
	{
		return productsUnapprovedCountValue;
	}

	public Label getProductsCheckCountValue()
	{
		return productsCheckCountValue;
	}

	public Label getProductsApprovedCountValue()
	{
		return productsApprovedCountValue;
	}

	public Button getProductsLinkBrowseAll()
	{
		return productsLinkBrowseAll;
	}

	public Button getProductsLinkCreateNew()
	{
		return productsLinkCreateNew;
	}

}
