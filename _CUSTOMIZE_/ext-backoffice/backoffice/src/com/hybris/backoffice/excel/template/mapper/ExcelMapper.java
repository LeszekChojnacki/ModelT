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
package com.hybris.backoffice.excel.template.mapper;

import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Abstraction which can map given INPUT to Collection of OUTPUT type
 *
 * @param <INPUT>
 *           type of input of the mapper
 * @param <OUTPUT>
 *           type of output of the mapper
 */
@FunctionalInterface
public interface ExcelMapper<INPUT, OUTPUT> extends Function<INPUT, Collection<OUTPUT>>
{
	/**
	 * Allows to filter result of the mapping.
	 *
	 * @param output
	 *           single result of the mapping
	 * @param filters
	 *           collection of filters
	 * @return whether the given value meets the filter criteria or not
	 */
	default boolean filter(final OUTPUT output, final Collection<ExcelFilter<OUTPUT>> filters)
	{
		return CollectionUtils.emptyIfNull(filters).stream().allMatch(filter -> filter.test(output));
	}

}
