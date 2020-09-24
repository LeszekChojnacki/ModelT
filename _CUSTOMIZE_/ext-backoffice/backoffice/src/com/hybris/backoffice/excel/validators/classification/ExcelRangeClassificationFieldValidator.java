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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.parser.RangeParserUtils;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;
import com.hybris.backoffice.excel.validators.util.ExcelValidationResultUtil;


/**
 * Allows to handle range types. It splits the value and delegates to not ranged validators.
 */
public class ExcelRangeClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	private static final String EXCEL_IMPORT_VALIDATION_RANGE = "excel.import.validation.range";

	private List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators = Collections.emptyList();

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return isRangeNotBlank(importParameters) && ExcelValidatorUtils.isNotMultivalue(importParameters)
				&& excelAttribute.getAttributeAssignment().getRange();
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		final Map<String, String> singleValueOfFrom = importParameters.getMultiValueParameters().get(0);
		final Map<String, String> singleValueOfTo = importParameters.getMultiValueParameters().get(1);

		final ImportParameters from = RangeParserUtils.getSingleImportParameters(excelAttribute, importParameters,
				singleValueOfFrom, RangeParserUtils.RangeBounds.FROM);

		final ImportParameters to = RangeParserUtils.getSingleImportParameters(excelAttribute, importParameters, singleValueOfTo,
				RangeParserUtils.RangeBounds.TO);

		final Predicate<ImportParameters> rawValueIsBlank = imp -> StringUtils
				.isBlank(imp.getSingleValueParameters().get(ImportParameters.RAW_VALUE));

		if (rawValueIsBlank.test(from) || rawValueIsBlank.test(to))
		{
			return new ExcelValidationResult(new ValidationMessage(EXCEL_IMPORT_VALIDATION_RANGE, importParameters.getCellValue()));
		}

		final List<ExcelValidationResult> fromResults = validators.stream()
				.filter(validator -> validator.canHandle(excelAttribute, from))
				.map(validator -> validator.validate(excelAttribute, from, context)).collect(Collectors.toList());

		final List<ExcelValidationResult> toResults = validators.stream()
				.filter(validator -> validator.canHandle(excelAttribute, to))
				.map(validator -> validator.validate(excelAttribute, to, context)).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(ExcelValidationResultUtil.mapExcelResultsToValidationMessages(fromResults))
				|| CollectionUtils.isNotEmpty(ExcelValidationResultUtil.mapExcelResultsToValidationMessages(toResults)))
		{
			return mergeResults(fromResults, toResults);
		}

		return ExcelValidationResult.SUCCESS;
	}

	protected ExcelValidationResult mergeResults(final Collection<ExcelValidationResult> fromResults,
			final Collection<ExcelValidationResult> toResults)
	{
		return ExcelValidationResultUtil.mergeExcelValidationResults(CollectionUtils.union(fromResults, toResults));
	}

	protected boolean isRangeNotBlank(final ImportParameters importParameters)
	{
		return importParameters.isCellValueNotBlank() && importParameters.getMultiValueParameters().size() > 1;
	}

	// optional
	public void setValidators(final List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators)
	{
		this.validators = validators;
	}
}
