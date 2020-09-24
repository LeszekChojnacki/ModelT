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

import de.hybris.platform.solrfacetsearch.search.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Default implementation of {@link Document}.
 */
public class DefaultDocument implements Document, Serializable
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> fields;
	private final Set<String> tags;

	/**
	 * Creates an empty document.
	 */
	public DefaultDocument()
	{
		fields = new HashMap<>();
		tags = new HashSet<>();
	}

	@Override
	public Collection<String> getFieldNames()
	{
		return getFields().keySet();
	}

	@Override
	public Object getFieldValue(final String fieldName)
	{
		return getFields().get(fieldName);
	}

	@Override
	public Map<String, Object> getFields()
	{
		return fields;
	}

	@Override
	public Set<String> getTags()
	{
		return tags;
	}
}
