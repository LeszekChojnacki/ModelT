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

import de.hybris.platform.catalog.model.classification.ClassificationAttributeUnitModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.classification.ClassificationSystemService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


public class ExcelUnitClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	public static final String INVALID_UNIT_MESSAGE_KEY = "excel.import.validation.unit.invalid";
	private static final String CACHE_KEY_PATTERN = "PossibleUnitsOf:%s:%s:%s";

	private ClassificationSystemService classificationSystemService;
	private List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators = Collections.emptyList();

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return ExcelValidatorUtils.hasUnit(importParameters) && !ExcelValidatorUtils.isMultivalue(importParameters)
				&& ExcelValidatorUtils.isNotRange(importParameters);
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{

		final LinkedList<ValidationMessage> validationErrors = new LinkedList<>();
		final List<String> allPossibleUnits = getAllPossibleUnitsForType(context, excelAttribute);

		final Map<String, String> singleValueParams = importParameters.getSingleValueParameters();
		final String unit = ExcelUnitUtils.extractUnitFromParams(singleValueParams);
		final String value = ExcelUnitUtils.extractValueFromParams(singleValueParams);
		final ImportParameters importParametersForValue = ExcelUnitUtils.getImportParametersForValue(importParameters, value);
		final List<ValidationMessage> validationMessages = executeValidators(excelAttribute, importParametersForValue, context);
		validationErrors.addAll(validationMessages);

		if (StringUtils.isNotBlank(value) && !allPossibleUnits.contains(unit))
		{
			validationErrors.add(new ValidationMessage(INVALID_UNIT_MESSAGE_KEY, unit,
					excelAttribute.getAttributeAssignment().getUnit().getUnitType()));
		}
		return new ExcelValidationResult(validationErrors);
	}

	private List<String> getAllPossibleUnitsForType(final Map<String, Object> context,
			final ExcelClassificationAttribute excelAttribute)
	{
		final ClassificationSystemVersionModel systemVersion = excelAttribute.getAttributeAssignment().getSystemVersion();
		final String unitType = excelAttribute.getAttributeAssignment().getUnit().getUnitType();
		final String cacheKey = createUnitsCacheKey(systemVersion, unitType);

		if (!context.containsKey(cacheKey))
		{
			final List<String> units = loadUnitsForTypeOf(systemVersion, unitType);
			context.put(cacheKey, units);
			return units;
		}
		return (List<String>) context.get(cacheKey);
	}

	private static String createUnitsCacheKey(final ClassificationSystemVersionModel systemVersion, final String unitType)
	{
		return String.format(CACHE_KEY_PATTERN, systemVersion.getCatalog().getId(), systemVersion.getVersion(), unitType);
	}

	private List<String> loadUnitsForTypeOf(final ClassificationSystemVersionModel systemVersion, final String unitType)
	{
		return classificationSystemService.getUnitsOfTypeForSystemVersion(systemVersion, unitType) //
				.stream() //
				.map(ClassificationAttributeUnitModel::getCode) //
				.collect(Collectors.toList());
	}

	private List<ValidationMessage> executeValidators(final ExcelClassificationAttribute excelAttribute,
			final ImportParameters importParameters, final Map<String, Object> context)
	{
		return validators.stream() //
				.filter(validator -> validator.canHandle(excelAttribute, importParameters)) //
				.map(validator -> validator.validate(excelAttribute, importParameters, context)) //
				.map(ExcelValidationResult::getValidationErrors) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toList());
	}

	@Required
	public void setClassificationSystemService(final ClassificationSystemService classificationSystemService)
	{
		this.classificationSystemService = classificationSystemService;
	}

	// optional
	public void setValidators(final List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators)
	{
		this.validators = validators;
	}
}
