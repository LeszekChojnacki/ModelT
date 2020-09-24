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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions.impl;

import static de.hybris.platform.ruleengineservices.util.RAOConstants.DELIVERY_MODE_PARAM;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleChangeDeliveryModeRAOAction extends AbstractRuleExecutableSupport
{
	private static final Logger LOG = LoggerFactory.getLogger(RuleChangeDeliveryModeRAOAction.class);

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final String deliveryModeCode = context.getParameter(DELIVERY_MODE_PARAM, String.class);
		return performAction(context, deliveryModeCode);
	}

	protected boolean performAction(final RuleActionContext context, final String deliveryModeCode)
	{
		boolean isPerformed = false;
		final CartRAO cartRao = context.getCartRao();

		final RuleEngineResultRAO result = context.getRuleEngineResultRao();

		final Optional<DeliveryModeRAO> deliveryMode = lookupRAOByType(DeliveryModeRAO.class, context,
				getDeliveryModeRAOFilter(deliveryModeCode));
		if (deliveryMode.isPresent())
		{
			isPerformed = true;
			changeDeliveryMode(cartRao, deliveryMode.get(), result, context);
		}
		else
		{
			LOG.error("no delivery mode found for code {} in rule {}, cannot apply rule action.", deliveryModeCode,
					getRuleCode(context));
		}
		return isPerformed;
	}

	protected Predicate<DeliveryModeRAO> getDeliveryModeRAOFilter(final String deliveryModeCode)
	{
		return o -> isFactDeliveryAndHasCode(o, deliveryModeCode);
	}

	public void changeDeliveryMode(final CartRAO cartRao, final DeliveryModeRAO mode, final RuleEngineResultRAO result,
			final RuleActionContext context)
	{
		validateRule(context);
		final ShipmentRAO shipment = getRuleEngineCalculationService().changeDeliveryMode(cartRao, mode);
		result.getActions().add(shipment);
		setRAOMetaData(context, shipment);
		context.scheduleForUpdate(cartRao, result);
		context.insertFacts(shipment);

		cartRao.getEntries().forEach(e -> consumeOrderEntry(e, shipment));
	}

	protected boolean isFactDeliveryAndHasCode(final Object fact, final String deliveryModeCode)
	{
		return fact instanceof DeliveryModeRAO && deliveryModeCode.equals(((DeliveryModeRAO) fact).getCode());
	}

}
