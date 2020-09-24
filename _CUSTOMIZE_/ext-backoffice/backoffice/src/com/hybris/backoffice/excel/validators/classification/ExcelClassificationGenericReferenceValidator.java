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
package com.hybris.backoffice.excel.validators.classification;

import de.hybris.platform.catalog.enums.ClassificationAttributeTypeEnum;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;
import com.hybris.backoffice.excel.translators.generic.factory.RequiredAttributesFactory;
import com.hybris.backoffice.excel.validators.ExcelGenericReferenceValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


public class ExcelClassificationGenericReferenceValidator extends AbstractSingleClassificationFieldValidator
{
	private ExcelGenericReferenceValidator excelGenericReferenceValidator;
	private RequiredAttributesFactory requiredAttributesFactory;

	@Override
	public boolean canHandleSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return ClassificationAttributeTypeEnum.REFERENCE == excelAttribute.getAttributeAssignment().getAttributeType();
	}

	@Override
	public ExcelValidationResult validate(final @Nonnull ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters, final @Nonnull Map<String, Object> context)
	{
		final ComposedTypeModel referenceType = excelAttribute.getAttributeAssignment().getReferenceType();
		final RequiredAttribute requiredAttribute = requiredAttributesFactory.create(referenceType);
		return excelGenericReferenceValidator.validateRequiredAttribute(requiredAttribute, importParameters, context);
	}

	@Required
	public void setExcelGenericReferenceValidator(final ExcelGenericReferenceValidator excelGenericReferenceValidator)
	{
		this.excelGenericReferenceValidator = excelGenericReferenceValidator;
	}

	@Required
	public void setRequiredAttributesFactory(final RequiredAttributesFactory requiredAttributesFactory)
	{
		this.requiredAttributesFactory = requiredAttributesFactory;
	}
}
