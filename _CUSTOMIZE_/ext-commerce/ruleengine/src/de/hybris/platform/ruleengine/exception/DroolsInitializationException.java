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
package de.hybris.platform.ruleengine.exception;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.ruleengine.ResultItem;

import java.util.Collection;

import com.google.common.base.Joiner;


/**
 * DroolsInitializationException is used only internally to propagate initialization errors
 */
public class DroolsInitializationException extends RuntimeException
{
	private final Collection<ResultItem> results;

	public DroolsInitializationException(final String message)
	{
		super(message);
		this.results = newArrayList();
	}

	public DroolsInitializationException(final String message, final Throwable cause)
	{
		super(message, cause);
		this.results = newArrayList();
	}

	/**
	 * the results from the building process
	 *
	 * @param results
	 * 			 the collection of {@link ResultItem}
	 * @param cause
	 * 			 an optional cause
	 */
	public DroolsInitializationException(final Collection<ResultItem> results, final Throwable cause)
	{
		super(cause);
		this.results = results;
	}

	/**
	 * the results from the building process
	 *
	 * @param results
	 * 			 the collection of {@link ResultItem}
	 * @param message
	 * 			 reason of the exception
	 */
	public DroolsInitializationException(final Collection<ResultItem> results, final String message)
	{
		super(message + Joiner.on(", ").join(results.stream().map(ResultItem::getMessage).collect(toList())));
		this.results = results;
	}

	/**
	 * @return the results
	 */
	public Collection<ResultItem> getResults()
	{
		return results;
	}

}
