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
package com.hybris.backoffice.excel.translators.classification;

import de.hybris.platform.catalog.enums.ClassificationAttributeTypeEnum;
import de.hybris.platform.classification.features.FeatureValue;

import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;
import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * Translates java types like: numbers, boolean values, strings and dates. Dates are exported in format:
 * {@link ExcelDateUtils#getDateTimeFormat()}. Time zone of the exported date is:
 * {@link ExcelDateUtils#getExportTimeZone()}
 */
public class ExcelClassificationJavaTypeTranslator extends AbstractClassificationRangeTranslator
{
	private static final Set<ClassificationAttributeTypeEnum> SUPPORTED_TYPES = EnumSet.of(//
			ClassificationAttributeTypeEnum.NUMBER, //
			ClassificationAttributeTypeEnum.STRING, //
			ClassificationAttributeTypeEnum.BOOLEAN, //
			ClassificationAttributeTypeEnum.DATE);

	private ExcelDateUtils excelDateUtils;
	private int order;

	@Override
	public boolean canHandleUnit(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return excelClassificationAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.NUMBER;
	}

	@Override
	public boolean canHandleRange(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return excelClassificationAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.NUMBER
				|| excelClassificationAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.DATE;
	}

	@Override
	public boolean canHandleAttribute(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return SUPPORTED_TYPES.contains(excelClassificationAttribute.getAttributeAssignment().getAttributeType());
	}

	@Override
	public Optional<String> exportSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final FeatureValue featureToExport)
	{
		final Object value = featureToExport.getValue();
		if (value instanceof Date)
		{
			return Optional.ofNullable(excelDateUtils.exportDate((Date) value));
		}

		return Optional.ofNullable(value.toString());
	}

	@Override
	public ImpexValue importSingle(final @Nonnull ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters, final @Nonnull ExcelImportContext excelImportContext)
	{
		if (ClassificationAttributeTypeEnum.DATE.equals(excelAttribute.getAttributeAssignment().getAttributeType()))
		{
			return importDate(excelAttribute, importParameters, excelImportContext);
		}
		return importSimple(excelAttribute, importParameters, excelImportContext);
	}

	protected ImpexValue importDate(final ExcelClassificationAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		final String headerValueName = getClassificationAttributeHeaderValueCreator().create(excelAttribute, excelImportContext);
		final String dateToImport = excelDateUtils.importDate(String.valueOf(importParameters.getCellValue()));
		return new ImpexValue(dateToImport, new ImpexHeaderValue.Builder(headerValueName).withLang(importParameters.getIsoCode())
				.withDateFormat(excelDateUtils.getDateTimeFormat()).withQualifier(excelAttribute.getQualifier()).build());
	}

	protected ImpexValue importSimple(final ExcelClassificationAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		final String headerValueName = getClassificationAttributeHeaderValueCreator().create(excelAttribute, excelImportContext);
		return new ImpexValue(importParameters.getCellValue(), new ImpexHeaderValue.Builder(headerValueName)
				.withLang(importParameters.getIsoCode()).withQualifier(excelAttribute.getQualifier()).build());
	}

	@Override
	public @Nonnull String singleReferenceFormat(@Nonnull final ExcelClassificationAttribute excelAttribute)
	{
		if (ClassificationAttributeTypeEnum.DATE.getCode()
				.equals(excelAttribute.getAttributeAssignment().getAttributeType().getCode()))
		{
			return excelDateUtils.getDateTimeFormat();
		}
		return StringUtils.EMPTY;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
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

}
