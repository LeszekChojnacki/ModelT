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
package com.hybris.backoffice.excel.template;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;


/**
 * Handles collection format of elements that are being put to a single Excel cell.
 */
public interface CollectionFormatter
{
	/**
	 * By default converts the passed elements to collection and calls {@link #formatToString(Collection)}.
	 * 
	 * @see #formatToString(Collection)
	 */
	default String formatToString(@Nonnull final String... elements)
	{
		return formatToString(Arrays.asList(elements));
	}

	/**
	 * Formats given collection to a string that can be put to a single Excel cell.
	 * 
	 * @param collection
	 *           of elements that are intended to be put in a single Excel cell
	 * @return collection representation ready to be put to a single Excel cell
	 */
	String formatToString(@Nonnull Collection<String> collection);

	/**
	 * Extracts the collection from a single Excel cell to a collection.
	 * 
	 * @param string
	 *           formatted collection from a single Excel cell
	 * @return collection of extracted elements
	 */
	Set<String> formatToCollection(@Nonnull String string);
}
