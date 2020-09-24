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

import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloAbstractTypeException;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.jalo.type.TypeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.log4j.Logger;


/**
 * PromotionResult.
 * <p/>
 * The result of evaluating a promotion against a cart or order is a set of promotion results. Each promotion my produce
 * zero or more results to express the results of its evaluation. A {@link PromotionResult} is associated with a single
 * {@link AbstractPromotion} and is either fired ({@link #getFired()}) or potentially could fire (
 * {@link #getCouldFire()}).
 * <p/>
 * A result that has fired as met all the requirements of the promotion. A result that potentially could fire has not
 * met all the requirements of the promotion. When the result could fire the promotion also assigns a certainty (
 * {@link #getCertainty()}) value to the result to indicate how close the result is to firing. This is a value in the
 * range 0 to 1, where 1 indicates that the promotion has fired. This value can be used to rank potential promotion
 * results.
 * <p/>
 * A promotion result holds a number of {@link PromotionOrderEntryConsumed} instances to represent the entries in the
 * order that have been consumed by the promotion in generating this result. If the promotion has fired then these
 * consumed entries are not available to other promotions. If the promotion has not fired then these consumed entries
 * are just an indication of the entries that will be consumed when the promotion can fire.
 * <p/>
 * If the promotion has fired the promotion result also holds a number of {@link AbstractPromotionAction} instances to
 * represent the actions that the promotion takes. These actions are either applied or not ({@link #isApplied()}). When
 * the promotions are evaluated by
 * {@link PromotionsManager#updatePromotions(SessionContext, Collection, de.hybris.platform.jalo.order.AbstractOrder, boolean, de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode, de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode, java.util.Date)}
 * a firing promotion result may be automatically applied depending on the parameters passed to the method. The actions
 * of a specific promotion result can be applied by calling the {@link #apply()} method.
 * <p/>
 * The actions of a specific promotion result can be reversed by calling the {@link #undo()} method.
 *
 *
 */
public class PromotionResult extends GeneratedPromotionResult //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(PromotionResult.class);

	/**
	 * Removes the item. When this item is removed the associated consumed order entries and actions will also be
	 * removed.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove all of our owned actions and consumed order entries
		setConsumedEntries(ctx, Collections.emptyList());
		setActions(ctx, Collections.emptyList());

		// then remove the item
		super.remove(ctx);
	}

	/**
	 * Returns <i>true</i> if the promotion fired and all of its actions have been applied to the order.
	 *
	 * @return Whether the promotion has been applied
	 */
	public final boolean isApplied()
	{
		return isApplied(getSession().getSessionContext());
	}

	/**
	 * Returns <i>true</i> if the promotion fired and all of its actions have been applied to the order.
	 *
	 * This method just checks that all the actions have been asked to apply themselves to the order.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return Whether the promotion has been applied
	 */
	public boolean isApplied(final SessionContext ctx)
	{
		// Cannot be applied if the promotion result is not fired
		if (getFired(ctx))
		{
			final Collection<AbstractPromotionAction> actions = getActions(ctx);
			if (actions != null && !actions.isEmpty())
			{
				for (final AbstractPromotionAction action : actions)
				{
					if (!action.isMarkedApplied(ctx).booleanValue()) //NOSONAR
					{
						// If any action is not applied then, return false
						return false;
					}
				}

				// All actions are applied
				return true;
			}
		}

		// Either not fired or has no actions
		return false;
	}

	/**
	 * Returns <i>true</i> if the promotion fired and all of its actions have been applied to the order.
	 *
	 * This method checks that all the actions are still applied to the order.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return Whether the promotion has been applied
	 */
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		// Cannot be applied if the promotion result is not fired
		if (getFired(ctx))
		{
			final Collection<AbstractPromotionAction> actions = getActions(ctx);
			if (actions != null && !actions.isEmpty())
			{
				for (final AbstractPromotionAction action : actions)
				{
					if (!action.isAppliedToOrder(ctx)) //NOSONAR
					{
						// If any action is not applied then, return false
						return false;
					}
				}

				// All actions are applied
				return true;
			}
		}

		// Either not fired or has no actions
		return false;
	}

	/**
	 * Returns <i>true</i> if the promotion fired and has produced a result.
	 *
	 * @return Whether the promotion fired
	 */
	public final boolean getFired() // NOPMD
	{
		return getFired(getSession().getSessionContext());
	}

	/**
	 * Returns <i>true</i> if the promotion fired and has produced a result.
	 * <p/>
	 * When the certainty is 1 the promotion has fired.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return Whether the promotion fired
	 */
	public boolean getFired(final SessionContext ctx)
	{
		return getCertainty(ctx).floatValue() >= 1.0f;
	}

	/**
	 * Is this a potential result.
	 *
	 * Returns <i>true</i> if the promotion believes it has a chance of firing, for instance if it requires 3 qualifying
	 * products but can only find 1.
	 *
	 * @return Whether the promotion could fire
	 */
	public final boolean getCouldFire() // NOPMD
	{
		return getCouldFire(getSession().getSessionContext());
	}

	/**
	 * Is this a potential result.
	 *
	 * Returns <i>true</i> if the promotion believes it has a chance of firing, for instance if it requires 3 qualifying
	 * products but can only find 1.
	 * <p/>
	 * When the certainty is less than 1 the promotion has not fired.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return Whether the promotion could fire
	 */
	public boolean getCouldFire(final SessionContext ctx)
	{
		final float certainty = getCertainty(ctx).floatValue();
		return certainty < 1.0f;
	}

	/**
	 * Gets the description of this promotion result.
	 *
	 * This method uses the default locale (Locale.getDefault())
	 *
	 * @return A description of the promotion result
	 *
	 * @see #getDescription(java.util.Locale)
	 */
	public final String getDescription()
	{
		return getDescription(getSession().getSessionContext(), null);
	}

	/**
	 * Gets the description of this promotion result.
	 *
	 * Gets the description for this promotion result. This description is based on the state of the result, the
	 * promotion that generated the result and the user supplied formatting strings.
	 * <p/>
	 * The {@link Locale} specified is used to format any numbers, dates or currencies for display to the user. It is
	 * important that this locale best represents the formatting options appropriate for display to the user. The default
	 * currency for the locale is ignored. The currency is always explicitly taken from the
	 * {@link de.hybris.platform.jalo.order.AbstractOrder#getCurrency()}. The currency is then formatted appropriately in
	 * the locale specified. For example, this does mean that values in the EURO currency will be formatted differently
	 * depending on the locale specified as each locale can specify currency specific formatting.
	 * <p/>
	 * The currency formatting is part of the Java VM configuration to support multiple locales. If the formatting is
	 * incorrect check your VM configuration for the locale and currency combination.
	 *
	 * @param locale
	 *           The locale to use to format the messages. This locale must support currency formatting, i.e. this should
	 *           be a region specific local. e.g de_DE, en_US, en_GB
	 * @return A description of the promotion result
	 */
	public final String getDescription(final Locale locale)
	{
		return getDescription(getSession().getSessionContext(), locale);
	}

	/**
	 * Gets the description of this promotion result.
	 *
	 * Gets the description for this promotion result. This description is based on the state of the result, the
	 * promotion that generated the result and the user supplied formatting strings.
	 * <p/>
	 * The {@link Locale} specified is used to format any numbers, dates or currencies for display to the user. It is
	 * important that this locale best represents the formatting options appropriate for display to the user. The default
	 * currency for the locale is ignored. The currency is always explicitly taken from the
	 * {@link de.hybris.platform.jalo.order.AbstractOrder#getCurrency()}. The currency is then formatted appropriately in
	 * the locale specified. For example, this does mean that values in the EURO currency will be formatted differently
	 * depending on the locale specified as each locale can specify currency specific formatting.
	 * <p/>
	 * The currency formatting is part of the Java VM configuration to support multiple locales. If the formatting is
	 * incorrect check your VM configuration for the locale and currency combination.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param locale
	 *           The locale to use to format the messages. This locale must support currency formatting, i.e. this must
	 *           be a region specific local. e.g de_DE, en_US, en_GB
	 * @return A description of the promotion result
	 */
	public String getDescription(final SessionContext ctx, Locale locale)
	{
		if (locale == null)
		{
			// Cannot use ctx.getLanguage().getLocale() here as this will only return
			// a neutral locale and we prefer to have a region specified locale that
			// supports currency formatting.
			locale = Locale.getDefault(); //NOSONAR
		}

		// The local specified should be region specific rather than region neutral.
		// Because we use the cart/order's currency to lookup and specify the currency
		// we will always get a currency string, however the symbols used and the number
		// formatting may not be correct unless a region specific locale is used.

		final AbstractPromotion promotion = getPromotion(ctx);
		if (promotion != null)
		{
			return promotion.getResultDescription(ctx, this, locale);
		}

		// Missing promotion object
		return "";
	}

	/**
	 * Applies all of the actions that this promotion generated to the order.
	 *
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	public final boolean apply()
	{
		return apply(getSession().getSessionContext());
	}

	/**
	 * Applies all of the actions that this promotion generated to the order.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	public boolean apply(final SessionContext ctx)
	{
		boolean needsCalculate = false;
		if (getFired(ctx))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("(" + getPK() + ") apply: Applying actions for promotion result (" + getPK().getLongValueAsString() + ")");
			}

			final Collection<AbstractPromotionAction> actions = getActions(ctx);
			if (actions != null)
			{
				for (final AbstractPromotionAction action : actions)
				{
					if (!action.isMarkedApplied(ctx).booleanValue()) //NOSONAR
					{
						needsCalculate |= action.apply(ctx);
					}
				}
			}
		}
		return needsCalculate;
	}

	/**
	 * Undoes all of the changes that this promotion made to the order.
	 *
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	public final boolean undo()
	{
		return undo(getSession().getSessionContext());
	}

	/**
	 * Undoes all of the changes that this promotion made to the order.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	public boolean undo(final SessionContext ctx)
	{
		boolean needsCalculate = false;
		if (getFired(ctx))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("(" + getPK() + ") undo: Undoing actions for promotion result (" + getPK().getLongValueAsString() + ")");
			}

			final Collection<AbstractPromotionAction> actions = getActions(ctx);
			if (actions != null)
			{
				for (final AbstractPromotionAction action : actions)
				{
					if (action.isMarkedApplied(ctx).booleanValue()) //NOSONAR
					{
						needsCalculate |= action.undo(ctx);
					}
				}
			}
		}
		return needsCalculate;
	}

	/**
	 * Gets the total number of items consumed by this promotion.
	 *
	 * @param includeCouldFirePromotions
	 *           include could fire promotions
	 * @return The total number of items consumed
	 */
	public final long getConsumedCount(final boolean includeCouldFirePromotions)
	{
		return getConsumedCount(getSession().getSessionContext(), includeCouldFirePromotions);
	}

	/**
	 * Gets the total number of items consumed by this promotion.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param includeCouldFirePromotions
	 *           include could fire promotions
	 * @return The total number of items consumed
	 */
	public long getConsumedCount(final SessionContext ctx, final boolean includeCouldFirePromotions)
	{
		long count = 0;
		for (final PromotionOrderEntryConsumed poec : (Collection<PromotionOrderEntryConsumed>) this.getConsumedEntries(ctx))
		{
			if (includeCouldFirePromotions || poec.isRemovedFromOrder())
			{
				count += poec.getQuantity(ctx).longValue();
			}
		}
		return count;
	}

	/**
	 * Gets the total value of all discounts in this result. This result will be the same regardless of the applied state
	 * of this result, i.e. if not applied this is the discount value that would be applied, if it is applied then it is
	 * the value of the discount.
	 *
	 * @return The double value for the total discount value
	 */
	public final double getTotalDiscount()
	{
		return getTotalDiscount(getSession().getSessionContext());
	}

	/**
	 * Gets the total value of all discounts in this result. This result will be the same regardless of the applied state
	 * of this result, i.e. if not applied this is the discount value that would be applied, if it is applied then it is
	 * the value of the discount.
	 *
	 * @param ctx
	 *           The session context
	 * @return The double value for the total discount value
	 */
	public double getTotalDiscount(final SessionContext ctx)
	{
		double totalDiscount = 0.0D;

		final Collection<AbstractPromotionAction> actions = getActions(ctx);
		if (actions != null)
		{
			for (final AbstractPromotionAction action : actions)
			{
				totalDiscount += action.getValue(ctx);
			}
		}

		return totalDiscount;
	}

	/**
	 * Is this promotion result valid. This method checks to ensure that the promotion exists.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return true if the promotion result is valid
	 */
	protected boolean isValid(final SessionContext ctx)
	{
		try
		{
			final AbstractPromotion promotion = this.getPromotion(ctx);
			if (promotion == null)
			{
				return false;
			}
		}
		catch (final Exception e)//NOSONAR
		{
			return false;
		}
		return true;
	}

	/**
	 * Adds an action to the promotion results.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param action
	 *           The action to add
	 */
	public void addAction(final SessionContext ctx, final AbstractPromotionAction action)
	{
		if (ctx != null && action != null)
		{
			action.setPromotionResult(ctx, this);
		}
	}

	/**
	 * Adds a consumed entry to the current result.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param poec
	 *           the entry to add
	 */
	public void addConsumedEntry(final SessionContext ctx, final PromotionOrderEntryConsumed poec)
	{
		if (ctx != null && poec != null)
		{
			poec.setPromotionResult(ctx, this);
		}
	}

	/**
	 * Removes a consumed entry from the current result.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param poec
	 *           the entry to remove
	 */
	public void removeConsumedEntry(final SessionContext ctx, final PromotionOrderEntryConsumed poec)
	{
		if (ctx != null && poec != null)
		{
			// To remove the item just delete it
			try
			{
				poec.remove(ctx);
			}
			catch (final ConsistencyCheckException ex)//NOSONAR
			{
				LOG.warn("(" + getPK() + ") removeConsumedEntry failed to remove [" + poec + "] from db..");
			}
		}
	}

	/**
	 * Creates a deep clone of this promotion result and attach it to the specified order.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param target
	 *           the target order to transfer to
	 * @return the cloned promotion result
	 */
	protected PromotionResult transferToOrder(final SessionContext ctx, final Order target) //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("(" + getPK() + ") transferToOrder [" + this + "] order=[" + target + "]");
		}

		if (ctx != null && target != null)
		{
			final AbstractPromotion promotion = getPromotion(ctx);
			if (promotion != null)
			{
				// The order must hold an immutable copy of the promotion, so we try to find an existing
				// immutable clone, failing that we create a new clone
				final AbstractPromotion dupPromotion = promotion.findOrCreateImmutableClone(ctx);
				if (dupPromotion != null)
				{
					// We always need to deep clone the actions and consumed order entries so that they are
					// associated with the target order.
					final Collection<AbstractPromotionAction> dupActions = deepCloneAllActions(ctx);
					final Collection<PromotionOrderEntryConsumed> dupConsumedEntries = deepCloneConsumedEntriesAndAttachToOrder(ctx,
							target);

					// Create the new PromotionResult
					PromotionResult dupPromotionResult = null;

					final Map promotionResultValues = new HashMap();
					promotionResultValues.put(PromotionResult.CERTAINTY, getCertainty(ctx));
					promotionResultValues.put(PromotionResult.PROMOTION, dupPromotion);
					promotionResultValues.put(PromotionResult.ORDER, target);
					promotionResultValues.put(PromotionResult.ACTIONS, dupActions);
					promotionResultValues.put(PromotionResult.CONSUMEDENTRIES, dupConsumedEntries);

					final ComposedType type = TypeManager.getInstance().getComposedType(PromotionResult.class); // NOSONAR
					try //NOSONAR
					{
						dupPromotionResult = (PromotionResult) type.newInstance(ctx, promotionResultValues);
					}
					catch (final JaloGenericCreationException | JaloAbstractTypeException ex)
					{
						LOG.warn("(" + getPK() + ") transferToOrder: failed to create instance of PromotionResult", ex);
					}

					if (dupPromotionResult != null) //NOSONAR
					{
						// Associate the duplicated PromotionOrderEntryConsumed with the duplicated PromotionResult
						for (final PromotionOrderEntryConsumed dupConsumedEntry : dupConsumedEntries)
						{
							dupConsumedEntry.setPromotionResult(ctx, dupPromotionResult);
						}
					}
					return dupPromotionResult;
				}
			}
		}
		return null;
	}

	/**
	 * Clones all the actions attached to this promotion result.
	 *
	 * @param ctx
	 *           the hybris context
	 * @return the cloned actions
	 */
	protected Collection<AbstractPromotionAction> deepCloneAllActions(final SessionContext ctx)
	{
		final Collection<AbstractPromotionAction> dupActions = new ArrayList<>();

		final Collection<AbstractPromotionAction> actions = getActions(ctx);
		if (actions != null && !actions.isEmpty())
		{
			for (final AbstractPromotionAction a : actions)
			{
				dupActions.add(a.deepClone(ctx));
			}
		}

		return dupActions;
	}

	/**
	 * Clones of the consumed order entries attached to this promotion result and attach them to order entries in the
	 * specified order.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param target
	 *           the target order to attach the consumed order entries to
	 * @return the cloned order entries
	 */
	protected Collection<PromotionOrderEntryConsumed> deepCloneConsumedEntriesAndAttachToOrder(final SessionContext ctx,
			final Order target)
	{
		final Collection<PromotionOrderEntryConsumed> dupConsumedEntries = new ArrayList<>();

		// Get all the order entries in the target order
		final List<AbstractOrderEntry> allTargetEntries = target.getAllEntries(); // NOSONAR

		final Collection<PromotionOrderEntryConsumed> consumedEntries = getConsumedEntries(ctx);
		if (consumedEntries != null && !consumedEntries.isEmpty())
		{
			for (final PromotionOrderEntryConsumed poe : consumedEntries)
			{
				final PromotionOrderEntryConsumed consumedEntry = deepCloneConsumedEntryAndAttachToOrder(ctx, poe, allTargetEntries);
				if (consumedEntry != null)
				{
					dupConsumedEntries.add(consumedEntry);
				}
			}
		}

		return dupConsumedEntries;
	}

	/**
	 * Clones a consumed order entry and attach it to an order entry from the list specified.
	 *
	 * @param ctx
	 *           the hybris context
	 * @param source
	 *           the consumed entry to clone
	 * @param allTargetEntries
	 *           the list of order entries in the target order
	 * @return the cloned consumed order entry
	 */
	protected static PromotionOrderEntryConsumed deepCloneConsumedEntryAndAttachToOrder(final SessionContext ctx,
			final PromotionOrderEntryConsumed source, final List<AbstractOrderEntry> allTargetEntries)
	{
		final AbstractOrderEntry sourceOrderEntry = source.getOrderEntry(ctx);
		if (sourceOrderEntry != null)
		{
			final int entryNumber = sourceOrderEntry.getEntryNumber().intValue();

			// Get target order entry with same entry number. This is the same order entry as our sourceOrderEntry but attached to the target order
			final AbstractOrderEntry targetOrderEntry = findOrderEntryWithEntryNumber(allTargetEntries, entryNumber);
			if (targetOrderEntry == null)
			{
				// Cannot find matching entry with same entry number
				LOG.warn("cloneConsumedEntryToOrder source=[" + source + "] cannot find matching order entry with entryNumber=["
						+ entryNumber + "]");
			}
			else
			{
				// Check that the targetOrderEntry has the same product and quantity as our sourceOrderEntry
				if (!sourceOrderEntry.getProduct(ctx).equals(targetOrderEntry.getProduct(ctx)))
				{
					LOG.warn("transferToOrder source=[" + source + "] order entry with entryNumber=[" + entryNumber
							+ "] has different product. expected=[" + sourceOrderEntry.getProduct(ctx) + "] actual=["
							+ targetOrderEntry.getProduct(ctx) + "]");
				}
				else if (!sourceOrderEntry.getQuantity(ctx).equals(targetOrderEntry.getQuantity(ctx)))
				{
					LOG.warn("transferToOrder source=[" + source + "] order entry with entryNumber=[" + entryNumber
							+ "] has different quantity. expected=[" + sourceOrderEntry.getQuantity(ctx) + "] actual=["
							+ targetOrderEntry.getQuantity(ctx) + "]");
				}
				else
				{
					// Create a new consumed order entry attached to the target order entry
					return PromotionsManager.getInstance().createPromotionOrderEntryConsumed(ctx, source.getCode(ctx),
							targetOrderEntry, source.getQuantity(ctx).longValue(), source.getAdjustedUnitPrice(ctx).doubleValue());
				}
			}
		}
		return null;
	}

	/**
	 * Finds the order entry with the specified entry number.
	 *
	 * @param allTargetEntries
	 *           the list of order entries
	 * @param entryNumber
	 *           the entry number to search for
	 * @return the order entry with the entry number specified or null of not found.
	 */
	protected static AbstractOrderEntry findOrderEntryWithEntryNumber(final List<AbstractOrderEntry> allTargetEntries,
			final int entryNumber)
	{
		for (final AbstractOrderEntry entry : allTargetEntries)
		{
			if (entryNumber == entry.getEntryNumber().intValue())
			{
				return entry;
			}
		}
		return null;
	}

	/**
	 * Gets the collection of {@link PromotionOrderEntryConsumed} consumed.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return A collection of {@link PromotionOrderEntryConsumed} consumed by this result.
	 */
	@Override
	public Collection getConsumedEntries(final SessionContext ctx)
	{
		final String query = "SELECT {" + Item.PK + "} " + "FROM   {"
				+ TypeManager.getInstance().getComposedType(PromotionOrderEntryConsumed.class).getCode() + "} " + "WHERE  {" // NOSONAR
				+ PromotionOrderEntryConsumed.PROMOTIONRESULT + "} = ?promotionResult";

		final Flat3Map args = new Flat3Map();
		args.put("promotionResult", this);

		final Collection results = getSession().getFlexibleSearch().search(ctx, query, args, PromotionOrderEntryConsumed.class)
				.getResult();
		return Collections.unmodifiableCollection(results);
	}

	/**
	 * Sets the collection of {@link PromotionOrderEntryConsumed} consumed.
	 * <p/>
	 * The {@link PromotionOrderEntryConsumed} instances associated with this promotion result are owned (and part of)
	 * this result. They cannot belong to another instance, therefore when setting the collection any
	 * {@link PromotionOrderEntryConsumed} instances previously associated with this result, that are no longer
	 * associated are deleted from the database.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param promotionOrderEntryConsumeds
	 *           the collection of consumed entries
	 */
	@Override
	public void setConsumedEntries(final SessionContext ctx, final Collection promotionOrderEntryConsumeds) //NOSONAR
	{
		// Copy the inbound collection into a modifiable list of the correct types
		final ArrayList<PromotionOrderEntryConsumed> newItems = new ArrayList<>();
		if (promotionOrderEntryConsumeds != null && !promotionOrderEntryConsumeds.isEmpty())
		{
			for (final Object obj : promotionOrderEntryConsumeds)
			{
				if (obj instanceof PromotionOrderEntryConsumed)
				{
					newItems.add((PromotionOrderEntryConsumed) obj);
				}
			}
		}

		// Loop through existing items, remove any that are not in the newItems list
		final Collection<PromotionOrderEntryConsumed> oldItems = getConsumedEntries(ctx);
		if (oldItems != null && !oldItems.isEmpty())
		{
			for (final PromotionOrderEntryConsumed oldItem : oldItems)
			{
				// This is a bit hard to read but basically if the oldItem is in the
				// newItems list then we are keeping it, we don't need to add or remove it
				// remove here returns true if it was in the list
				final boolean keepItem = newItems.remove(oldItem);
				if (!keepItem)
				{
					// we are not keeping this item, delete from DB
					try //NOSONAR
					{
						oldItem.remove(ctx);
					}
					catch (final ConsistencyCheckException ex)
					{
						LOG.error("setConsumedEntries failed to remove [" + oldItem + "] from database", ex);
					}
				}
			}
		}

		// Now loop through what is left in newItems and add them
		if (!newItems.isEmpty())
		{
			for (final PromotionOrderEntryConsumed newItem : newItems)
			{
				newItem.setPromotionResult(ctx, this);
			}
		}
	}

	/**
	 * Gets the collection of {@link AbstractPromotionAction} instances.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @return A collection of {@link AbstractPromotionAction} instances attached to this result.
	 */
	@Override
	public Collection getActions(final SessionContext ctx)
	{
		final String query = "SELECT {" + Item.PK + "} " + "FROM   {"
				+ TypeManager.getInstance().getComposedType(AbstractPromotionAction.class).getCode() + "} " + "WHERE  {" // NOSONAR
				+ AbstractPromotionAction.PROMOTIONRESULT + "} = ?promotionResult";

		final Flat3Map args = new Flat3Map();
		args.put("promotionResult", this);

		final Collection results = getSession().getFlexibleSearch().search(ctx, query, args, AbstractPromotionAction.class)
				.getResult();
		return Collections.unmodifiableCollection(results);
	}

	/**
	 * Sets the collection of {@link AbstractPromotionAction} instance.
	 * <p/>
	 * The {@link AbstractPromotionAction} instances associated with this promotion result are owned (and part of) this
	 * result. They cannot belong to another instance, therefore when setting the collection any
	 * {@link AbstractPromotionAction} instances previously associated with this result, that are no longer associated
	 * are deleted from the database.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param actions
	 *           the collection of actions
	 */
	@Override
	public void setActions(final SessionContext ctx, final Collection actions) //NOSONAR
	{
		// Copy the inbound collection into a modifiable list of the correct types
		final ArrayList<AbstractPromotionAction> newActions = new ArrayList<>();
		if (actions != null && !actions.isEmpty())
		{
			for (final Object obj : actions)
			{
				if (obj instanceof AbstractPromotionAction)
				{
					newActions.add((AbstractPromotionAction) obj);
				}
			}
		}

		// Loop through existing actions, remove any that are not in the newActions list
		final Collection<AbstractPromotionAction> oldActions = getActions(ctx);
		if (oldActions != null && !oldActions.isEmpty())
		{
			for (final AbstractPromotionAction oldAction : oldActions)
			{
				// This is a bit hard to read but basically if the oldAction is in the
				// newActions list then we are keeping it, we don't need to add or remove it
				// remove here returns true if it was in the list
				final boolean keepItem = newActions.remove(oldAction);
				if (!keepItem)
				{
					// we are not keeping this item, delete from DB
					try //NOSONAR
					{
						oldAction.remove(ctx);
					}
					catch (final ConsistencyCheckException ex)
					{
						LOG.error("setActions failed to remove [" + oldAction + "] from database", ex);
					}
				}
			}
		}

		// Now loop through what is left in newActions and add them
		if (!newActions.isEmpty())
		{
			for (final AbstractPromotionAction newAction : newActions)
			{
				newAction.setPromotionResult(ctx, this);
			}
		}
	}

	/**
	 * Generates a string identifier that can be used to establish if 2 PromotionResults are the same. The identifier
	 * should be based on the data for the PromotionResult, e.g. the promotion that created it, the number and type of
	 * products consumed, the actions created.
	 *
	 * @param ctx
	 *           The hybris context
	 * @return a string that identifies this promotion result
	 */
	protected String getDataUnigueKey(final SessionContext ctx)
	{
		final AbstractPromotion promotion = getPromotion(ctx);
		if (promotion != null)
		{
			return promotion.getPromotionResultDataUnigueKey(ctx, this);
		}
		return null;
	}

}
