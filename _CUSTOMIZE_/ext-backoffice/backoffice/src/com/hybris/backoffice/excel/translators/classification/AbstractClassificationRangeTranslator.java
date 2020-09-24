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

import de.hybris.platform.catalog.model.classification.ClassificationAttributeUnitModel;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;
import com.hybris.backoffice.excel.importing.parser.RangeParserUtils;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


public abstract class AbstractClassificationRangeTranslator extends AbstractClassificationAttributeTranslator
{

	@Override
	protected @Nonnull Stream<String> getStreamOfValuesToJoin(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final Feature feature)
	{
		final List<FeatureValue> featureValues = new ArrayList<>(getFeatureValues(excelAttribute, feature));

		return excelAttribute.getAttributeAssignment().getRange() //
				? Stream.of(exportRange(excelAttribute, getPartitionedData(featureValues)).orElse(StringUtils.EMPTY)) //
				: featureValues//
						.stream()//
						.map(featureValue -> exportWithUnit(excelAttribute, featureValue))//
						.filter(Optional::isPresent)//
						.map(Optional::get);
	}

	public Optional<String> exportRange(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final Collection<Pair<FeatureValue, FeatureValue>> featureToExport)
	{
		return Optional.of( //
				featureToExport.stream() //
						.map(pair -> Arrays.asList(getSingle(excelAttribute, pair.getLeft()),
								getSingle(excelAttribute, pair.getRight()))) //
						.map( //
								list -> list.stream().collect(Collectors.joining(RangeParserUtils.RANGE_DELIMITER,
										RangeParserUtils.RANGE_PREFIX, RangeParserUtils.RANGE_SUFFIX)) //
						) //
						.collect(Collectors.joining(ExcelTemplateConstants.MULTI_VALUE_DELIMITER)) //
		);
	}

	@Override
	public Impex importData(final ExcelAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		if (excelAttribute instanceof ExcelClassificationAttribute
				&& ((ExcelClassificationAttribute) excelAttribute).getAttributeAssignment().getRange())
		{
			final ExcelClassificationAttribute excelClassificationAttribute = (ExcelClassificationAttribute) excelAttribute;
			final int multiSize = importParameters.getMultiValueParameters().size();

			if (multiSize == 0)
			{
				final String headerValueName = getClassificationAttributeHeaderValueCreator().create(excelClassificationAttribute,
						excelImportContext);
				final Impex impex = new Impex();
				final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
				impexForType.putValue(0, new ImpexHeaderValue.Builder(headerValueName).withLang(importParameters.getIsoCode())
						.withQualifier(excelAttribute.getQualifier()).build(), StringUtils.EMPTY);
				return impex;
			}

			final List<Map<String, String>> paramsOfFrom = importParameters.getMultiValueParameters().subList(0, multiSize / 2);
			final List<Map<String, String>> paramsOfTo = importParameters.getMultiValueParameters().subList(multiSize / 2,
					multiSize);

			final List<String> impexRangeValues = new ArrayList<>();
			ImpexHeaderValue impexHeaderValue = null;
			for (int i = 0; i < paramsOfFrom.size(); i++)
			{
				final Map<String, String> fromParams = paramsOfFrom.get(i);
				final Map<String, String> toParams = paramsOfTo.get(i);

				final ImportParameters fromImportParameters = getFromImportParameters(excelClassificationAttribute, importParameters,
						fromParams);
				final ImportParameters toImportParameters = getToImportParameters(excelClassificationAttribute, importParameters,
						toParams);

				final ImpexValue fromImpexValue = importValue((ExcelClassificationAttribute) excelAttribute, fromImportParameters,
						excelImportContext);

				final ImpexValue toImpexValue = importValue((ExcelClassificationAttribute) excelAttribute, toImportParameters,
						excelImportContext);

				if (fromImpexValue != null && toImpexValue != null)
				{
					impexHeaderValue = fromImpexValue.getHeaderValue();
					final String impexVal = String.format("%s" + ExcelTemplateConstants.MULTI_VALUE_DELIMITER + "%s",
							fromImpexValue.getValue(), toImpexValue.getValue());
					impexRangeValues.add(impexVal);
				}
			}

			final Impex impex = new Impex();

			if (!impexRangeValues.isEmpty())
			{
				final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
				impexForType.putValue(0, impexHeaderValue,
						StringUtils.join(impexRangeValues, ExcelTemplateConstants.MULTI_VALUE_DELIMITER));
			}

			return impex;
		}

		return super.importData(excelAttribute, importParameters, excelImportContext);
	}

	protected ImportParameters getFromImportParameters(final ExcelClassificationAttribute excelClassificationAttribute,
			final ImportParameters importParameters, final Map<String, String> fromParams)
	{
		return RangeParserUtils.getSingleImportParameters(excelClassificationAttribute, importParameters, fromParams,
				RangeParserUtils.RangeBounds.FROM);
	}

	protected ImportParameters getToImportParameters(final ExcelClassificationAttribute excelClassificationAttribute,
			final ImportParameters importParameters, final Map<String, String> toParams)
	{
		return RangeParserUtils.getSingleImportParameters(excelClassificationAttribute, importParameters, toParams,
				RangeParserUtils.RangeBounds.TO);
	}

	@Override
	public @Nonnull String referenceFormat(@Nonnull final ExcelClassificationAttribute excelAttribute)
	{
		if (excelAttribute.getAttributeAssignment().getRange())
		{
			final String singleReferenceFormat = singleReferenceFormat(excelAttribute);
			final ClassificationAttributeUnitModel unit = excelAttribute.getAttributeAssignment().getUnit();

			return StringUtils.isEmpty(singleReferenceFormat) && unit == null ? //
					RangeParserUtils.SIMPLE_TYPE_RANGE_FORMAT //
					: String.format(RangeParserUtils.COMPLEX_TYPE_RANGE_FORMAT, super.referenceFormat(excelAttribute));
		}
		return super.referenceFormat(excelAttribute);
	}

	protected String getSingle(final ExcelClassificationAttribute excelAttribute, final FeatureValue featureValue)
	{
		return exportWithUnit(excelAttribute, featureValue).orElse(StringUtils.EMPTY);
	}

}
