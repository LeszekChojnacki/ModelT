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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.QualifierProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


/**
 * Dummy qualifier provider for the cases where no qualifiers are required.
 */
public class NoOpQualifierProvider implements QualifierProvider
{
	@Override
	public Set<Class<?>> getSupportedTypes()
	{
		return Collections.emptySet();
	}

	@Override
	public Collection<Qualifier> getAvailableQualifiers(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean canApply(final IndexedProperty indexedProperty)
	{
		return false;
	}

	@Override
	public void applyQualifier(final Qualifier qualifier)
	{
		// do nothing
	}

	@Override
	public Qualifier getCurrentQualifier()
	{
		return null;
	}
}
