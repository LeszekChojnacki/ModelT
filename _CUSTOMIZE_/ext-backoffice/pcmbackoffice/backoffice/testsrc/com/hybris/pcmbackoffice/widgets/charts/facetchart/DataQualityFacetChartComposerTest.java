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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.chart.Charts;

import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class DataQualityFacetChartComposerTest
{
	@InjectMocks
	@Spy
	private DataQualityFacetChartComposer dataQualityFacetChartComposer;

	@Before
	public void setUp()
	{
		CockpitTestUtil.mockZkEnvironment();
		when(dataQualityFacetChartComposer.getXAxisLabel()).thenReturn("xLabel");
	}

	@Test
	public void composeChart()
	{
		final Charts charts = new Charts();
		dataQualityFacetChartComposer.composeChart(charts);

		assertThat(charts.getXAxis().getTitle().getText()).isEqualTo("xLabel");
	}
}
