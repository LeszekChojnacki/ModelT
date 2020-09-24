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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.ruleengineservices.enums.FactContextType;
import de.hybris.platform.ruleengineservices.rao.providers.FactContextFactory;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link FactContext}.
 */
public class DefaultFactContextFactory implements FactContextFactory
{
	private Map<String, Map<Class, List<RAOProvider>>> raoProviders;
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFactContextFactory.class);

	@Override
	public FactContext createFactContext(final FactContextType type, final Collection<?> facts)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("###################FactContextFactory RaoProviders Map####################");
			raoProviders.forEach((k, v) -> LOGGER.debug("Map Key+++++++++++++ {}. Map Value+++++++++++ {}", k, v));
		}
		if (!getRaoProviders().containsKey(type.toString()))
		{
			throw new IllegalArgumentException(String.format("The Fact Context Type with name '%s' is not defined", type.name()));
		}
		return new FactContext(type, getRaoProviders().get(type.name()), facts);
	}

	protected Map<String, Map<Class, List<RAOProvider>>> getRaoProviders()
	{
		return raoProviders;
	}

	@Required
	public void setRaoProviders(final Map<String, Map<Class, List<RAOProvider>>> raoProviders)
	{
		this.raoProviders = raoProviders;
	}
}
