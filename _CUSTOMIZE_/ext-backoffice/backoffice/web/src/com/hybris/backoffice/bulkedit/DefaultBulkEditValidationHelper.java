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
package com.hybris.backoffice.bulkedit;

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.cockpitng.type.ObjectValueService;
import com.hybris.cockpitng.validation.LocalizationAwareValidationHandler;
import com.hybris.cockpitng.validation.LocalizedQualifier;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.ValidationHandler;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


public class DefaultBulkEditValidationHelper implements BulkEditValidationHelper
{

	private ObjectValueService objectValueService;
	private LocalizationAwareValidationHandler localizationAwareValidationHandler;
	private CommonI18NService commonI18NService;
	private ValidationHandler validationHandler;

	@Override
	public Set<String> getValidatableProperties(final BulkEditForm bulkEditForm)
	{
		return bulkEditForm.getAttributesForm().getChosenAttributes().stream()
				.filter(attribute -> hasValidatableData(bulkEditForm, bulkEditForm.getTemplateObject(), attribute.getQualifier()))
				.map(Attribute::getQualifier).collect(Collectors.toSet());
	}

	@Override
	public Collection<LocalizedQualifier> getValidatablePropertiesWithLocales(final BulkEditForm bulkEditForm)
	{
		return bulkEditForm.getAttributesForm().getChosenAttributes().stream()
				.filter(attribute -> hasValidatableData(bulkEditForm, bulkEditForm.getTemplateObject(), attribute.getQualifier()))
				.map(attribute -> new LocalizedQualifier(attribute.getQualifier(), extractPropertyLocales(attribute)))
				.collect(Collectors.toSet());
	}

	protected List<Locale> extractPropertyLocales(final Attribute chosenAttribute)
	{
		return chosenAttribute.getSubAttributes().stream().map(Attribute::getIsoCode).map(commonI18NService::getLocaleForIsoCode)
				.collect(Collectors.toList());
	}

	@Override
	public ValidationHandler createProxyValidationHandler(final BulkEditForm bulkEditForm)
	{

		return new ValidationHandler()
		{
			@Override
			public List<ValidationInfo> validate(final Object objectToValidate, final ValidationContext validationContext)
			{
				return localizationAwareValidationHandler.validate(objectToValidate, validationContext);
			}

			@Override
			public List<ValidationInfo> validate(final Object objectToValidate, final List<String> qualifiers,
					final ValidationContext validationContext)
			{
				Collection<LocalizedQualifier> localizedQualifiers = getValidatablePropertiesWithLocales(bulkEditForm);
				if (!bulkEditForm.isValidateAllAttributes())
				{
					localizedQualifiers = localizedQualifiers.stream().filter(a -> qualifiers.contains(a.getName()))
							.collect(Collectors.toSet());
				}
				return getLocalizationAwareValidationHandler().validate(objectToValidate, localizedQualifiers, validationContext);
			}
		};
	}

	@Override
	public Map<Object, List<ValidationInfo>> validateModifiedItems(final BulkEditForm bulkEditForm,
			final ValidationSeverity severityHigherThan)
	{
		final Collection<LocalizedQualifier> validatableProperties = getValidatablePropertiesWithLocales(bulkEditForm);
		final Map<Object, List<ValidationInfo>> marchingValidations = new HashMap<>();

		bulkEditForm.getItemsToEdit().forEach(item -> {
			final List<ValidationInfo> validationResults = bulkEditForm.isValidateAllAttributes()
					? localizationAwareValidationHandler.validate(item)
					: localizationAwareValidationHandler.validate(item, validatableProperties, null);

			final List<ValidationInfo> errors = validationResults.stream()
					.filter(info -> info.getValidationSeverity().isHigherThan(severityHigherThan)).collect(Collectors.toList());
			if (!errors.isEmpty())
			{
				marchingValidations.put(item, errors);
			}
		});
		return marchingValidations;
	}

	protected boolean hasValidatableData(final BulkEditForm bulkEditForm, final Object objectToValidate, final String qualifier)
	{
		if (bulkEditForm.isClearAttribute(qualifier))
		{
			return true;
		}

		final Object value = getObjectValueService().getValue(qualifier, objectToValidate);

		if (value instanceof Map)
		{
			return ((Map) value).values().stream().anyMatch(localizedValue -> localizedValue != null
					&& (!String.class.isInstance(value) || StringUtils.isNotBlank((CharSequence) value)));

		}
		else if (value instanceof Collection)
		{
			return CollectionUtils.isNotEmpty(((Collection) value));
		}
		else
		{
			return value != null;
		}
	}

	public ObjectValueService getObjectValueService()
	{
		return objectValueService;
	}

	@Required
	public void setObjectValueService(final ObjectValueService objectValueService)
	{
		this.objectValueService = objectValueService;
	}

	public LocalizationAwareValidationHandler getLocalizationAwareValidationHandler()
	{
		return localizationAwareValidationHandler;
	}

	@Required
	public void setLocalizationAwareValidationHandler(final LocalizationAwareValidationHandler validationHandler)
	{
		this.localizationAwareValidationHandler = validationHandler;
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

	/**
	 * @deprecated since 1811 in favour of {@link #getLocalizationAwareValidationHandler()}
	 */
	@Deprecated
	public ValidationHandler getValidationHandler()
	{
		return validationHandler;
	}

	/**
	 * @deprecated since 1811 in favour of
	 *             {@link #setLocalizationAwareValidationHandler(LocalizationAwareValidationHandler)}
	 */
	@Deprecated
	public void setValidationHandler(final ValidationHandler validationHandler)
	{
		this.validationHandler = validationHandler;
	}
}
