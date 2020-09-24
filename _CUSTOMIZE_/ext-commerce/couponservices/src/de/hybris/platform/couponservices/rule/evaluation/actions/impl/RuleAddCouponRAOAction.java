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
package de.hybris.platform.couponservices.rule.evaluation.actions.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.rao.AddCouponRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.RAOConstants;


/**
 * Rule for granting a coupon to a customer
 */
public class RuleAddCouponRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final String couponId = (String) context.getParameter(RAOConstants.VALUE_PARAM);
		addCoupon(context, couponId);
		return true;
	}

	protected void addCoupon(final RuleActionContext context, final String couponId)
	{
		checkArgument(nonNull(couponId), "Coupon ID argument must not be empty");

		final AddCouponRAO addCouponRao = new AddCouponRAO();
		addCouponRao.setCouponId(couponId);
		final CartRAO cart = context.getValue(CartRAO.class);
		getRaoUtils().addAction(cart, addCouponRao);
		final RuleEngineResultRAO result = context.getRuleEngineResultRao();
		result.getActions().add(addCouponRao);
		setRAOMetaData(context, addCouponRao);
		context.scheduleForUpdate(cart, result);
		context.insertFacts(addCouponRao);
	}

}
