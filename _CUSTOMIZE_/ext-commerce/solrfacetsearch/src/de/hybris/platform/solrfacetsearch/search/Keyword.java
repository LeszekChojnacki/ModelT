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
package de.hybris.platform.solrfacetsearch.search;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;


/**
 * Class representing a keyword.
 */
public class Keyword implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String value;
	private final Collection<KeywordModifier> modifiers;

	/**
	 * Creates a new keyword.
	 *
	 * @param value
	 *           - the value
	 * @param modifiers
	 *           - the modifiers
	 */
	public Keyword(final String value, final KeywordModifier... modifiers)
	{
		this.value = value;
		this.modifiers = Arrays.asList(modifiers);
	}

	/**
	 * Returns the value of the keyword.
	 *
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Returns the modifiers of the keyword.
	 *
	 * @return the modifiers
	 */
	public Collection<KeywordModifier> getModifiers()
	{
		return modifiers;
	}
}
