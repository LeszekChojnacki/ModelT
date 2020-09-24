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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategyFactory;

import org.springframework.beans.factory.annotation.Required;


/*
* Default implementation for the {@FacetSearchStrategyFactory} interface which returns an implementation for the
* {@FacetSearchStrategy} based on the legacy mode flag inside the configuration
*/
public class DefaultFacetSearchStrategyFactory implements FacetSearchStrategyFactory
{
	private FacetSearchStrategy legacyFacetSearchStrategy;

	private FacetSearchStrategy defaultFacetSearchStrategy;

	@Override
	public FacetSearchStrategy createStrategy(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		if (facetSearchConfig.getSearchConfig().isLegacyMode())
		{
			return legacyFacetSearchStrategy;
		}

		return defaultFacetSearchStrategy;
	}

	public FacetSearchStrategy getDefaultFacetSearchStrategy()
	{
		return defaultFacetSearchStrategy;
	}

	public void setDefaultFacetSearchStrategy(final FacetSearchStrategy defaultFacetSearchStrategy)
	{
		this.defaultFacetSearchStrategy = defaultFacetSearchStrategy;
	}

	public FacetSearchStrategy getLegacyFacetSearchStrategy()
	{
		return legacyFacetSearchStrategy;
	}

	@Required
	public void setLegacyFacetSearchStrategy(final FacetSearchStrategy legacyFacetSearchStrategy)
	{
		this.legacyFacetSearchStrategy = legacyFacetSearchStrategy;
	}
}
