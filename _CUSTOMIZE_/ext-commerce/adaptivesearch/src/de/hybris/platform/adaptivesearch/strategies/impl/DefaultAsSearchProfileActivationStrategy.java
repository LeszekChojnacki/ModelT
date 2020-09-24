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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.daos.AsSearchProfileActivationSetDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsSearchProfileActivationSetModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationStrategy;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileActivationStrategy}.
 */
public class DefaultAsSearchProfileActivationStrategy implements AsSearchProfileActivationStrategy
{
	private AsSearchProfileActivationSetDao asSearchProfileActivationSetDao;

	@Override
	public List<AbstractAsSearchProfileModel> getActiveSearchProfiles(final AsSearchProfileContext context)
	{
		final List<AsSearchProfileActivationSetModel> activationSets = getActivationSets(context);

		if (CollectionUtils.isEmpty(activationSets))
		{
			return Collections.emptyList();
		}

		return activationSets.stream().sorted(this::compareActivationSets)
				.flatMap(activationSet -> activationSet.getSearchProfiles().stream()).collect(Collectors.toList());
	}

	protected List<AsSearchProfileActivationSetModel> getActivationSets(final AsSearchProfileContext context)
	{
		final List<AsSearchProfileActivationSetModel> activationSets = new ArrayList<>();

		if (CollectionUtils.isEmpty(context.getCatalogVersions()))
		{
			final Optional<AsSearchProfileActivationSetModel> activationSetResult = asSearchProfileActivationSetDao
					.findSearchProfileActivationSetByIndexType(null, context.getIndexType());

			activationSetResult.ifPresent(activationSets::add);
		}
		else
		{
			for (final CatalogVersionModel catalogVersion : context.getCatalogVersions())
			{
				final Optional<AsSearchProfileActivationSetModel> activationSetResult = asSearchProfileActivationSetDao
						.findSearchProfileActivationSetByIndexType(catalogVersion, context.getIndexType());

				activationSetResult.ifPresent(activationSets::add);
			}
		}

		return activationSets;
	}

	protected int compareActivationSets(final AsSearchProfileActivationSetModel activationSet1,
			final AsSearchProfileActivationSetModel activationSet2)
	{
		return activationSet2.getPriority().compareTo(activationSet1.getPriority());
	}

	public AsSearchProfileActivationSetDao getAsSearchProfileActivationSetDao()
	{
		return asSearchProfileActivationSetDao;
	}

	@Required
	public void setAsSearchProfileActivationSetDao(final AsSearchProfileActivationSetDao asSearchProfileActivationSetDao)
	{
		this.asSearchProfileActivationSetDao = asSearchProfileActivationSetDao;
	}
}
