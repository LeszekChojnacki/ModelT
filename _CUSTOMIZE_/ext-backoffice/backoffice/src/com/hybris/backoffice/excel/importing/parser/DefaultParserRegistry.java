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
package com.hybris.backoffice.excel.importing.parser;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;


/**
 * Default implementation of {@link ParserRegistry}
 */
public class DefaultParserRegistry implements ParserRegistry
{

	private List<ImportParameterParser> parsers;

	/**
	 * {@inheritDoc}
	 */
	public ImportParameterParser getParser(@Nonnull final String referenceFormat)
	{
		return parsers.stream() //
				.filter(parser -> parser.matches(referenceFormat)) //
				.findFirst() //
				.orElseThrow(() -> new RuntimeException("There's no parser for given value!"));
	}

	@Required
	public void setParsers(final List<ImportParameterParser> parsers)
	{
		this.parsers = parsers;
		OrderComparator.sort(this.parsers);
	}
}
