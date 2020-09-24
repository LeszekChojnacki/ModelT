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
package de.hybris.platform.ruleengine.dao.interceptors;

import static org.apache.commons.lang.BooleanUtils.isFalse;

import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.RuleModelRemoveHandler;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

import org.springframework.beans.factory.annotation.Required;


/**
 * The remove interceptor implementation for DroolsRuleModel items
 */
public class RuleEngineRuleRemoveInterceptor implements RemoveInterceptor<DroolsRuleModel>
{

	private RuleModelRemoveHandler ruleModelRemoveHandler;

	@Override
	public void onRemove(final DroolsRuleModel droolsRule, final InterceptorContext ctx) throws InterceptorException
	{
		if (isFalse(droolsRule.getCurrentVersion()))
		{
			throw new InterceptorException("Cannot remove " + droolsRule + ". It is not a current rule version!", this);
		}
		else
		{
			getRuleModelRemoveHandler().handleOnRemove(droolsRule, ctx);
		}
	}

	protected RuleModelRemoveHandler getRuleModelRemoveHandler()
	{
		return ruleModelRemoveHandler;
	}

	@Required
	public void setRuleModelRemoveHandler(final RuleModelRemoveHandler ruleModelRemoveHandler)
	{
		this.ruleModelRemoveHandler = ruleModelRemoveHandler;
	}



}
