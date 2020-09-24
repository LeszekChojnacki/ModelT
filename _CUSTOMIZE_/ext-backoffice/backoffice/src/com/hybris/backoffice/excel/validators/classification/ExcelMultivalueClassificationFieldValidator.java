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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.util.ExcelValidationResultUtil;


/**
 * Allows to handle multivalue types. It splits the values and delegates to not multivalued validators.
 */
public class ExcelMultivalueClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	private List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators = Collections.emptyList();

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return importParameters.isCellValueNotBlank() && ExcelValidatorUtils.isMultivalue(importParameters)
				&& excelAttribute.getAttributeAssignment().getMultiValued();
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		final Collection<ImportParameters> importParametersCollection = excelAttribute.getAttributeAssignment().getRange()
				? splitImportParametersForRange(importParameters)
				: splitImportParametersForSingle(importParameters);

		final Collection<ExcelValidationResult> excelValidationResults = new ArrayList<>();
		for (final ImportParameters singleImportParam : importParametersCollection)
		{
			excelValidationResults.addAll( //
					validators.stream().filter(validator -> validator.canHandle(excelAttribute, singleImportParam))
							.map(validator -> validator.validate(excelAttribute, singleImportParam, context))
							.collect(Collectors.toList()) //
			);
		}
		return ExcelValidationResultUtil.mergeExcelValidationResults(excelValidationResults);
	}

	private Collection<ImportParameters> splitImportParametersForRange(final ImportParameters importParameters)
	{
		return getCollectionOfImportParameters(importParameters, (idx, cellValuesLength) -> {
			if (importParameters.getMultiValueParameters().size() <= 1)
			{
				return Collections.emptyList();
			}
			final List<List<Map<String, String>>> partitionedList = ListUtils.partition(importParameters.getMultiValueParameters(),
					cellValuesLength);
			final List<Map<String, String>> from = partitionedList.get(0);
			final List<Map<String, String>> to = partitionedList.get(1);
			final List<Map<String, String>> mergedParams = new ArrayList<>();
			mergedParams.add(from.get(idx));
			mergedParams.add(to.get(idx));
			return mergedParams;
		});
	}

	private Collection<ImportParameters> splitImportParametersForSingle(final ImportParameters importParameters)
	{
		return getCollectionOfImportParameters(importParameters,
				(idx, cellValuesLength) -> Lists.newArrayList(importParameters.getMultiValueParameters().get(idx)));
	}

	private static Collection<ImportParameters> getCollectionOfImportParameters(final ImportParameters importParameters,
			final BiFunction<Integer, Integer, List<Map<String, String>>> paramsMapper)
	{
		final String[] cellValues = StringUtils.splitPreserveAllTokens(String.valueOf(importParameters.getCellValue()),
				ImportParameters.MULTIVALUE_SEPARATOR);

		return IntStream.range(0, cellValues.length) //
				.mapToObj( //
						idx -> new ImportParameters(importParameters.getTypeCode(), importParameters.getIsoCode(), cellValues[idx],
								importParameters.getEntryRef(), paramsMapper.apply(idx, cellValues.length)) //
				).collect(Collectors.toList());
	}

	// optional
	public void setValidators(final List<ExcelAttributeValidator<ExcelClassificationAttribute>> validators)
	{
		this.validators = validators;
	}
}
