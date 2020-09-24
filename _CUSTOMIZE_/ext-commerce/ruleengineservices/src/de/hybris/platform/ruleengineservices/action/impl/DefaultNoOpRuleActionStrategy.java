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
package de.hybris.platform.ruleengineservices.action.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;


/**
 * The DefaultNoOpRuleActionStrategy doesn't do anything.
 */
public class DefaultNoOpRuleActionStrategy implements RuleActionStrategy<ItemModel>, BeanNameAware
{
	private String beanName;

	@Override
	public List<ItemModel> apply(final AbstractRuleActionRAO action)
	{
		// NoOp as in no operation.
		return Collections.emptyList();
	}

	@Override
	public void undo(final ItemModel action)
	{
		// NoOp as in no operation.
	}

	@Override
	public String getStrategyId()
	{
		return beanName;
	}

	@Override
	public void setBeanName(final String beanName)
	{
		this.beanName = beanName;
	}

}
