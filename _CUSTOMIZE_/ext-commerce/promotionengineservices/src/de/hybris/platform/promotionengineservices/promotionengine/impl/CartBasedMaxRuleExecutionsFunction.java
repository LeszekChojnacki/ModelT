/*
 * [y] hybris Platform
 *
 * Copyright (c) 2019 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.promotionengineservices.promotionengine.impl;

 import static java.util.Objects.nonNull;

 import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengineservices.rao.CartRAO;

 import java.util.Optional;
import java.util.function.Function;

 import org.springframework.beans.factory.annotation.Required;


 /**
 * Function that returns a number that represents maximum rule executions during a single evaluation of a context with
 * {@link CartRAO} present
 */
public class CartBasedMaxRuleExecutionsFunction implements Function<RuleEvaluationContext, Integer>
{
	private int minExecutions;

 	@Override
	public Integer apply(final RuleEvaluationContext context)
	{
		final Optional<CartRAO> cartRAO =
				context.getFacts().stream().filter(CartRAO.class::isInstance).findFirst().map(CartRAO.class::cast);

 		return cartRAO.isPresent() && nonNull(cartRAO.get().getEntries())
				? (cartRAO.get().getEntries().stream().mapToInt(e -> e.getQuantity()).sum() + getMinExecutions())
				: getMinExecutions();
	}

 	protected int getMinExecutions()
	{
		return minExecutions;
	}

 	@Required
	public void setMinExecutions(final int minExecutions)
	{
		this.minExecutions = minExecutions;
	}
}