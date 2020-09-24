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

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerListener;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link RuleCompilerListener} that adds rule version to the context.
 */
public class DefaultRuleCompilerListener implements RuleCompilerListener
{
	private EngineRuleDao engineRuleDao;

	@Override
	public void beforeCompile(final RuleCompilerContext context)
	{
		// NOOP
	}

	@Override
	public void afterCompile(final RuleCompilerContext context)
	{
		final AbstractRuleEngineRuleModel engineRule = getEngineRuleDao().getRuleByCode(context.getRule().getCode(),
				context.getModuleName());

		context.setRuleVersion(engineRule.getVersion());
	}

	@Override
	public void afterCompileError(final RuleCompilerContext context)
	{
		// NOOP
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}
}
