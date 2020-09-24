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
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Allows to map {@link String} to Collection<{@link AttributeDescriptorModel}>
 */
public class FromTypeCodeToAttributeDescriptorsMapper implements ToAttributeDescriptorsMapper<String>
{

	private ExcelMapper<ComposedTypeModel, AttributeDescriptorModel> mapper;
	private TypeService typeService;
	private Collection<ExcelFilter<AttributeDescriptorModel>> filters;

	@Override
	public Collection<AttributeDescriptorModel> apply(final String s)
	{
		return mapper.apply(typeService.getComposedTypeForCode(s)) //
				.stream() //
				.filter(attribute -> filter(attribute, filters)) //
				.collect(Collectors.toList());
	}

	@Required
	public void setMapper(final ExcelMapper<ComposedTypeModel, AttributeDescriptorModel> mapper)
	{
		this.mapper = mapper;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	// optional
	public void setFilters(final Collection<ExcelFilter<AttributeDescriptorModel>> filters)
	{
		this.filters = filters;
	}
}
