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
package de.hybris.platform.solrfacetsearch.config.cache;

import java.util.Set;


public class FacetSearchConfigInvalidationTypeSet
{
	final Set<String> invalidationTypes;

	public FacetSearchConfigInvalidationTypeSet(final Set<String> invalidationTypes)
	{
		super();
		this.invalidationTypes = invalidationTypes;
	}

	public Set<String> getInvalidationTypes()
	{
		return invalidationTypes;
	}
}
