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


import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.promotions.result.PromotionException;
import de.hybris.platform.util.DiscountValue;

import java.util.List;
import java.util.Map;


/**
 * AbstractPromotionAction. Base class for promotion actions. Actions represent the effect that an AbstractPromotion
 * must take to apply its promotion. If a promotion has fired then it will return as part of the PromotionResult the
 * actions that should be applied to create the promotional behaviour. Actions can be applied and undone. A promotion
 * may require multiple actions to be applied and therefore the PromotionResult holds a collection of actions and
 * applied them all. Different actions are implemented in subclasses.
 *
 *
 */
public abstract class AbstractPromotionAction extends GeneratedAbstractPromotionAction
{
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPromotionAction.class.getName());

	/**
	 * Get the Unique Identifier assigned to this action.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return the unique identifier
	 */
	@Override
	public String getGuid(final SessionContext ctx)
	{
		final String retval = super.getGuid(ctx);
		if (retval == null || retval.length() == 0)
		{
			throw new PromotionException("Action with PK:" + this.getPK() + " has a null or empty GUID");
		}
		return retval;
	}

	/**
	 * Apply the action to the order. Do not call {@link AbstractOrder#recalculate} to update the totals as this disposes
	 * of all applied discounts and the promotions engine is unable to intercept the recalculate call to reinstate these.
	 *
	 * @return <i>true</i> if discounts have been applied and calculateTotals needs to be called, <i>false</i> otherwise.
	 */
	public final boolean apply()
	{
		return apply(getSession().getSessionContext());
	}

	/**
	 * Apply the action to the order. Do not call {@link AbstractOrder#recalculate} to update the totals as this disposes
	 * of all applied discounts and the promotions engine is unable to intercept the recalculate call to reinstate these.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return <i>true</i> if discounts have been applied and calculateTotals needs to be called, <i>false</i> otherwise.
	 */
	public abstract boolean apply(final SessionContext ctx);

	/**
	 * Unapply the action from the order.
	 *
	 * @return <i>true</i> if discounts have been removed and calculateTotals needs to be called, <i>false</i> otherwise.
	 */
	public final boolean undo()
	{
		return undo(getSession().getSessionContext());
	}

	/**
	 * Unapply the action from the order.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return <i>true</i> if discounts have been removed and calculateTotals needs to be called, <i>false</i> otherwise.
	 */
	public abstract boolean undo(final SessionContext ctx);

	/**
	 * Test if this action is applied to the order.
	 *
	 * This is different from isMarkedApplied as there are many ways of removing the effect of the action other than
	 * calling the {@link #undo} method.
	 *
	 * @return <i>true</i> if the action is applied, <i>false</i> otherwise.
	 */
	public final boolean isAppliedToOrder()
	{
		return isAppliedToOrder(getSession().getSessionContext());
	}

	/**
	 * Test if this action is applied to the order.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return <i>true</i> if the action is applied, <i>false</i> otherwise.
	 */
	public abstract boolean isAppliedToOrder(final SessionContext ctx);

	/**
	 * Get the total value of this action. For actions providing a discount this should be a positive value.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return The double value that is the value of this action
	 */
	public abstract double getValue(SessionContext ctx);

	/**
	 * Deep clone this action instance.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return a clone of this instance
	 */
	protected AbstractPromotionAction deepClone(final SessionContext ctx)
	{
		try
		{
			final Map values = this.getAllAttributes(ctx);

			// Remove 'standard' values that cannot be set
			values.remove(Item.PK);
			values.remove(Item.MODIFIED_TIME);
			values.remove(Item.CREATION_TIME);
			values.remove("savedvalues");
			values.remove("customLinkQualifier");
			values.remove("synchronizedCopies");
			values.remove("synchronizationSources");
			values.remove("alldocuments");
			values.remove(Item.TYPE);
			values.remove(Item.OWNER);
			values.remove(PROMOTIONRESULT);

			// Clone subclass specific values
			deepCloneAttributes(ctx, values);

			final ComposedType type = getComposedType();
			try //NOSONAR
			{
				return (AbstractPromotionAction) type.newInstance(ctx, values);
			}
			catch (final JaloGenericCreationException | JaloAbstractTypeException ex)
			{
				log.warn("deepClone [" + this + "] failed to create instance of " + this.getClass().getSimpleName(), ex); //NOSONAR
			}
		}
		catch (final JaloSecurityException ex)
		{
			log.warn("deepClone [" + this + "] failed to getAllAttributes", ex);
		}
		return null;
	}

	/**
	 * Called to deep clone attributes of this instance.
	 *
	 * The values map contains all the attributes defined on this instance. The map will be used to initialse a new
	 * instance of the Action that is a clone of this instance. This method can remove, replace or add to the Map of
	 * attributes.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param values
	 *           The map to write into
	 */
	protected void deepCloneAttributes(final SessionContext ctx, final Map values)
	{
		// DOCTODO Document reason, why this block is empty
	}

	/**
	 * Add a global DiscountValue to the Order at the first position in the list of discounts
	 *
	 * @param ctx
	 *           The hybris context
	 * @param order
	 *           The order
	 * @param discountValue
	 *           The discount to add
	 */
	protected static void insertFirstGlobalDiscountValue(final SessionContext ctx, final AbstractOrder order,
			final DiscountValue discountValue)
	{
		// Get the full list of global discount values
		final List<DiscountValue> list = order.getGlobalDiscountValues(ctx); // NOSONAR

		// Remove all the current discount values
		order.removeAllGlobalDiscountValues(ctx); // NOSONAR

		// Add the discount value at the first position
		order.addGlobalDiscountValue(ctx, discountValue); // NOSONAR

		// Add the original discount values back to the list
		if (list != null)
		{
			order.addAllGlobalDiscountValues(ctx, list); // NOSONAR
		}
	}

	/**
	 * Add a DiscountValue to the OrderEntry at the first position in the list of discounts
	 *
	 * @param ctx
	 *           The hybris context
	 * @param orderEntry
	 *           The order
	 * @param discountValue
	 *           The discount to add
	 */
	protected static void insertFirstOrderEntryDiscountValue(final SessionContext ctx, final AbstractOrderEntry orderEntry,
			final DiscountValue discountValue)
	{
		// Get the full list of global discount values
		final List<DiscountValue> list = orderEntry.getDiscountValues(ctx); // NOSONAR

		// Remove all the current discount values
		orderEntry.removeAllDiscountValues(ctx); // NOSONAR

		// Add the discount value at the first position
		orderEntry.addDiscountValue(ctx, discountValue); // NOSONAR

		// Add the original discount values back to the list
		if (list != null)
		{
			orderEntry.addAllDiscountValues(ctx, list); // NOSONAR
		}
	}

}
