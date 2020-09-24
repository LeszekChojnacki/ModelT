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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Default excel translator for enum type.
 */
public class ExcelEnumTypeTranslator extends AbstractExcelValueTranslator<HybrisEnumValue>
{

	private ExcelFilter<AttributeDescriptorModel> excelUniqueFilter;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return attributeDescriptorModel.getAttributeType() instanceof EnumerationMetaTypeModel;
	}

	@Override
	public Optional<Object> exportData(final HybrisEnumValue enumToExport)
	{
		return Optional.ofNullable(enumToExport).map(HybrisEnumValue::getCode);
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		return new ImpexValue(importParameters.getCellValue(),
				new ImpexHeaderValue.Builder(
						String.format("%s(%s)", attributeDescriptor.getQualifier(), EnumerationMetaTypeModel.CODE))
								.withUnique(excelUniqueFilter.test(attributeDescriptor))
								.withMandatory(mandatoryFilter.test(attributeDescriptor)).withLang(importParameters.getIsoCode())
								.withQualifier(attributeDescriptor.getQualifier()).build());
	}

	@Required
	public void setExcelUniqueFilter(final ExcelFilter<AttributeDescriptorModel> excelUniqueFilter)
	{
		this.excelUniqueFilter = excelUniqueFilter;
	}

	public ExcelFilter<AttributeDescriptorModel> getMandatoryFilter()
	{
		return mandatoryFilter;
	}

	@Required
	public void setMandatoryFilter(ExcelFilter<AttributeDescriptorModel> mandatoryFilter)
	{
		this.mandatoryFilter = mandatoryFilter;
	}
}
