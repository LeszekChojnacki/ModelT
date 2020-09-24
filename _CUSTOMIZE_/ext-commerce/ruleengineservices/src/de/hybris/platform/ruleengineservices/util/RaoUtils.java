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
package de.hybris.platform.ruleengineservices.util;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;


import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

import de.hybris.platform.ruleengineservices.rao.AbstractActionedRAO;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;


/**
 * The class provides some utility methods for Rule Aware Objects (that are generated at
 * de.hybris.platform.ruleengineservices.rao package).
 *
 *
 */
public class RaoUtils
{
	private static final String ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE = "actionedRao object is not expected to be NULL here";
	/**
	 * Returns ordered set of Discounts of an ActionedRAO (filters out it actions getting only DiscountRAO).
	 */
	public Set<DiscountRAO> getDiscounts(final AbstractActionedRAO actionedRao)
	{
		Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);

		final Set<DiscountRAO> result = new LinkedHashSet();
		if (isNotEmpty(actionedRao.getActions()))
		{
			actionedRao.getActions().stream().filter(action -> action instanceof DiscountRAO)
					.forEachOrdered(action -> result.add((DiscountRAO) action));
		}
		return result;
	}

	/**
	 * Finds Delivery of an actionedRao Item (Order, Product, etc.) if any.
	 */
	public Optional<ShipmentRAO> getShipment(final AbstractActionedRAO actionedRao)
	{
		Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);

		Optional<ShipmentRAO> shipmentRao = empty();
		if (isNotEmpty(actionedRao.getActions()))
		{
			shipmentRao = actionedRao.getActions().stream().filter(action -> action instanceof ShipmentRAO)
					.map(action -> (ShipmentRAO) action).findFirst();
		}
		return shipmentRao;
	}

	/**
	 * Finds out if the Discount is absolute or not
	 */
	public boolean isAbsolute(final DiscountRAO discount)
	{
		return isNotEmpty(discount.getCurrencyIsoCode());
	}

	/**
	 * Sets references between {@code action} and {@code actionedRao}.
	 *
	 * @param actionedRao
	 * @param action
	 */
	public void addAction(final AbstractActionedRAO actionedRao, final AbstractRuleActionRAO action)
	{
		Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);
		Preconditions.checkNotNull(action, "actionRao object is not expected to be NULL here");

		action.setAppliedToObject(actionedRao);
		LinkedHashSet<AbstractRuleActionRAO> actions = actionedRao.getActions();
		if (isNull(actions))
		{
			actions = new LinkedHashSet<>();
			actionedRao.setActions(actions);
		}
		actions.add(action);
	}
}
