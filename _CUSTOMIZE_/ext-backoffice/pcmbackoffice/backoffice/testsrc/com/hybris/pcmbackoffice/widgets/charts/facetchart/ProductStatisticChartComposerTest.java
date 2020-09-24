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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.Tooltip;

import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class ProductStatisticChartComposerTest
{
	@InjectMocks
	@Spy
	private ProductStatisticChartComposer productStatisticChartComposer;

	@Before
	public void setUp()
	{
		CockpitTestUtil.mockZkEnvironment();
	}

	@Test
	public void composeSeries()
	{
		//given
		final Point point1 = new Point("name", 1);
		point1.getDataLabels().setDistance(ProductStatisticChartComposer.CHART_DATA_LABEL_INSIDE_POSITION);
		final Point point2 = new Point("name2", 100);
		point2.getDataLabels().setDistance(ProductStatisticChartComposer.CHART_DATA_LABEL_INSIDE_POSITION);

		final List<Point> points = Arrays.asList(point1, point2);
		final Series series = mock(Series.class);

		when(series.getData()).thenReturn(points);
		when(series.getTooltip()).thenReturn(new Tooltip());

		//when
		productStatisticChartComposer.composeSeries(series);

		//then
		assertThat(point1.getDataLabels().getDistance()).isEqualTo(ProductStatisticChartComposer.CHART_DATA_LABEL_OUTSIDE_POSITION);
		assertThat(point1.getDataLabels().getColor().stringValue()).isEqualTo("black");

		assertThat(point2.getDataLabels().getDistance()).isEqualTo(ProductStatisticChartComposer.CHART_DATA_LABEL_INSIDE_POSITION);

	}

	@Test
	public void shouldComposeWithoutData()
	{
		//given
		final List<Point> points = null;
		final Series series = mock(Series.class);

		when(series.getData()).thenReturn(points);
		when(series.getTooltip()).thenReturn(mock(Tooltip.class));

		//when
		productStatisticChartComposer.composeSeries(series);

		//then
		verify(series.getTooltip()).setHeaderFormat(any());
		verify(series.getTooltip()).setPointFormat(any());
	}
}
