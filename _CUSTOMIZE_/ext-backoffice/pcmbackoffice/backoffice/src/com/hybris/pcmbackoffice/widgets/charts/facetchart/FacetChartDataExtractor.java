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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.zkoss.chart.Point;

import com.hybris.cockpitng.search.data.FullTextSearchData;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.facet.FacetValueData;


public class FacetChartDataExtractor
{

	private final static Comparator<FacetValueData> rangeLabelsComparator = Comparator.comparing(FacetValueData::getName,
			Comparator.comparing(FacetChartDataExtractor::findFirstInteger, Comparator.nullsFirst(Comparator.naturalOrder())));

	private static Point facetValeDataToPointMapper(final FacetValueData facetValueData)
	{
		return new Point(facetValueData.getName(), facetValueData.getCount());
	}

	protected static Comparator<FacetValueData> getRangeLabelsComparator()
	{
		return rangeLabelsComparator;
	}

	private static Integer findFirstInteger(final String inputString)
	{
		final Matcher integerMatcher = Pattern.compile("\\d+").matcher(inputString);
		final boolean continsInteger = integerMatcher.find();
		if (!continsInteger)
		{
			return null;
		}
		final String number = integerMatcher.group();
		return Integer.parseInt(number);
	}

	public List<Point> getPoints(final FullTextSearchData fullTextSearchData, final String facetCode)
	{
		return fullTextSearchData.getFacets().stream()//
				.filter(facet -> facetCode.equals(facet.getName()))//
				.findFirst()//
				.map(FacetData::getFacetValues)//
				.orElseGet(Collections::emptyList)//
				.stream()//
				.sorted(rangeLabelsComparator) //
				.map(FacetChartDataExtractor::facetValeDataToPointMapper)//
				.collect(Collectors.toList());
	}
}
