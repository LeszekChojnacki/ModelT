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
package com.hybris.backoffice.config;


import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.util.Utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.events.AfterInitializationEndBackofficeListener;
import com.hybris.cockpitng.core.util.CockpitProperties;
import com.hybris.cockpitng.core.util.Resettable;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.type.impl.TypeFacadeStrategyRegistry;


/**
 * Class responsible for pre-filling caches in TypeFacade in order to improve performance (for example displaying
 * explorer tree entries) Can be disabled by setting parameter {@value #PROPERTY_FILL_TYPE_FACADE_ON_STARTUP} to false
 * in local.properties.
 */
public class WarmUpTypeFacadeCaches
{
	protected static final String PROPERTY_FILL_TYPE_FACADE_ON_STARTUP = "fill.typefacade.cache.on.startup";
	private static final Logger LOG = LoggerFactory.getLogger(WarmUpTypeFacadeCaches.class);
	private final Set<Resettable> resettableStrategies = new HashSet<>();
	private TypeFacade typeFacade;
	private TypeFacadeStrategyRegistry strategyRegistry;
	private CockpitProperties cockpitProperties;
	private Set<String> typeNames;
	private boolean includeSubtypes;

	public void warmUpCaches()
	{
		final String propertyValue = cockpitProperties.getProperty(PROPERTY_FILL_TYPE_FACADE_ON_STARTUP);
		final boolean enabled = propertyValue == null || Boolean.parseBoolean(propertyValue);

		if (enabled && typeNames != null)
		{
			if (isSystemInitialized())
			{
				cacheTypesAndLoadStrategies(typeNames);
			}
			else if (LOG.isDebugEnabled())
			{
				LOG.debug("Cannot initialize cache (reason: System not initialized)");
			}
		}
	}

	private boolean isSystemInitialized()
	{
		final Tenant tenant = Registry.getCurrentTenantNoFallback();
		return (tenant != null && Utilities.isSystemInitialized(tenant.getDataSource()));
	}

	private void cacheTypesAndLoadStrategies(final Collection<String> typeNames)
	{
		for (final String typeName : typeNames)
		{
			try
			{
				final DataType dataType = typeFacade.load(typeName);
				loadResettableStrategy(typeName);
				if (getIncludeSubtypes())
				{
					cacheTypesAndLoadStrategies(dataType.getSubtypes());
				}
			}
			catch (final TypeNotFoundException ex)
			{
				LOG.warn("Could not load type: " + typeName, ex);
			}
		}
	}

	private void loadResettableStrategy(final String typeName)
	{
		final TypeFacadeStrategy strategy = strategyRegistry.getStrategy(typeName);
		if (strategy instanceof Resettable)
		{
			resettableStrategies.add((Resettable) strategy);
		}
	}

	@Required
	public void setAfterInitializationEndBackofficeListener(
			final AfterInitializationEndBackofficeListener afterInitializationEndBackofficeListener)
	{
		registerAfterInitializationEndCallback(afterInitializationEndBackofficeListener);
	}

	private void registerAfterInitializationEndCallback(
			final AfterInitializationEndBackofficeListener afterInitializationEndBackofficeListener)
	{
		afterInitializationEndBackofficeListener.registerCallback(event -> {
			clearCaches();
			warmUpCaches();
		});
	}

	private void clearCaches()
	{
		for (final Resettable strategy : resettableStrategies)
		{
			strategy.reset();
		}
		resettableStrategies.clear();
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	@Required
	public void setStrategyRegistry(final TypeFacadeStrategyRegistry strategyRegistry)
	{
		this.strategyRegistry = strategyRegistry;
	}

	@Required
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

	/**
	 * @param typeNames
	 *           set of type names which should be loaded at startup.
	 */
	public void setTypeNames(final Set<String> typeNames)
	{
		this.typeNames = typeNames;
	}

	public boolean getIncludeSubtypes()
	{
		return includeSubtypes;
	}

	public void setIncludeSubtypes(boolean includeSubtypes)
	{
		this.includeSubtypes = includeSubtypes;
	}
}
