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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslatorFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslatorNotFoundException;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RuleSourceCodeTranslatorFactory}.
 */
public class DefaultRuleSourceCodeTranslatorFactory implements RuleSourceCodeTranslatorFactory
{
	private RuleSourceCodeTranslator sourceRuleSourceCodeTranslator;

	@Override
	public RuleSourceCodeTranslator getSourceCodeTranslator(final RuleCompilerContext context)
	{
		if (context.getRule() instanceof SourceRuleModel)
		{
			return sourceRuleSourceCodeTranslator;
		}

		throw new RuleSourceCodeTranslatorNotFoundException("Source code translator not found for rule: " + context.getRule());
	}

	public RuleSourceCodeTranslator getSourceRuleSourceCodeTranslator()
	{
		return sourceRuleSourceCodeTranslator;
	}

	@Required
	public void setSourceRuleSourceCodeTranslator(final RuleSourceCodeTranslator sourceRuleSourceCodeTranslator)
	{
		this.sourceRuleSourceCodeTranslator = sourceRuleSourceCodeTranslator;
	}
}
