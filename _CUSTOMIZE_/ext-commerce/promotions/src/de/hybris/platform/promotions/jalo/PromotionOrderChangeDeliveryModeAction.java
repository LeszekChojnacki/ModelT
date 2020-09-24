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
package de.hybris.platform.promotions.jalo;


import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.delivery.JaloDeliveryModeException;
import de.hybris.platform.util.Config;

import org.apache.log4j.Logger;


/**
 * PromotionOrderChangeDeliveryModeAction. Action to change the order delivery mode on the order to the specified value.
 * Applying this action causes the delivery mode on the order to be changed to the specified delivery mode. The original
 * delivery mode is stored on the order in the PreviousDeliveryMode attribute. Undoing this action restores the
 * PreviousDeliveryMode back onto the order.
 *
 * If the delivery cost should be updated, please set the <b>orderThresholdChangeDeliveryMode.updateDeliveryCost</b> to
 * <code>true</code> in the project.properties. This is valid for 4.4 or later versions.
 *
 */
public class PromotionOrderChangeDeliveryModeAction extends GeneratedPromotionOrderChangeDeliveryModeAction //NOSONAR
{

	private static final Logger LOG = Logger.getLogger(PromotionOrderChangeDeliveryModeAction.class);

	@Override
	public boolean apply(final SessionContext ctx)
	{
		boolean calculate = false;

		if (!isMarkedAppliedAsPrimitive(ctx))
		{
			final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);
			if (order != null)
			{
				// The promotion order delivery mode may be null
				final DeliveryMode orderDeliveryMode = order.getDeliveryMode(ctx);
				final PromotionsManager pm = PromotionsManager.getInstance();
				final DeliveryMode newDeliveryMode = this.getDeliveryMode(ctx);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("(" + getPK() + ") apply: Applying ChangeDeliveryMode action. orderDeliveryMode=["
							+ deliveryModeToString(ctx, orderDeliveryMode) + "] newDeliveryMode=["
							+ deliveryModeToString(ctx, newDeliveryMode) + "]");
				}

				pm.setPreviousDeliveryMode(ctx, order, orderDeliveryMode);
				order.setDeliveryMode(newDeliveryMode);

				//PRO-78
				updateDeliveryCost(order, newDeliveryMode);

				calculate = true;

				setMarkedApplied(ctx, true);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("(" + getPK() + ") apply: After apply orderDeliveryMode=["
							+ deliveryModeToString(ctx, order.getDeliveryMode(ctx)) + "]");
				}
			}
		}
		return calculate;
	}

	@Override
	public boolean undo(final SessionContext ctx)
	{
		boolean calculate = false;

		if (isMarkedAppliedAsPrimitive(ctx))
		{
			final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);
			if (order != null)
			{
				final DeliveryMode orderDeliveryMode = order.getDeliveryMode(ctx);
				final PromotionsManager pm = PromotionsManager.getInstance();
				final DeliveryMode previousDeliveryMode = pm.getPreviousDeliveryMode(ctx, order);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("(" + getPK() + ") undo: Undoing ChangeDeliveryMode action. orderDeliveryMode=["
							+ deliveryModeToString(ctx, orderDeliveryMode) + "] previousDeliveryMode=["
							+ deliveryModeToString(ctx, previousDeliveryMode) + "]");
				}

				order.setDeliveryMode(ctx, previousDeliveryMode);
				pm.setPreviousDeliveryMode(ctx, order, null);
				//PRO-128
				updateDeliveryCost(order, previousDeliveryMode);

				calculate = true;

				setMarkedApplied(ctx, false);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("(" + getPK() + ") undo: After undo orderDeliveryMode=["
							+ deliveryModeToString(ctx, order.getDeliveryMode(ctx)) + "]");
				}
			}
		}

		return calculate;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		return 0.0D;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		if (order != null)
		{
			// The previous delivery mode could be null if the original order delivery mode was null at the
			// time when this promotion was applied
			final DeliveryMode orderDeliveryMode = order.getDeliveryMode(ctx);
			final DeliveryMode newDeliveryMode = this.getDeliveryMode(ctx);

			if (orderDeliveryMode != null && orderDeliveryMode.equals(newDeliveryMode))
			{
				return true;
			}
		}
		return false;
	}

	protected String deliveryModeToString(final SessionContext ctx, final DeliveryMode deliveryMode)
	{
		if (deliveryMode == null)
		{
			return "(null)";
		}
		return deliveryMode.getClass().getSimpleName() + " '" + deliveryMode.getCode(ctx) + "' ("
				+ deliveryMode.getPK().getLongValueAsString() + ")";
	}

	protected void updateDeliveryCost(final AbstractOrder order, final DeliveryMode deliveryMode)
	{
		if (Boolean.parseBoolean(Config.getParameter("orderThresholdChangeDeliveryMode.updateDeliveryCost")))
		{
			try
			{
				final double deliveryCost = deliveryMode.getCost(order).getValue();
				order.setDeliveryCosts(deliveryCost);
			}
			catch (final JaloDeliveryModeException e)
			{
				LOG.error("Delivery mode error for mode " + deliveryMode.getCode(), e);
			}
		}
	}

}
