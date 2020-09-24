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

import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.promotions.jalo.PromotionGroup;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.result.PromotionEvaluationContext;
import de.hybris.platform.util.Config;

import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class RuleBasedPromotion extends GeneratedRuleBasedPromotion
{

	@Override
	public List<PromotionResult> evaluate(final SessionContext ctx, final PromotionEvaluationContext promoContext)
	{
		checkJaloCall("evaluate()");
		return Collections.emptyList();
	}

	@Override
	public String getResultDescription(final SessionContext ctx, final PromotionResult promotionResult, final Locale locale)
	{
		checkJaloCall("getResultDescription()");
		return "not supported";
	}

	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove all of our owned restrictions
		setRestrictions(ctx, Collections.emptyList());

		// then remove this item
		super.remove(ctx);
	}

	@Override
	public void setPromotionGroup(final SessionContext ctx, final PromotionGroup promotionGroup)
	{
		if (promotionGroup == null)
		{
			throw new JaloInvalidParameterException("Cannot set promotionGroup to NULL", 999);
		}
		super.setPromotionGroup(ctx, promotionGroup);
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
