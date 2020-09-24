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
package de.hybris.platform.promotionengineservices.jalo;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.util.Config;


/**
 * Provides a base implementation for rule-based promotion actions.
 */
public abstract class AbstractRuleBasedPromotionAction extends GeneratedAbstractRuleBasedPromotionAction
{
	@Override
	public boolean apply(final SessionContext ctx)
	{
		checkJaloCall("apply()");
		return false;
	}

	@Override
	public boolean undo(final SessionContext ctx)
	{
		checkJaloCall("undo()");
		return false;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		checkJaloCall("isAppliedToOrder()");
		return false;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		checkJaloCall("getValue()");
		return 0;
	}

	protected void checkJaloCall(final String methodName)
	{
		if (Config.getBoolean("promotionengineservices.prohibit.abstractpromotionaction.jalo.calls", false))
		{
			throw new IllegalStateException("calling RuleBasedPromotionAction." + methodName
					+ " is not allowed as it is incompatible with promotionengineservices extension. You can disable this behavior by setting promotionengineservices.prohibit.abstractpromotionaction.jalo.calls=false.");
		}

	}
}
