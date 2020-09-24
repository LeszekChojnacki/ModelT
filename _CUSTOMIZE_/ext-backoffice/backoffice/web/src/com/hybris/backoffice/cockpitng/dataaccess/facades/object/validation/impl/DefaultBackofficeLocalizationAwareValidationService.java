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

import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.services.impl.LocalizedHybrisConstraintViolation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation.BackofficeLocalizationAwareValidationService;
import com.hybris.cockpitng.validation.LocalizedQualifier;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.model.ValidationInfo;


public class DefaultBackofficeLocalizationAwareValidationService extends DefaultBackofficeValidationService
		implements BackofficeLocalizationAwareValidationService
{

	@Override
	public List<ValidationInfo> validate(final Object objectToValidate, final Collection<LocalizedQualifier> localizedQualifiers,
			final ValidationContext validationContext)
	{
		if (objectToValidate == null)
		{
			return Collections.emptyList();
		}

		final List<String> qualifiers = localizedQualifiers.stream().map(LocalizedQualifier::getName).collect(Collectors.toList());

		final Set<HybrisConstraintViolation> constraintViolations = validateProperties(objectToValidate, qualifiers,
				validationContext);

		final Set<LocalizedHybrisConstraintViolation> invalidValidationConstraints = constraintViolations.stream()
				.filter(LocalizedHybrisConstraintViolation.class::isInstance).map(LocalizedHybrisConstraintViolation.class::cast)
				.filter(violation -> isViolationInvalidForLocalizedQualifier(localizedQualifiers, violation))
				.collect(Collectors.toSet());

		constraintViolations.removeAll(invalidValidationConstraints);

		return translatePlatformViolations(objectToValidate, validationContext, constraintViolations);
	}

	protected boolean isViolationInvalidForLocalizedQualifier(final Collection<LocalizedQualifier> localizedQualifiers,
			final LocalizedHybrisConstraintViolation violation)
	{
		final String violatingQualifier = violation.getProperty();
		final Locale violatingQualifierLanguage = violation.getViolationLanguage();

		final Collection<Locale> validatingLocales = getValidatingLocalesForQualifier(localizedQualifiers, violatingQualifier);

		return !validatingLocales.isEmpty() && !validatingLocales.contains(violatingQualifierLanguage);
	}

	private Collection<Locale> getValidatingLocalesForQualifier(final Collection<LocalizedQualifier> localizedQualifiers,
			final String violatingQualifier)
	{
		return localizedQualifiers.stream().filter(localizedQualifier -> violatingQualifier.equals(localizedQualifier.getName()))
				.findFirst().map(LocalizedQualifier::getLocales).orElse(Collections.emptyList());
	}
}
