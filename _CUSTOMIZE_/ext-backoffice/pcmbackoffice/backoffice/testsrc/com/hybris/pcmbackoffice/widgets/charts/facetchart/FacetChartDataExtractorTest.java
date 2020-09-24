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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.chart.Point;

import com.hybris.cockpitng.search.data.FullTextSearchData;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.facet.FacetType;
import com.hybris.cockpitng.search.data.facet.FacetValueData;


@RunWith(MockitoJUnitRunner.class)
public class FacetChartDataExtractorTest
{
	private final String VALID_FACET_NAME = "facetName";
	private final String ANOTHER_FACET_NAME = "anotherName";

	@InjectMocks
	private FacetChartDataExtractor facetChartDataExtractor;

	@Mock
	private FullTextSearchData fullTextSearchData;

	@Test
	public void shouldCreateChartPointsFromFacets()
	{
		when(fullTextSearchData.getFacets()).thenReturn(prepareFacetsData());

		final List<Point> points = facetChartDataExtractor.getPoints(fullTextSearchData, VALID_FACET_NAME);

		assertThat(points).hasSize(5);

		for (int i = 0; i < 5; i++)
		{
			assertThat(points.get(i).getName()).isEqualTo(createDataValueName(VALID_FACET_NAME, i));
			assertThat(points.get(i).getY().intValue()).isEqualTo(i);
		}
	}

	private List<FacetData> prepareFacetsData()
	{
		final List<FacetValueData> facedDataValues = IntStream.range(0, 5)//
				.mapToObj(i -> new FacetValueData(createDataValueName(VALID_FACET_NAME, i), i, false))//
				.collect(Collectors.toList());

		final FacetData facedData = new FacetData(VALID_FACET_NAME, VALID_FACET_NAME, FacetType.REFINE, facedDataValues);

		final List<FacetValueData> anotherFacedDataValues = IntStream.range(0, 5)//
				.mapToObj(i -> new FacetValueData(createDataValueName(ANOTHER_FACET_NAME, i), i, false))//
				.collect(Collectors.toList());

		final FacetData anotherFacetData = new FacetData(ANOTHER_FACET_NAME, ANOTHER_FACET_NAME, FacetType.REFINE,
				anotherFacedDataValues);

		return Lists.newArrayList(facedData, anotherFacetData);
	}

	private String createDataValueName(final String facetName, final int facetDataNumber)
	{
		return String.format("%s - data value %d", facetName, facetDataNumber);
	}

	@Test
	public void shouldCompareRangeLabels()
	{
		final Comparator<FacetValueData> rangeLabelsComparator = FacetChartDataExtractor.getRangeLabelsComparator();

		assertThat(rangeLabelsComparator.compare(new FacetValueData("20-39%", 1, true), new FacetValueData("0-19%", 1, true)))
				.isGreaterThan(0);
		assertThat(rangeLabelsComparator.compare(new FacetValueData("100%", 1, true), new FacetValueData("80-99%", 1, true)))
				.isGreaterThan(0);
		assertEquals(0, rangeLabelsComparator.compare(new FacetValueData("12suffix", 1, true), new FacetValueData("12", 1, true)));
		assertThat(rangeLabelsComparator.compare(new FacetValueData("not-a-number", 1, true), new FacetValueData("7", 1, true)))
				.isLessThan(0);
	}
}
