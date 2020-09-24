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
package com.hybris.backoffice.variants;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.product.VariantsService;
import de.hybris.platform.product.impl.DefaultVariantsService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.variants.jalo.VariantProduct;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class DefaultBackofficeVariantsService extends DefaultVariantsService implements BackofficeVariantsService
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeVariantsService.class);

	private transient VariantsService variantService;
	private transient ModelService modelService;
	private transient SessionService sessionService;
	private transient UserService userService;
	private transient CommonI18NService commonI18NService;


	@Override
	public Map<Locale, Object> getLocalizedVariantAttributeValue(final VariantProductModel variant, final String qualifier)
	{
		final VariantProduct variantProduct = modelService.getSource(variant);

		final Object attributeValue = getLocalizedValuesForAllLanguages(qualifier, variantProduct);
		if (attributeValue != null)
		{
			return convertToLocaleMap((Map<Language, Object>) attributeValue);
		}
		return null;
	}

	private Object getLocalizedValuesForAllLanguages(final String qualifier, final VariantProduct variantProduct)
	{
		return sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				final SessionContext sessionContext = JaloSession.getCurrentSession().getSessionContext();
				sessionContext.setLanguage(null);
				try
				{
					return variantProduct.getAttribute(qualifier);
				}
				catch (final JaloSecurityException e)
				{
					LOG.error("Couldn't retrieve variant attribute for qualifier " + qualifier, e);
				}
				return null;
			}
		}, userService.getAdminUser());
	}

	@Override
	public void setLocalizedVariantAttributeValue(final VariantProductModel variantProductModel, final String qualifier,
			final Map<Locale, Object> localizedValues)
	{

		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				final SessionContext sessionContext = JaloSession.getCurrentSession().getSessionContext();
				sessionContext.setLanguage(null);
				variantService.setVariantAttributeValue(variantProductModel, qualifier, convertToLanguageMap(localizedValues));
			}
		}, userService.getAdminUser());
	}

	private Map<Locale, Object> convertToLocaleMap(final Map<Language, Object> languageMap)
	{
		final Map<Locale, Object> localeMap = new HashMap<>();
		languageMap.forEach((language, value) -> localeMap.put(language.getLocale(), value));
		return localeMap;
	}

	private Map<LanguageModel, Object> convertToLanguageMap(final Map<Locale, Object> localeMap)
	{
		final Map<LanguageModel, Object> languageMap = new HashMap<>();
		localeMap.forEach((locale, value) -> {
			final LanguageModel language = commonI18NService.getLanguage(locale.toString());
			languageMap.put(language, value);
		});
		return languageMap;
	}

	public VariantsService getVariantService()
	{
		return variantService;
	}

	@Required
	public void setVariantService(final VariantsService variantService)
	{
		this.variantService = variantService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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
}
