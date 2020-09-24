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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Allows to chain two {@link ExcelMapper}s. The result of the first {@link ExcelMapper} is then mapped to result of
 * {@link ExcelMapper}. You can specify two collection of {@link ExcelFilter}s - one for first mapping operation and
 * another for second mapping operation. Use {@link #setFilters1(Collection)} to filter result of {@link #mapper1} and
 * {@link #setFilters2(Collection)} to filter result of {@link #mapper2}.
 *
 * @param <INPUT>
 *           type of the input value
 * @param <ATTRIBUTE>
 *           type of the output. It has to extend {@link ExcelAttribute}
 */
public class ChainMapper<INPUT, ATTRIBUTE extends ExcelAttribute> implements ExcelMapper<INPUT, ATTRIBUTE>
{
	private ExcelMapper<INPUT, AttributeDescriptorModel> mapper1;
	private ExcelMapper<Collection<AttributeDescriptorModel>, ATTRIBUTE> mapper2;
	private Collection<ExcelFilter<AttributeDescriptorModel>> filters1;
	private Collection<ExcelFilter<ATTRIBUTE>> filters2;

	@Override
	public Collection<ATTRIBUTE> apply(final INPUT input)
	{
		final Collection<AttributeDescriptorModel> result1 = mapper1.apply(input).stream().filter(this::filter1)
				.collect(Collectors.toList());
		return mapper2.apply(result1).stream().filter(this::filter2).collect(Collectors.toList());
	}

	protected boolean filter1(final AttributeDescriptorModel attributeDescriptor)
	{
		return filter(filters1, attributeDescriptor);
	}

	protected boolean filter2(final ATTRIBUTE excelAttribute)
	{
		return filter(filters2, excelAttribute);
	}

	private static <T> boolean filter(final Collection<ExcelFilter<T>> filters, final T t)
	{
		return CollectionUtils.emptyIfNull(filters).stream().allMatch(filter -> filter.test(t));
	}

	@Required
	public void setMapper1(final ExcelMapper<INPUT, AttributeDescriptorModel> mapper1)
	{
		this.mapper1 = mapper1;
	}

	@Required
	public void setMapper2(final ExcelMapper<Collection<AttributeDescriptorModel>, ATTRIBUTE> mapper2)
	{
		this.mapper2 = mapper2;
	}

	// optional
	public void setFilters1(final Collection<ExcelFilter<AttributeDescriptorModel>> filters1)
	{
		this.filters1 = filters1;
	}

	// optional
	public void setFilters2(final Collection<ExcelFilter<ATTRIBUTE>> filters2)
	{
		this.filters2 = filters2;
	}
}
