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
import de.hybris.platform.catalog.model.classification.ClassificationAttributeValueModel;
import de.hybris.platform.classification.features.FeatureValue;
import de.hybris.platform.core.HybrisEnumValue;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;


public class ExcelClassificationEnumTypeTranslator extends AbstractClassificationAttributeTranslator
{
	private int order;

	@Override
	public boolean canHandleAttribute(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return excelClassificationAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.ENUM;
	}

	@Override
	public Optional<String> exportSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final FeatureValue featureToExport)
	{
		final Object featureValue = featureToExport.getValue();
		return Stream.of(extractHybrisEnumValueCode(featureValue), extractClassificationAttributeValueCode(featureValue)) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.findFirst();
	}

	private static Optional<String> extractHybrisEnumValueCode(final Object value)
	{
		return Optional.ofNullable(value) //
				.filter(HybrisEnumValue.class::isInstance) //
				.map(HybrisEnumValue.class::cast) //
				.map(HybrisEnumValue::getCode);
	}

	private static Optional<String> extractClassificationAttributeValueCode(final Object value)
	{
		return Optional.ofNullable(value) //
				.filter(ClassificationAttributeValueModel.class::isInstance) //
				.map(ClassificationAttributeValueModel.class::cast) //
				.map(ClassificationAttributeValueModel::getCode);
	}

	@Override
	public ImpexValue importSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, final @Nonnull ExcelImportContext excelImportContext)
	{
		final String headerName = getClassificationAttributeHeaderValueCreator().create(excelAttribute, excelImportContext);
		return new ImpexValue(importParameters.getCellValue(), new ImpexHeaderValue.Builder(headerName)
				.withLang(importParameters.getIsoCode()).withQualifier(excelAttribute.getQualifier()).build());
	}

	@Override
	public @Nonnull String singleReferenceFormat(@Nonnull final ExcelClassificationAttribute excelAttribute)
	{
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

}
