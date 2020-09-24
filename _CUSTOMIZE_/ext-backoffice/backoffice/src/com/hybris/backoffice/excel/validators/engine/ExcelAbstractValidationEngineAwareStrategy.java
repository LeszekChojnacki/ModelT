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
package com.hybris.backoffice.excel.validators.engine;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.validation.enums.Severity;
import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;
import de.hybris.platform.validation.services.ValidationService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeValidationDao;
import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.engine.converters.ExcelValueConverter;
import com.hybris.backoffice.excel.validators.engine.converters.ExcelValueConverterRegistry;


public abstract class ExcelAbstractValidationEngineAwareStrategy implements ExcelValidationEngineAwareStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelAbstractValidationEngineAwareStrategy.class);

	private TypeService typeService;
	private ValidationService validationService;
	private BackofficeValidationDao validationDao;
	private ExcelValueConverterRegistry converterRegistry;
	private List<String> constraintGroups;
	private List<Severity> severities;

	/**
	 * Converts cell value to appropriate object representation and invokes validation engine. During validation process
	 * constraint groups and severities are taken into account.
	 *
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return list of constraint violations
	 */
	protected Collection<HybrisConstraintViolation> validateValue(final ImportParameters importParameters,
			final ExcelAttribute excelAttribute)
	{
		final Collection<ConstraintGroupModel> loadedGroups = getValidationDao().getConstraintGroups(getConstraintGroups());
		final Class<ItemModel> modelClass = getTypeService().getModelClass(importParameters.getTypeCode());

		final Object convertedValue = convertValue(excelAttribute, importParameters);
		try
		{
			return getValidationService().validateValue(modelClass, excelAttribute.getQualifier(), convertedValue, loadedGroups)
					.stream().filter(violation -> getSeverities().contains(violation.getViolationSeverity()))
					.collect(Collectors.toList());
		}
		catch (final RuntimeException ex)
		{
			LOG.debug(String.format("Cannot validate field: '%s' of item: '%s' for value: '%s'", excelAttribute.getQualifier(),
					modelClass, convertedValue), ex);
			return Collections.emptyList();
		}
	}

	/**
	 * Converts cell value into appropriate object representation
	 *
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return
	 */
	protected Object convertValue(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		final Optional<ExcelValueConverter> foundConverter = getConverterRegistry().getConverter(excelAttribute, importParameters);
		if (foundConverter.isPresent())
		{
			try
			{
				return foundConverter.get().convert(excelAttribute, importParameters);
			}
			catch (final RuntimeException ex)
			{
				LOG.debug(String.format("Cannot convert %s to %s", importParameters.getCellValue(), excelAttribute.getType()), ex);
			}
		}
		return importParameters.getCellValue();
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public ValidationService getValidationService()
	{
		return validationService;
	}

	@Required
	public void setValidationService(final ValidationService validationService)
	{
		this.validationService = validationService;
	}

	public BackofficeValidationDao getValidationDao()
	{
		return validationDao;
	}

	@Required
	public void setValidationDao(final BackofficeValidationDao validationDao)
	{
		this.validationDao = validationDao;
	}

	public ExcelValueConverterRegistry getConverterRegistry()
	{
		return converterRegistry;
	}

	@Required
	public void setConverterRegistry(final ExcelValueConverterRegistry converterRegistry)
	{
		this.converterRegistry = converterRegistry;
	}

	public List<String> getConstraintGroups()
	{
		return constraintGroups;
	}

	@Required
	public void setConstraintGroups(final List<String> constraintGroups)
	{
		this.constraintGroups = constraintGroups;
	}

	public List<Severity> getSeverities()
	{
		return severities;
	}

	@Required
	public void setSeverities(final List<Severity> severities)
	{
		this.severities = severities;
	}
}
