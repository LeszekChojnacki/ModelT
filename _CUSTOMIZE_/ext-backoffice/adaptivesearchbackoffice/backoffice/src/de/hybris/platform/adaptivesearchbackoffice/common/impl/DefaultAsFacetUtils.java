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
package de.hybris.platform.adaptivesearchbackoffice.common.impl;

import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetVisibility;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsFacetUtils}.
 */
public class DefaultAsFacetUtils implements AsFacetUtils
{
	private SessionService sessionService;
	private CommonI18NService commonI18NService;
	private AsSearchProviderFactory asSearchProviderFactory;

	@Override
	public boolean isOpen(final AsFacetData facet)
	{
		return (CollectionUtils.isNotEmpty(facet.getValues()) || CollectionUtils.isNotEmpty(facet.getSelectedValues()))
				&& (facet.getVisibility() == AsFacetVisibility.SHOW_TOP_VALUES
						|| facet.getVisibility() == AsFacetVisibility.SHOW_VALUES);
	}

	@Override
	public void localizeFacets(final NavigationContextData navigationContext, final SearchContextData searchContext,
			final List<? extends AbstractFacetConfigurationEditorData> facets)
	{
		if (navigationContext == null || StringUtils.isBlank(navigationContext.getIndexType()) || searchContext == null
				|| StringUtils.isBlank(searchContext.getLanguage()))
		{
			return;
		}

		final String indexType = navigationContext.getIndexType();
		final String languageCode = searchContext.getLanguage();

		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				final LanguageModel language = commonI18NService.getLanguage(languageCode);
				commonI18NService.setCurrentLanguage(language);

				final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();

				for (final AbstractFacetConfigurationEditorData facet : facets)
				{
					final Optional<AsIndexPropertyData> indexProperty = searchProvider.getIndexPropertyForCode(indexType,
							facet.getIndexProperty());
					localizeFacet(facet, indexProperty);
				}
			}
		});
	}

	protected void localizeFacet(final AbstractFacetConfigurationEditorData facet,
			final Optional<AsIndexPropertyData> indexProperty)
	{
		if (indexProperty.isPresent() && !StringUtils.isBlank(indexProperty.get().getName()))
		{
			facet.setLabel(indexProperty.get().getName());
		}
		else
		{
			facet.setLabel("[" + facet.getIndexProperty() + "]");
		}
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

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public AsSearchProviderFactory getAsSearchProviderFactory()
	{
		return asSearchProviderFactory;
	}

	@Required
	public void setAsSearchProviderFactory(final AsSearchProviderFactory asSearchProviderFactory)
	{
		this.asSearchProviderFactory = asSearchProviderFactory;
	}
}
