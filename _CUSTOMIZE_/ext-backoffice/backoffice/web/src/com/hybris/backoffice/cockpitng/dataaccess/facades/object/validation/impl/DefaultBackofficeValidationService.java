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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.ViewResultItem;
import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;
import de.hybris.platform.validation.services.ValidationService;
import de.hybris.platform.validation.services.impl.LocalizedHybrisConstraintViolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation.BackofficeValidationService;
import com.hybris.backoffice.daos.BackofficeValidationDao;
import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationGroup;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


/**
 * Default implementation of the {@link BackofficeValidationService} using {@link ValidationService} to validate
 * objects.
 */
public class DefaultBackofficeValidationService implements BackofficeValidationService
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBackofficeValidationService.class);

	private Map<String, Class> attributesNotSupportedByValidation;
	private ValidationService validationService;
	private BackofficeValidationDao validationDao;

	private List<String> validateGroups = Collections.emptyList();
	private TypeFacade typeFacade;
	private ModelService modelService;

	public BackofficeValidationDao getValidationDao()
	{
		return validationDao;
	}

	@Required
	public void setValidationDao(final BackofficeValidationDao validationDao)
	{
		this.validationDao = validationDao;
	}

	/**
	 * Set the attributes that are not supported by validation service.
	 */
	@Required
	public void setAttributesNotSupportedByValidation(final Map<String, Class> attributesNotSupportedByValidation)
	{
		this.attributesNotSupportedByValidation = attributesNotSupportedByValidation;
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

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public List<String> getValidateGroups()
	{
		return validateGroups;
	}

	public void setValidateGroups(final List<String> validateGroups)
	{
		this.validateGroups = validateGroups;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Override
	public List<ValidationInfo> validate(final Object objectToValidate, final ValidationContext validationContext)
	{
		if (objectToValidate == null)
		{
			return Collections.emptyList();
		}

		final Collection<ConstraintGroupModel> platformGroups = getPlatformConstraintGroupModels(validationContext);
		final Set<HybrisConstraintViolation> validate = getValidationService().validate(objectToValidate, platformGroups);
		return translatePlatformViolations(objectToValidate, validationContext, validate);
	}

	@Override
	public List<ValidationInfo> validate(final Object objectToValidate, final List<String> qualifiers,
			final ValidationContext validationContext)
	{
		if (objectToValidate == null)
		{
			return Collections.emptyList();
		}

		final Set<HybrisConstraintViolation> constraintViolations = validateProperties(objectToValidate, qualifiers,
				validationContext);

		return translatePlatformViolations(objectToValidate, validationContext, constraintViolations);
	}

	protected Set<HybrisConstraintViolation> validateProperties(final Object objectToValidate, final List<String> qualifiers,
			final ValidationContext validationContext)
	{
		final Set<HybrisConstraintViolation> constraintViolations = new HashSet<>();
		if (shouldValidateObject(objectToValidate))
		{
			for (final String propertyToValidate : qualifiers)
			{
				validateSingleProperty(objectToValidate, validationContext, constraintViolations, propertyToValidate);
			}
		}
		return constraintViolations;
	}

	protected void validateSingleProperty(final Object objectToValidate, final ValidationContext validationContext,
			final Set<HybrisConstraintViolation> constraintViolations, final String propertyToValidate)
	{
		final boolean noValidation = attributesNotSupportedByValidation.keySet().stream().anyMatch(propertyToValidate::contains);

		if (noValidation)
		{
			LOGGER.debug("Property {} is not supported by validation framework", propertyToValidate);
		}
		else
		{
			final Collection<ConstraintGroupModel> platformGroups = getPlatformConstraintGroupModels(validationContext);

			final Set<HybrisConstraintViolation> currentPropertyViolations = validateSingleProperty(objectToValidate,
					propertyToValidate, platformGroups);

			constraintViolations.addAll(currentPropertyViolations);
		}
	}

	protected Set<HybrisConstraintViolation> validateSingleProperty(final Object objectToValidate, final String propertyToValidate,
			final Collection<ConstraintGroupModel> platformGroups)
	{
		try
		{
			return getValidationService().validateProperty(objectToValidate, propertyToValidate, platformGroups);
		}
		catch (final RuntimeException originalException)
		{
			final DataAttribute attribute = getTypeFacade().getAttribute(objectToValidate, propertyToValidate);
			if (attribute != null)
			{
				// assume a flex attribute - no validation
				return Collections.emptySet();
			}
			throw originalException;
		}
	}

	protected Set<HybrisConstraintViolation> validateSingleValue(final Object objectToValidate, final String propertyToValidate,
			final Collection<ConstraintGroupModel> platformGroups)
	{
		final Object attributeValue = getModelService().getAttributeValue(objectToValidate, propertyToValidate);
		return getValidationService().validateValue(objectToValidate.getClass(), propertyToValidate, attributeValue,
				platformGroups);
	}

	protected boolean shouldValidateObject(final Object objectToValidate)
	{
		return objectToValidate instanceof ItemModel || objectToValidate instanceof ViewResultItem
				|| objectToValidate instanceof Collection;
	}

	protected List<ValidationInfo> translatePlatformViolations(final Object objectToValidate,
			final ValidationContext validationContext, final Set<HybrisConstraintViolation> validate)
	{
		if (CollectionUtils.isNotEmpty(validate))
		{
			final List<ValidationInfo> validationResult = new ArrayList<>();
			try
			{
				final DataType dataType = getDataType(objectToValidate);
				validationResult
						.addAll(validate.stream().map(each -> getCockpitValidationObject(each, dataType)).collect(Collectors.toList()));
				return adjustConfirmedWarnings(validationResult, validationContext);
			}
			catch (final TypeNotFoundException e)
			{
				LOGGER.warn(String.format("Could not load type for %s", objectToValidate.getClass().getSimpleName()), e);
			}
		}
		return Collections.emptyList();
	}

	private DataType getDataType(final Object objectToValidate) throws TypeNotFoundException
	{
		final String type = getTypeFacade().getType(objectToValidate);
		return getTypeFacade().load(type);
	}

	private Collection<ConstraintGroupModel> getPlatformConstraintGroupModels(final ValidationContext validationContext)
	{
		if (validationContext != null && CollectionUtils.isNotEmpty(validationContext.getConstraintGroups()))
		{
			final List<String> ids = validationContext.getConstraintGroups().stream().map(ValidationGroup::getId)
					.collect(Collectors.toList());
			return getValidationDao().getConstraintGroups(ids);
		}
		else
		{
			final ArrayList<ConstraintGroupModel> groupModels = new ArrayList<>();
			if (!getValidateGroups().isEmpty())
			{
				groupModels.addAll(getValidationDao().getConstraintGroups(getValidateGroups()));
			}

			groupModels.add(getValidationService().getDefaultConstraintGroup());

			return groupModels;
		}
	}

	private ValidationInfo getCockpitValidationObject(final HybrisConstraintViolation violation, final DataType dataType)
	{
		final DefaultValidationInfo validation = new DefaultValidationInfo();
		validation.setValidationMessage(violation.getLocalizedMessage());
		if (StringUtils.isBlank(violation.getProperty()))
		{
			validation.setInvalidPropertyPath(StringUtils.EMPTY);
		}
		else
		{
			final DataAttribute attribute = dataType.getAttribute(violation.getProperty());
			validation.setInvalidPropertyPath(attribute.getQualifier());
		}
		validation.setConfirmed(false);
		validation.setInvalidValue(violation.getConstraintViolation().getInvalidValue());
		validation.setValidationSeverity(getSeverity(violation));
		insertLocaleIDForLocalizedViolations(violation, validation);
		return validation;
	}

	private static void insertLocaleIDForLocalizedViolations(final HybrisConstraintViolation violation,
			final ValidationInfo validation)
	{
		if (violation instanceof LocalizedHybrisConstraintViolation)
		{
			final Locale violationLanguage = ((LocalizedHybrisConstraintViolation) violation).getViolationLanguage();
			if (violationLanguage != null)
			{
				((DefaultValidationInfo) validation)
						.setInvalidPropertyPath(violation.getProperty() + "[" + violationLanguage.toLanguageTag() + "]");
			}
		}
	}

	private ValidationSeverity getSeverity(final HybrisConstraintViolation violation)
	{
		switch (violation.getViolationSeverity())
		{
			case ERROR:
				return ValidationSeverity.ERROR;
			case WARN:
				return ValidationSeverity.WARN;
			case INFO:
				return ValidationSeverity.INFO;
		}
		return null;
	}

	private static List<ValidationInfo> adjustConfirmedWarnings(final List<ValidationInfo> validationInfos,
			final ValidationContext validationContext)
	{
		if (validationContext != null)
		{
			validationInfos.forEach(info -> {
				final List<ValidationInfo> confirmed = validationContext.getConfirmed(info.getValidationSeverity());
				confirmValidationResult(info, confirmed);
			});
		}
		return validationInfos;
	}

	private static void confirmValidationResult(final ValidationInfo candidate, final List<ValidationInfo> confirmed)
	{
		for (final ValidationInfo each : confirmed)
		{
			final boolean result = each.isConfirmed()
					&& ObjectValuePath.parse(each.getInvalidPropertyPath()).startsWith(candidate.getInvalidPropertyPath())
					&& each.getValidationSeverity().equals(candidate.getValidationSeverity())
					&& each.getValidationMessage().equals(candidate.getValidationMessage());
			if (result)
			{
				candidate.setConfirmed(true);
			}
		}
	}

}
