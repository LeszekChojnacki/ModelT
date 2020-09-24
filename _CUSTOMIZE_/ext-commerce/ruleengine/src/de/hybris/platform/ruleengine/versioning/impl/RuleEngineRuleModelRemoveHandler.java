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
package de.hybris.platform.ruleengine.versioning.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.RuleModelRemoveHandler;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;

import org.springframework.beans.factory.annotation.Required;


public class RuleEngineRuleModelRemoveHandler implements RuleModelRemoveHandler
{

	private EngineRuleDao engineRuleDao;

	@Override
	public void handleOnRemove(final AbstractRuleEngineRuleModel rule, final InterceptorContext context)
	{
		previousVersionExistsMakeItActive(rule, context);
	}

	protected void previousVersionExistsMakeItActive(final AbstractRuleEngineRuleModel ruleEngineRule, final InterceptorContext ctx)
	{
		checkArgument(nonNull(ruleEngineRule), "The rule engine rule should not be null here");
		checkArgument(ruleEngineRule instanceof DroolsRuleModel, "The rule must be an instance of DroolsRule");

		final DroolsRuleModel droolsRule = (DroolsRuleModel)ruleEngineRule;
		final DroolsKIEModuleModel module = droolsRule.getKieBase().getKieModule();

		final long version = ruleEngineRule.getVersion() - 1;
		if (version >= 0)
		{

			final AbstractRuleEngineRuleModel rule = getEngineRuleDao().getRuleByCodeAndMaxVersion(ruleEngineRule.getCode(), module.getName(),
					ruleEngineRule.getVersion() - 1);
			if (nonNull(rule))
			{
				rule.setCurrentVersion(Boolean.TRUE);
				ctx.registerElementFor(ruleEngineRule, PersistenceOperation.DELETE);
				ctx.registerElementFor(rule, PersistenceOperation.SAVE);
			}
		}
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
