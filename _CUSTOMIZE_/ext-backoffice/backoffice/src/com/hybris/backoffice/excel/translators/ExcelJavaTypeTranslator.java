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

import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.MapTypeModel;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * Translates java types like: numbers, boolean values and dates. Dates are exported in format:
 * {@link ExcelDateUtils#getDateTimeFormat()}. Dates are exported in {@link ExcelDateUtils#getExportTimeZone()} time
 * zone. During import date is treated as in export time zone so it's converted back into system time zone (default is
 * UTC).
 */
public class ExcelJavaTypeTranslator extends AbstractExcelValueTranslator<Object>
{

	private ExcelDateUtils excelDateUtils;
	private ExcelFilter<AttributeDescriptorModel> excelUniqueFilter;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptorModel)
	{
		final boolean isAtomicType = attributeDescriptorModel.getAttributeType() instanceof AtomicTypeModel;
		final boolean isLocalizedAtomicField = attributeDescriptorModel.getLocalized()
				&& attributeDescriptorModel.getAttributeType() instanceof MapTypeModel
				&& ((MapTypeModel) attributeDescriptorModel.getAttributeType()).getReturntype() instanceof AtomicTypeModel;
		final boolean isPk = "pk".equals(attributeDescriptorModel.getQualifier());
		return !isPk && (isAtomicType || isLocalizedAtomicField);
	}

	@Override
	public Optional<Object> exportData(final Object objectToExport)
	{
		if (objectToExport instanceof Date)
		{
			return Optional.of(excelDateUtils.exportDate((Date) objectToExport));
		}

		return Optional.ofNullable(objectToExport);
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		if (Date.class.getCanonicalName().equals(attributeDescriptor.getAttributeType().getCode()))
		{
			return importDate(attributeDescriptor, importParameters);
		}
		return new ImpexValue(importParameters.getCellValue(), new ImpexHeaderValue.Builder(attributeDescriptor.getQualifier())
				.withUnique(excelUniqueFilter.test(attributeDescriptor)).withMandatory(getMandatoryFilter().test(attributeDescriptor))
				.withLang(importParameters.getIsoCode()).withQualifier(attributeDescriptor.getQualifier()).build());
	}

	protected ImpexValue importDate(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final String dateToImport = importParameters.getCellValue() != null
				? excelDateUtils.importDate(importParameters.getCellValue().toString()) : null;
		return new ImpexValue(dateToImport,
				new ImpexHeaderValue.Builder(attributeDescriptor.getQualifier())
						.withUnique(excelUniqueFilter.test(attributeDescriptor))
						.withMandatory(getMandatoryFilter().test(attributeDescriptor)).withLang(importParameters.getIsoCode())
						.withDateFormat(excelDateUtils.getDateTimeFormat()).withQualifier(attributeDescriptor.getQualifier()).build());
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return Date.class.getCanonicalName().equals(attributeDescriptor.getAttributeType().getCode())
				? excelDateUtils.getDateTimeFormat() : StringUtils.EMPTY;
	}

	public ExcelDateUtils getExcelDateUtils()
	{
		return excelDateUtils;
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}

	public ExcelFilter<AttributeDescriptorModel> getExcelUniqueFilter()
	{
		return excelUniqueFilter;
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
	public void setMandatoryFilter(final ExcelFilter<AttributeDescriptorModel> mandatoryFilter)
	{
		this.mandatoryFilter = mandatoryFilter;
	}
}
