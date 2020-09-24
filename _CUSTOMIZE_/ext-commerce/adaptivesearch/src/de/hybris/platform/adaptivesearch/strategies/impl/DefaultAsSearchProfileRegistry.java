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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileActivationMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link AsSearchProfileRegistry}.
 */
public class DefaultAsSearchProfileRegistry implements AsSearchProfileRegistry, ApplicationContextAware, InitializingBean
{
	private ApplicationContext applicationContext;

	private Map<String, AsSearchProfileMapping> searchProfileMappings;
	private List<AsSearchProfileActivationMapping> searchProfileActivationMappings;

	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet()
	{
		initializeSearchProfileMappings();
		initializeSearchProfileActivationMappings();
	}

	protected void initializeSearchProfileMappings()
	{
		final Map<String, DefaultAsSearchProfileMapping> beans = applicationContext
				.getBeansOfType(DefaultAsSearchProfileMapping.class);

		searchProfileMappings = beans.values().stream()
				.collect(Collectors.toMap(DefaultAsSearchProfileMapping::getType, mapping -> mapping));
	}

	protected void initializeSearchProfileActivationMappings()
	{
		final Map<String, DefaultAsSearchProfileActivationMapping> beans = applicationContext
				.getBeansOfType(DefaultAsSearchProfileActivationMapping.class);

		searchProfileActivationMappings = beans.values().stream().sorted(this::compareSearchProfileActivationMappings)
				.collect(Collectors.toList());
	}

	protected int compareSearchProfileActivationMappings(final DefaultAsSearchProfileActivationMapping mapping1,
			final DefaultAsSearchProfileActivationMapping mapping2)
	{
		return Integer.compare(mapping2.getPriority(), mapping1.getPriority());
	}

	@Override
	public AsSearchProfileMapping getSearchProfileMapping(final AbstractAsSearchProfileModel searchProfile)
	{
		return searchProfileMappings.get(searchProfile.getClass().getName());
	}

	@Override
	public Map<String, AsSearchProfileMapping> getSearchProfileMappings()
	{
		return Collections.unmodifiableMap(searchProfileMappings);
	}

	@Override
	public List<AsSearchProfileActivationMapping> getSearchProfileActivationMappings()
	{
		return Collections.unmodifiableList(searchProfileActivationMappings);
	}
}
