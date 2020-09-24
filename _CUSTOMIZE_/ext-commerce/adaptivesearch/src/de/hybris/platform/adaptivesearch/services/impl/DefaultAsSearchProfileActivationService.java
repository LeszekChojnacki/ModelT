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
package de.hybris.platform.adaptivesearch.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileActivationService;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileRegistry;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileActivationService}.
 */
public class DefaultAsSearchProfileActivationService implements AsSearchProfileActivationService
{
	protected static final String CURRENT_SEARCH_PROFILES = "asSearchProfiles";

	private ModelService modelService;
	private SessionService sessionService;
	private AsSearchProfileRegistry asSearchProfileRegistry;

	@Override
	public void setCurrentSearchProfiles(final List<AbstractAsSearchProfileModel> searchProfiles)
	{
		validateParameterNotNullStandardMessage("searchProfiles", searchProfiles);

		final List<PK> pks = searchProfiles.stream().map(AbstractAsSearchProfileModel::getPk).collect(Collectors.toList());

		sessionService.setAttribute(CURRENT_SEARCH_PROFILES, pks);
	}

	@Override
	public Optional<List<AbstractAsSearchProfileModel>> getCurrentSearchProfiles()
	{
		final List<PK> pks = sessionService.getAttribute(CURRENT_SEARCH_PROFILES);
		if (pks == null)
		{
			return Optional.empty();
		}

		final List<AbstractAsSearchProfileModel> searchProfiles = pks.stream().map(modelService::<AbstractAsSearchProfileModel> get)
				.collect(Collectors.toList());

		return Optional.of(searchProfiles);
	}

	@Override
	public void clearCurrentSearchProfiles()
	{
		sessionService.removeAttribute(CURRENT_SEARCH_PROFILES);
	}

	@Override
	public List<AsSearchProfileActivationGroup> getSearchProfileActivationGroupsForContext(final AsSearchProfileContext context)
	{
		final Optional<List<AbstractAsSearchProfileModel>> currentSearchProfiles = getCurrentSearchProfiles();
		if (currentSearchProfiles.isPresent())
		{
			final AsSearchProfileActivationGroup group = new AsSearchProfileActivationGroup();

			group.setSearchProfiles(currentSearchProfiles.get());

			return Collections.singletonList(group);
		}

		final List<AsSearchProfileActivationGroup> searchProfileGroups = new ArrayList<>();

		for (final AsSearchProfileActivationMapping mapping : asSearchProfileRegistry.getSearchProfileActivationMappings())
		{
			final AsSearchProfileActivationStrategy strategy = mapping.getActivationStrategy();
			final AsSearchProfileActivationGroup activeSearchProfileGroup = strategy.getSearchProfileActivationGroup(context);

			if (activeSearchProfileGroup != null)
			{
				searchProfileGroups.add(activeSearchProfileGroup);
			}
		}

		return searchProfileGroups;
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

	public AsSearchProfileRegistry getAsSearchProfileRegistry()
	{
		return asSearchProfileRegistry;
	}

	@Required
	public void setAsSearchProfileRegistry(final AsSearchProfileRegistry asSearchProfileRegistry)
	{
		this.asSearchProfileRegistry = asSearchProfileRegistry;
	}
}
