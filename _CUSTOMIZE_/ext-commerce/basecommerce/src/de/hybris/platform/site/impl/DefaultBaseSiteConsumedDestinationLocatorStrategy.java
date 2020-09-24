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

package de.hybris.platform.site.impl;

import de.hybris.platform.apiregistryservices.model.AbstractDestinationModel;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.services.DestinationService;
import de.hybris.platform.apiregistryservices.strategies.ConsumedDestinationLocatorStrategy;
import de.hybris.platform.site.BaseSiteService;

import static de.hybris.platform.apiregistryservices.strategies.impl.DefaultConsumedDestinationLocatorStrategy.CLIENT_CLASS_NAME;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Strategy for finding the consumed destination regarding to current base site
 */
public class DefaultBaseSiteConsumedDestinationLocatorStrategy implements ConsumedDestinationLocatorStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBaseSiteConsumedDestinationLocatorStrategy.class);
	public static final String BASE_SITE = "baseSite";

	private DestinationService<AbstractDestinationModel> destinationService;
	private BaseSiteService baseSiteService;
	
	@Override
	public ConsumedDestinationModel lookup(final String clientTypeName)
	{
		final List<AbstractDestinationModel> destinations = getDestinationService().getAllDestinations();
		final String currentBaseSiteId = getCurrentBaseSiteId();

		final Optional<AbstractDestinationModel> destination = destinations.stream()
				.filter(ConsumedDestinationModel.class::isInstance)
				.filter(dest -> dest.getAdditionalProperties().containsKey(CLIENT_CLASS_NAME)
						&& dest.getAdditionalProperties().get(CLIENT_CLASS_NAME).equals(clientTypeName))
				.filter(dest -> dest.getAdditionalProperties().containsKey(BASE_SITE)
						? dest.getAdditionalProperties().get(BASE_SITE).equals(currentBaseSiteId)
						: (currentBaseSiteId == null))
				.findFirst();

		if (!destination.isPresent())
		{
			LOG.warn("Failed to find consumed destination for the given id [{}] and the current base site [{}]", clientTypeName,
					currentBaseSiteId);
			return null;
		}

		return (ConsumedDestinationModel) destination.get();
	}

	protected String getCurrentBaseSiteId()
	{
		return getBaseSiteService().getCurrentBaseSite() != null ? getBaseSiteService().getCurrentBaseSite().getUid() : null;
	}

	protected DestinationService<AbstractDestinationModel> getDestinationService()
	{
		return destinationService;
	}

	@Required
	public void setDestinationService(final DestinationService<AbstractDestinationModel> destinationService)
	{
		this.destinationService = destinationService;
	}

	protected BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}
}
