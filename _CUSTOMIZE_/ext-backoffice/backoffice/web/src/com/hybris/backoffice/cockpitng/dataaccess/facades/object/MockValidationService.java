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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.spring.SpringUtil;

import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


/**
 * Mock validation service.
 */
public class MockValidationService
{

	private static final Logger LOGGER = LoggerFactory.getLogger(MockValidationService.class);

	private TypeFacade typeFacade;

	private CockpitLocaleService localeService;

	private LabelService labelService;

	public TypeFacade getTypeFacade()
	{
		if (typeFacade == null)
		{
			typeFacade = (TypeFacade) SpringUtil.getBean("defaultTypeFacade");
		}
		return typeFacade;
	}

	public CockpitLocaleService getLocaleService()
	{
		if (localeService == null)
		{
			localeService = (CockpitLocaleService) SpringUtil.getBean("cockpitLocaleService");
		}
		return localeService;
	}

	public LabelService getLabelService()
	{
		if (labelService == null)
		{
			labelService = (LabelService) SpringUtil.getBean("labelService");
		}
		return labelService;
	}

	/**
	 * Validates the given object.
	 *
	 * @param objectToValidate
	 *           object to validate.
	 * @param qualifiers
	 *           qualifiers to validate.
	 * @param validationContext
	 *           validation context.
	 * @param <T>
	 *           type of the object to validate.
	 * @return a list of validation info (@link {@link ValidationInfo} objects.
	 */
	public <T> List<ValidationInfo> validate(final T objectToValidate, final Collection<String> qualifiers,
			final ValidationContext validationContext)
	{
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Generating validation error for {}: {}", getLabelService().getObjectLabel(objectToValidate),
					Objects.toString(qualifiers, StringUtils.EMPTY));
		}

		final Random random = new Random();
		random.setSeed(new Date().getTime());
		final int max = random.nextInt(ValidationSeverity.values().length - 1);
		final ValidationSeverity severity = getSeverity(max);

		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Generating validation error for {}: {} with severity {}",
					getLabelService().getObjectLabel(objectToValidate), Objects.toString(qualifiers, StringUtils.EMPTY), severity);
		}

		if (ValidationSeverity.NONE.isLowerThan(severity))
		{
			final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
			validationInfo.setValidationMessage(createValidationMessageForIncorrectValue(qualifiers.stream().findFirst()));
			validationInfo.setValidationSeverity(severity);
			if (CollectionUtils.isNotEmpty(qualifiers))
			{
				validationInfo.setInvalidPropertyPath(qualifiers.iterator().next());
			}
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("Generated validation error for {}:", getLabelService().getObjectLabel(objectToValidate),
						Objects.toString(qualifiers, StringUtils.EMPTY));
			}
			return Collections.singletonList(validationInfo);
		}
		return Collections.emptyList();
	}

	/**
	 * Validates the given object.
	 *
	 * @param objectToValidate
	 *           object to validate.
	 * @param validationContext
	 *           validation context.
	 * @param <T>
	 *           type of the object to validate.
	 * @return a list of validation info (@link {@link ValidationInfo} objects.
	 */
	public <T> List<ValidationInfo> validate(final T objectToValidate, final ValidationContext validationContext)
	{
		final String type = getTypeFacade().getType(objectToValidate);
		if ("MyProduct".equals(type))
		{
			LOGGER.info("Generating validation errors for " + getLabelService().getObjectLabel(objectToValidate));
			try
			{
				final Random random = new Random();
				random.setSeed(new Date().getTime());
				final int max = random.nextInt(ValidationSeverity.values().length - 1);
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("Generating validation errors for {} not higher than {}",
							getLabelService().getObjectLabel(objectToValidate), ValidationSeverity.values()[max]);
				}

				final List<ValidationInfo> validationInfos = new ArrayList<>();
				final DataType dataType = getTypeFacade().load(type);
				final Collection<DataAttribute> allAttributes = dataType.getAttributes();
				for (final DataAttribute attrib : allAttributes)
				{
					if (attrib.isLocalized())
					{
						getLocaleService().getAllLocales().stream().filter(locale -> random.nextBoolean()).forEach(locale -> {
							final ValidationSeverity severity = getSeverity(max);
							if (ValidationSeverity.NONE.isLowerThan(severity))
							{
								final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
								validationInfo.setValidationSeverity(severity);
								validationInfo.setInvalidPropertyPath(attrib.getQualifier() + "[" + locale.getISO3Language() + "]");
								validationInfo.setValidationMessage(createValidationMessageForIncorrectValue(attrib.getQualifier()));
								validationInfos.add(validationInfo);
							}
						});
					}
					else
					{
						final ValidationSeverity severity = getSeverity(max);
						if (ValidationSeverity.NONE.isLowerThan(severity))
						{
							final DefaultValidationInfo validationInfo = new DefaultValidationInfo();
							validationInfo.setValidationSeverity(severity);
							validationInfo.setInvalidPropertyPath(attrib.getQualifier());
							validationInfo.setValidationMessage(createValidationMessageForIncorrectValue(attrib.getQualifier()));
							validationInfos.add(validationInfo);
						}
					}

				}
				if (LOGGER.isInfoEnabled())
				{
					LOGGER.info("Generated {} validation error for {}", validationInfos.size(),
							getLabelService().getObjectLabel(objectToValidate));
				}
				return validationInfos;
			}
			catch (final TypeNotFoundException e)
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(String.format("Type not found: %s.", type), e);
				}
				return Collections.emptyList();
			}
		}
		else
		{
			return Collections.emptyList();
		}
	}

	private static String createValidationMessageForIncorrectValue(final Object object)
	{
		return object + " has incorrect value";
	}

	protected ValidationSeverity getSeverity(final int max)
	{
		final Random random = new Random();
		random.setSeed(new Date().getTime());
		final int idx = random.nextInt(ValidationSeverity.values().length - max);
		return ValidationSeverity.values()[max + idx];
	}
}
