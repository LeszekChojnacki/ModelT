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
package de.hybris.platform.ruleengineservices.impex.impl;

import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.impex.jalo.header.HeaderDescriptor;
import de.hybris.platform.impex.jalo.imp.DefaultImportProcessor;
import de.hybris.platform.impex.jalo.imp.ImpExImportReader;
import de.hybris.platform.impex.jalo.imp.ValueLine;
import de.hybris.platform.jalo.Item;

import java.util.Collections;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Impex ImportProcessor that loosens strict existing item validation performed by {@link DefaultImportProcessor}.
 * Conditional logic relies on provided {@link Predicate<ValueLine>} implementation that should decide whether a given line
 * should be considered for import, or omitted.
 * In order to use this import processor the 'processor' and 'condition' header descriptor modifiers should be specified, e.g.:
 *
 * <tt>INSERT_UPDATE PromotionSourceRule[processor=de.hybris.platform.ruleengineservices.impex.impl.ConditionalImportProcessor,condition=de.hybris.platform.ruleengineservices.impex.impl.RuleImportCondition];code[unique=true]</tt>
 */
public class ConditionalImportProcessor extends DefaultImportProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(ConditionalImportProcessor.class);

	private Predicate<ValueLine> importProcessorCondition;

	@Override
	public void init(final ImpExImportReader reader)
	{
		super.init(reader);

		try
		{
			this.importProcessorCondition = getImportProcessorCondition(getReader().getCurrentHeader());
		}
		catch (final ImpExException exc)
		{
			LOG.error("Cannot use conditional import processor", exc);
			throw new IllegalStateException("Cannot use conditional import processor - provided condition class is invalid", exc);
		}
	}

	@Override
	public Item processItemData_Impl(final ValueLine valueLine) throws ImpExException
	{
		try
		{
			adjustSessionSettings();

			if (getImportProcessorCondition().test(valueLine))
			{
				return super.processItemData_Impl(valueLine);
			}
			else
			{
				valueLine.resolve((Item) null, Collections.emptyList());
			}
		}
		finally
		{
			restoreSessionSettings();
		}

		return null;
	}

	protected Predicate<ValueLine> getImportProcessorCondition(final HeaderDescriptor header) throws ImpExException
	{
		final String condition = header.getDescriptorData().getModifier("condition");

		try
		{
			final Class<Predicate<ValueLine>> clazz = (Class<Predicate<ValueLine>>) Class.forName(condition);
			return clazz.newInstance();
		}
		catch (final Exception exc)
		{
			throw new ImpExException(exc, "Can not instantiate Processor Condition [" + condition + "] because: "
					+ exc.getMessage(), 0);
		}
	}

	protected Predicate<ValueLine> getImportProcessorCondition()
	{
		return importProcessorCondition;
	}
}
