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

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeUnitModel;
import de.hybris.platform.classification.ClassificationService;
import de.hybris.platform.classification.ClassificationSystemService;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureValue;
import de.hybris.platform.classification.features.LocalizedFeature;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportContext;
import com.hybris.backoffice.excel.importing.parser.splitter.ExcelParserSplitter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslator;


public abstract class AbstractClassificationAttributeTranslator implements ExcelAttributeTranslator<ExcelClassificationAttribute>
{

	protected static final String SIMPLE_TYPE_WITH_UNITS_REFERENCE_FORMAT = "value:unit[%s]";
	protected static final String COMPLEX_TYPE_WITH_UNITS_REFERENCE_FORMAT = "%s:unit[%s]";
	protected static final String VALUE_WITH_UNITS_FORMAT = "%s:%s";

	private ClassificationService classificationService;
	private ClassificationSystemService classificationSystemService;
	private CommonI18NService commonI18NService;
	private ExcelParserSplitter excelParserSplitter;
	private ClassificationAttributeHeaderValueCreator classificationAttributeHeaderValueCreator;

	public abstract Optional<String> exportSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final FeatureValue featureToExport);

	public abstract @Nonnull String singleReferenceFormat(@Nonnull final ExcelClassificationAttribute excelAttribute);

	public boolean canHandleUnit(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return false;
	}

	public boolean canHandleRange(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute)
	{
		return false;
	}

	public abstract boolean canHandleAttribute(@Nonnull final ExcelClassificationAttribute excelClassificationAttribute);

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute)
	{
		if (excelAttribute.getAttributeAssignment().getUnit() != null && !canHandleUnit(excelAttribute))
		{
			return false;
		}
		if (excelAttribute.getAttributeAssignment().getRange() && !canHandleRange(excelAttribute))
		{
			return false;
		}
		return canHandleAttribute(excelAttribute);
	}

	@Override
	public Optional<String> exportData(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final Object objectToExport)
	{
		if (objectToExport instanceof ProductModel)
		{
			final Feature feature = getClassificationService().getFeature((ProductModel) objectToExport,
					excelAttribute.getAttributeAssignment());

			return Optional.ofNullable(//
					getStreamOfValuesToJoin(excelAttribute, feature)//
							.collect(Collectors.joining(ExcelTemplateConstants.MULTI_VALUE_DELIMITER)) //
			);
		}
		return Optional.empty();
	}

	protected @Nonnull Stream<String> getStreamOfValuesToJoin(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final Feature feature)
	{
		final List<FeatureValue> featureValues = new ArrayList<>(getFeatureValues(excelAttribute, feature));

		return featureValues//
				.stream()//
				.map(featureValue -> exportWithUnit(excelAttribute, featureValue))//
				.filter(Optional::isPresent)//
				.map(Optional::get);
	}

	protected Optional<String> exportWithUnit(final ExcelClassificationAttribute excelAttribute, final FeatureValue featureValue)
	{
		final ClassificationAttributeUnitModel unit = featureValue.getUnit();
		final Optional<String> exportedValue = exportSingle(excelAttribute, featureValue);
		if (exportedValue.isPresent() && unit != null)
		{
			return Optional.of(String.format(VALUE_WITH_UNITS_FORMAT, exportedValue.get(), unit.getCode()));
		}
		return exportedValue;
	}

	@Override
	public Impex importData(final ExcelAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		final Impex impex = new Impex();
		if (excelAttribute instanceof ExcelClassificationAttribute)
		{
			final ExcelClassificationAttribute excelClassificationAttribute = (ExcelClassificationAttribute) excelAttribute;

			final BiConsumer<ImpexHeaderValue, Object> insertValToImpex = (headerValue, impexValue) -> {
				final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
				impexForType.putValue(0, headerValue, impexValue);
			};

			if (excelClassificationAttribute.getAttributeAssignment().getMultiValued())
			{
				final Collection<ImportParameters> multiImportParameters = splitImportParametersOfMultivalue(importParameters);
				final List<ImpexValue> impexValues = multiImportParameters.stream() //
						.map(impParam -> importValue(excelClassificationAttribute, impParam, excelImportContext)) //
						.collect(Collectors.toList());
				final String valueToInsert = impexValues.stream().filter(Objects::nonNull) //
						.map(ImpexValue::getValue) //
						.map(String::valueOf) //
						.collect(Collectors.joining(ExcelTemplateConstants.MULTI_VALUE_DELIMITER));
				if (!impexValues.isEmpty())
				{
					insertValToImpex.accept(impexValues.get(0).getHeaderValue(), valueToInsert);
				}
			}
			else
			{
				final ImpexValue impexValue = importValue(excelClassificationAttribute, importParameters, excelImportContext);
				if (impexValue != null)
				{
					insertValToImpex.accept(impexValue.getHeaderValue(), impexValue.getValue());
				}
			}
		}
		return impex;
	}

	private Collection<ImportParameters> splitImportParametersOfMultivalue(final ImportParameters importParameters)
	{
		final String cellValue = String.valueOf(importParameters.getCellValue());
		if (!StringUtils.contains(cellValue, ExcelTemplateConstants.MULTI_VALUE_DELIMITER))
		{
			return Lists.newArrayList(importParameters);
		}
		final String[] multiCellValue = StringUtils.split(cellValue, ExcelTemplateConstants.MULTI_VALUE_DELIMITER);
		final List<ImportParameters> splitImportParameters = new ArrayList<>();
		for (int i = 0; i < multiCellValue.length; i++)
		{
			final String singleCellValue = multiCellValue[i];
			final Map<String, String> singleParams = importParameters.getMultiValueParameters().get(i);
			final ImportParameters singleImportParameters = new ImportParameters(importParameters.getTypeCode(),
					importParameters.getIsoCode(), singleCellValue, importParameters.getEntryRef(), Lists.newArrayList(singleParams));
			splitImportParameters.add(singleImportParameters);
		}
		return splitImportParameters;
	}

	protected ImpexValue importValue(final ExcelClassificationAttribute excelAttribute, final ImportParameters importParameters,
			final ExcelImportContext excelImportContext)
	{
		if (excelAttribute.getAttributeAssignment().getUnit() != null)
		{
			final Pair<ImportParameters, String> pair = splitToImportParametersWithoutUnitAndUnit(importParameters);
			if (pair != null)
			{
				final ImportParameters importParametersWithoutUnit = pair.getLeft();
				final String unit = pair.getRight();
				final ImpexValue impexValueWithoutUnit = importSingle(excelAttribute, importParametersWithoutUnit,
						excelImportContext);
				if (impexValueWithoutUnit != null)
				{
					final String impexValueWithUnit = impexValueWithoutUnit.getValue()
							+ ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR + unit;
					return new ImpexValue(impexValueWithUnit, impexValueWithoutUnit.getHeaderValue());
				}
			}
		}

		return importSingle(excelAttribute, importParameters, excelImportContext);
	}

	private Pair<ImportParameters, String> splitToImportParametersWithoutUnitAndUnit(final ImportParameters importParameters)
	{
		final String[] cellValues = excelParserSplitter.apply(String.valueOf(importParameters.getCellValue()));
		if (cellValues.length != 2)
		{
			return null;
		}
		final String[] rawValues = excelParserSplitter
				.apply(importParameters.getSingleValueParameters().get(ImportParameters.RAW_VALUE));
		final Map<String, String> paramsWithoutUnit = new LinkedHashMap<>();
		paramsWithoutUnit.putAll(importParameters.getSingleValueParameters());
		paramsWithoutUnit.replace(ImportParameters.RAW_VALUE, rawValues[0]);
		final ImportParameters importParametersWithoutUnit = new ImportParameters(importParameters.getTypeCode(),
				importParameters.getIsoCode(), cellValues[0], importParameters.getEntryRef(), Lists.newArrayList(paramsWithoutUnit));
		return ImmutablePair.of(importParametersWithoutUnit, rawValues[1]);
	}

	protected abstract @Nullable ImpexValue importSingle(@Nonnull ExcelClassificationAttribute excelAttribute,
			@Nonnull ImportParameters importParameters, @Nonnull ExcelImportContext excelImportContext);



	protected @Nonnull Collection<FeatureValue> getFeatureValues(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final Feature feature)
	{
		final Collection<FeatureValue> values = feature.getClassAttributeAssignment().getLocalized()
				? getLocalizedFeatureValues(excelAttribute, (LocalizedFeature) feature)
				: getUnlocalizedFeatureValues(excelAttribute, feature);
		return values.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	protected @Nonnull Collection<FeatureValue> getUnlocalizedFeatureValues(
			@Nonnull final ExcelClassificationAttribute excelAttribute, @Nonnull final Feature feature)
	{
		final ClassAttributeAssignmentModel classAttributeAssignment = feature.getClassAttributeAssignment();
		return CollectionUtils.emptyIfNull(//
				classAttributeAssignment.getMultiValued() || classAttributeAssignment.getRange() ? //
						feature.getValues() : Lists.newArrayList(feature.getValue())//
		);
	}

	protected @Nonnull Collection<FeatureValue> getLocalizedFeatureValues(
			@Nonnull final ExcelClassificationAttribute excelAttribute, @Nonnull final LocalizedFeature feature)
	{
		final Locale locale = getCommonI18NService().getLocaleForIsoCode(excelAttribute.getIsoCode());
		final ClassAttributeAssignmentModel attributeAssignment = excelAttribute.getAttributeAssignment();
		return CollectionUtils.emptyIfNull(//
				attributeAssignment.getMultiValued() || attributeAssignment.getRange() ? //
						feature.getValues(locale) : Lists.newArrayList(feature.getValue(locale))//
		);
	}

	protected Collection<Pair<FeatureValue, FeatureValue>> getPartitionedData(final List<FeatureValue> featureValues)
	{
		if (featureValues.isEmpty())
		{
			return Collections.emptyList();
		}
		final Function<List<FeatureValue>, FeatureValue> getOrNull = list -> list.size() > 1 ? list.get(1)
				: new FeatureValue(StringUtils.EMPTY);
		return ListUtils.partition(featureValues, 2) //
				.stream() //
				.map(list -> ImmutablePair.of(list.get(0), getOrNull.apply(list))) //
				.collect(Collectors.toList());
	}

	@Override
	public @Nonnull String referenceFormat(@Nonnull final ExcelClassificationAttribute excelAttribute)
	{
		final ClassificationAttributeUnitModel unit = excelAttribute.getAttributeAssignment().getUnit();
		if (unit != null)
		{
			final Collection<ClassificationAttributeUnitModel> units = getClassificationSystemService()
					.getUnitsOfTypeForSystemVersion(unit.getSystemVersion(), unit.getUnitType());

			if (CollectionUtils.isNotEmpty(units))
			{
				final String unitsAsString = units.stream()//
						.sorted(Comparator.comparing(ClassificationAttributeUnitModel::getConversionFactor).reversed())//
						.map(ClassificationAttributeUnitModel::getCode)//
						.collect(Collectors.joining(ExcelTemplateConstants.MULTI_VALUE_DELIMITER));
				final String singleReferenceFormat = singleReferenceFormat(excelAttribute);
				return StringUtils.isEmpty(singleReferenceFormat)
						? String.format(SIMPLE_TYPE_WITH_UNITS_REFERENCE_FORMAT, unitsAsString)
						: String.format(COMPLEX_TYPE_WITH_UNITS_REFERENCE_FORMAT, singleReferenceFormat, unitsAsString);
			}
		}
		return singleReferenceFormat(excelAttribute);
	}

	public ClassificationService getClassificationService()
	{
		return classificationService;
	}

	@Required
	public void setClassificationService(final ClassificationService classificationService)
	{
		this.classificationService = classificationService;
	}

	public ClassificationSystemService getClassificationSystemService()
	{
		return classificationSystemService;
	}

	@Required
	public void setClassificationSystemService(final ClassificationSystemService classificationSystemService)
	{
		this.classificationSystemService = classificationSystemService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public ExcelParserSplitter getExcelParserSplitter()
	{
		return excelParserSplitter;
	}

	@Required
	public void setExcelParserSplitter(final ExcelParserSplitter excelParserSplitter)
	{
		this.excelParserSplitter = excelParserSplitter;
	}

	public ClassificationAttributeHeaderValueCreator getClassificationAttributeHeaderValueCreator()
	{
		return classificationAttributeHeaderValueCreator;
	}

	@Required
	public void setClassificationAttributeHeaderValueCreator(
			final ClassificationAttributeHeaderValueCreator classificationAttributeHeaderValueCreator)
	{
		this.classificationAttributeHeaderValueCreator = classificationAttributeHeaderValueCreator;
	}
}
