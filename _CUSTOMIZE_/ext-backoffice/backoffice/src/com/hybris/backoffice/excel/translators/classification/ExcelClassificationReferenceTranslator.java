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
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;
import com.hybris.backoffice.excel.translators.generic.factory.ExportDataFactory;
import com.hybris.backoffice.excel.translators.generic.factory.ReferenceFormatFactory;
import com.hybris.backoffice.excel.translators.generic.factory.RequiredAttributesFactory;


public class ExcelClassificationReferenceTranslator extends AbstractClassificationAttributeTranslator
{
	private int order = Ordered.LOWEST_PRECEDENCE - 100;

	private ExportDataFactory exportDataFactory;
	private ReferenceFormatFactory referenceFormatFactory;
	private RequiredAttributesFactory requiredAttributesFactory;
	private ClassificationAttributeHeaderValueCreator headerValueCreator;
	private ExcelFilter<AttributeDescriptorModel> filter;

	@Override
	public Optional<String> exportSingle(final @Nonnull ExcelClassificationAttribute excelAttribute,
			@Nonnull final FeatureValue featureToExport)
	{
		final RequiredAttribute requiredAttribute = getRequiredAttribute(excelAttribute);
		return exportDataFactory.create(requiredAttribute, featureToExport.getValue());
	}

	@Override
	public @Nonnull String singleReferenceFormat(final @Nonnull ExcelClassificationAttribute excelAttribute)
	{
		final RequiredAttribute requiredAttribute = getRequiredAttribute(excelAttribute);
		return referenceFormatFactory.create(requiredAttribute);
	}

	private RequiredAttribute getRequiredAttribute(final ExcelClassificationAttribute excelAttribute)
	{
		final ComposedTypeModel referenceType = excelAttribute.getAttributeAssignment().getReferenceType();
		return requiredAttributesFactory.create(referenceType);
	}

	@Override
	protected @Nullable ImpexValue importSingle(final @Nonnull ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters, final @Nonnull ExcelImportContext excelImportContext)
	{
		final String headerValueName = headerValueCreator.create(excelAttribute, excelImportContext);
		final String value = importParameters.getSingleValueParameters().get(ImportParameters.RAW_VALUE);
		return new ImpexValue(value, new ImpexHeaderValue.Builder(headerValueName).withLang(importParameters.getIsoCode())
				.withQualifier(excelAttribute.getQualifier()).build());
	}

	@Override
	public boolean canHandleAttribute(final @Nonnull ExcelClassificationAttribute excelAttribute)
	{
		return ClassificationAttributeTypeEnum.REFERENCE == excelAttribute.getAttributeAssignment().getAttributeType()
				&& hasAtLeastOneUniqueAttribute(excelAttribute.getAttributeAssignment().getReferenceType());
	}

	protected boolean hasAtLeastOneUniqueAttribute(final ComposedTypeModel composedTypeModel)
	{
		if (composedTypeModel == null)
		{
			return false;
		}
		final List<AttributeDescriptorModel> allAttributes = new ArrayList<>();
		allAttributes.addAll(composedTypeModel.getDeclaredattributedescriptors());
		allAttributes.addAll(composedTypeModel.getInheritedattributedescriptors());
		return allAttributes.stream().anyMatch(filter);
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	// optional
	public void setOrder(final int order)
	{
		this.order = order;
	}

	@Required
	public void setExportDataFactory(final ExportDataFactory exportDataFactory)
	{
		this.exportDataFactory = exportDataFactory;
	}

	@Required
	public void setReferenceFormatFactory(final ReferenceFormatFactory referenceFormatFactory)
	{
		this.referenceFormatFactory = referenceFormatFactory;
	}

	@Required
	public void setRequiredAttributesFactory(final RequiredAttributesFactory requiredAttributesFactory)
	{
		this.requiredAttributesFactory = requiredAttributesFactory;
	}

	@Required
	public void setHeaderValueCreator(final ClassificationAttributeHeaderValueCreator headerValueCreator)
	{
		this.headerValueCreator = headerValueCreator;
	}

	public ExcelFilter<AttributeDescriptorModel> getFilter()
	{
		return filter;
	}

	@Required
	public void setFilter(final ExcelFilter<AttributeDescriptorModel> filter)
	{
		this.filter = filter;
	}
}
