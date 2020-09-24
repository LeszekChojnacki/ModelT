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


public class DefaultFacetChartComposer implements FacetChartComposer
{
	public void composeChart(final Charts charts)
	{
		//default implementation, just use default highcharts settings
	}

	@Override
	public void composeSeries(final Series series)
	{
		//default implementation, just use default highcharts settings
	}
}
