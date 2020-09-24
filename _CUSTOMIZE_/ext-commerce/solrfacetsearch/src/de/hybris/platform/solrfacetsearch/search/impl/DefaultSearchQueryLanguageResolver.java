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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQueryLanguageResolver;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the interface {@Link SearchQueryLanguageResolver}
 */
public class DefaultSearchQueryLanguageResolver implements SearchQueryLanguageResolver
{
	private CommonI18NService commonI18NService;
	private UserService userService;

	@Override
	public LanguageModel resolveLanguage(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		final UserModel user = userService.getCurrentUser();
		if (user.getSessionLanguage() == null)
		{
			return commonI18NService.getCurrentLanguage();
		}
		else
		{
			return user.getSessionLanguage();
		}
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

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
