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
package de.hybris.platform.promotions.result;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.jalo.PromotionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * An object representing the results of evaluating promotions on an AbstractOrder. This object holds details of each
 * promotion that fired, and the OrderEntry objects that it fired on, and details of all other OrderEntry objects,
 * indicating if they could be part of a potential promotion. A {@link PromotionResult} object represents the state of
 * any promotion that fired or potentially fired.
 *
 *
 */
public class PromotionOrderResults
{
	private final List<PromotionResult> promotionResults;
	private final AbstractOrder order;
	private final SessionContext ctx;
	private final double changeFromLastResults;

	// Volatile keyword enables thread safe double-check locking in JDK1.5
	private volatile List<PromotionResult> firedProductPromotions;
	private volatile List<PromotionResult> appliedProductPromotions;
	private volatile List<PromotionResult> potentialProductPromotions;
	private volatile List<PromotionResult> firedOrderPromotions;
	private volatile List<PromotionResult> appliedOrderPromotions;
	private volatile List<PromotionResult> potentialOrderPromotions;

	private volatile List<WrappedOrderEntry> entriesNotInFiredPromotions;
	private volatile List<WrappedOrderEntry> entriesNotInPromotions;
	private volatile List<WrappedOrderEntry> entriesWithPotentialPromotions;

	/**
	 * Constructor.
	 *
	 * @param ctx
	 *           The hybris context
	 * @param order
	 *           The order
	 * @param promotionResults
	 *           The promotion results
	 * @param changeFromLastResults
	 *           The change from last results
	 */
	public PromotionOrderResults(final SessionContext ctx, final AbstractOrder order,
			final List<PromotionResult> promotionResults, final double changeFromLastResults)
	{
		this.ctx = ctx;
		this.order = order;
		this.promotionResults = promotionResults;
		this.changeFromLastResults = changeFromLastResults;
	}

	/**
	 * Invalidate all cached data held in this PromotionOrderResults instance.
	 *
	 * This method should be called if any of the PromotionResults are applied or unapplied.
	 */
	public void invalidateCache()
	{
		synchronized (this)
		{
			firedProductPromotions = null;
			appliedProductPromotions = null;
			potentialProductPromotions = null;
			firedOrderPromotions = null;
			appliedOrderPromotions = null;
			potentialOrderPromotions = null;
			entriesNotInFiredPromotions = null;
			entriesNotInPromotions = null;
			entriesWithPotentialPromotions = null;
		}
	}

	/**
	 * Return the total change since the last evaluation of results.
	 *
	 * @return A double representing the calculation <i>newTotalValueOfDiscounts - oldTotalValueOfDiscounts</i>
	 */
	public double getTotalChangeFromLastResults()
	{
		return changeFromLastResults;
	}

	/**
	 * Returns all {@link PromotionResult} objects. This list will include all promotions that either fired or identified
	 * themselves as potentially firing.
	 *
	 * @return The list of promotion results
	 */
	public List<PromotionResult> getAllResults()
	{
		return Collections.unmodifiableList(promotionResults);
	}

	// ----------------------------------------------------------------------------
	// Product Promotions
	// ----------------------------------------------------------------------------

	/**
	 * Return a list of results for promotions that consumed products. This list will include all promotions that
	 * consumed products regardless of their firing status.
	 *
	 * @return The list of promotion results
	 */
	public List<PromotionResult> getAllProductPromotions()
	{
		return getPromotionResults(PromotionResultStatus.Any, PromotionResultProducts.RequireConsumedProducts);
	}

	/**
	 * Return a list of results for promotions that fired and consumed products.
	 *
	 * @return A list of the results of product promotions that fired
	 */
	public List<PromotionResult> getFiredProductPromotions()
	{
		if (firedProductPromotions == null)
		{
			synchronized (this)
			{
				if (firedProductPromotions == null)
				{
					firedProductPromotions = getPromotionResults(PromotionResultStatus.FiredOrApplied,
							PromotionResultProducts.RequireConsumedProducts);
				}
			}
		}
		return firedProductPromotions;
	}

	/**
	 * Return a list of results for promotions that fired and consumed products and have been applied.
	 *
	 * @return A list of the results of product promotions that fired and applied
	 */
	public List<PromotionResult> getAppliedProductPromotions()
	{
		if (appliedProductPromotions == null)
		{
			synchronized (this)
			{
				if (appliedProductPromotions == null)
				{
					appliedProductPromotions = getPromotionResults(PromotionResultStatus.AppliedOnly,
							PromotionResultProducts.RequireConsumedProducts);
				}
			}
		}
		return appliedProductPromotions;
	}

	/**
	 * Return a list of results for promotions that fired and consumed products and have been applied.
	 *
	 * @return A list of the results of product promotions that fired and applied
	 */
	public List<PromotionResult> getPotentialProductPromotions()
	{
		if (potentialProductPromotions == null)
		{
			synchronized (this)
			{
				if (potentialProductPromotions == null)
				{
					potentialProductPromotions = getPromotionResults(PromotionResultStatus.CouldFireOnly,
							PromotionResultProducts.RequireConsumedProducts);
				}
			}
		}
		return potentialProductPromotions;
	}

	// ----------------------------------------------------------------------------
	// Order Promotions
	// ----------------------------------------------------------------------------

	/**
	 * Return a list of results for promotions that did not consume products. This list will include all promotions that
	 * did not consume products regardless of their firing status
	 *
	 * @return The list of promotion results
	 */
	public List<PromotionResult> getAllOrderPromotions()
	{
		return getPromotionResults(PromotionResultStatus.Any, PromotionResultProducts.NoConsumedProducts);
	}

	/**
	 * Return a list of results for promotions that fired and did not consume products.
	 *
	 * @return A list of the results of promotions that fired
	 */
	public List<PromotionResult> getFiredOrderPromotions()
	{
		if (firedOrderPromotions == null)
		{
			synchronized (this)
			{
				if (firedOrderPromotions == null)
				{
					firedOrderPromotions = getPromotionResults(PromotionResultStatus.FiredOrApplied,
							PromotionResultProducts.NoConsumedProducts);
				}
			}
		}
		return firedOrderPromotions;
	}

	/**
	 * Return a list of results for promotions that fired and did not consume products and have been applied.
	 *
	 * @return A list of the results of promotions that fired and applied
	 */
	public List<PromotionResult> getAppliedOrderPromotions()
	{
		if (appliedOrderPromotions == null)
		{
			synchronized (this)
			{
				if (appliedOrderPromotions == null)
				{
					appliedOrderPromotions = getPromotionResults(PromotionResultStatus.AppliedOnly,
							PromotionResultProducts.NoConsumedProducts);
				}
			}
		}
		return appliedOrderPromotions;
	}

	/**
	 * Return a list of results for promotions that fired and did not consume products and have been applied.
	 *
	 * @return A list of the results of order promotions that fired and applied
	 */
	public List<PromotionResult> getPotentialOrderPromotions()
	{
		if (potentialOrderPromotions == null)
		{
			synchronized (this)
			{
				if (potentialOrderPromotions == null)
				{
					potentialOrderPromotions = getPromotionResults(PromotionResultStatus.CouldFireOnly,
							PromotionResultProducts.NoConsumedProducts);
				}
			}
		}
		return potentialOrderPromotions;
	}

	// ----------------------------------------------------------------------------
	// Utility method to get the promotions results
	// ----------------------------------------------------------------------------

	protected enum PromotionResultStatus
	{
		Any, CouldFireOnly, FiredOnly, AppliedOnly, FiredOrApplied //NOSONAR
	}

	protected enum PromotionResultProducts
	{
		Any, NoConsumedProducts, RequireConsumedProducts //NOSONAR
	}

	/**
	 * Return a list of results for promotions that meet the specified criteria.
	 *
	 * @param statusFlag
	 *           status criteria
	 * @param productsFlag
	 *           consumed products criteria
	 * @return a list of promotion results
	 */
	protected List<PromotionResult> getPromotionResults(final PromotionResultStatus statusFlag, //NOSONAR
			final PromotionResultProducts productsFlag)
	{
		final List<PromotionResult> tmpResults = new LinkedList<>();

		for (final PromotionResult promotionResult : promotionResults)
		{
			boolean statusOk = false;

			// Check the status flag
			if (statusFlag == PromotionResultStatus.Any
					|| (statusFlag == PromotionResultStatus.CouldFireOnly && promotionResult.getCouldFire(ctx)))
			{
				statusOk = true;
			}
			else if ((statusFlag == PromotionResultStatus.FiredOnly || statusFlag == PromotionResultStatus.AppliedOnly || statusFlag == PromotionResultStatus.FiredOrApplied)
					&& promotionResult.getFired(ctx))
			{
				if (statusFlag == PromotionResultStatus.FiredOrApplied)
				{
					statusOk = true;
				}
				else if (promotionResult.isApplied(ctx))
				{
					statusOk = statusFlag == PromotionResultStatus.AppliedOnly;
				}
				else
				// !promotionResult.isApplied(ctx)
				{
					statusOk = statusFlag == PromotionResultStatus.FiredOnly;
				}
			}

			if (statusOk)
			{
				boolean productsOk;

				if (productsFlag == PromotionResultProducts.Any)
				{
					productsOk = true;
				}
				else
				{
					// See if the result is consuming products
					final Collection consumed = promotionResult.getConsumedEntries(ctx);
					final boolean hasConsumedProducts = consumed != null && !consumed.isEmpty();

					productsOk = (productsFlag == PromotionResultProducts.RequireConsumedProducts && hasConsumedProducts)
							|| (productsFlag == PromotionResultProducts.NoConsumedProducts && !hasConsumedProducts);
				}

				if (productsOk)
				{
					tmpResults.add(promotionResult);
				}
			}
		}

		return Collections.unmodifiableList(tmpResults);
	}

	// ----------------------------------------------------------------------------
	// Order entries with attached promotions
	// ----------------------------------------------------------------------------

	/**
	 * Return a list of WrappedOrderEntry objects that were not part of any any promotion that fired or could fire.
	 *
	 * @return a list of WrappedOrderEntry objects
	 */
	public List<WrappedOrderEntry> getEntriesNotInPromotions() //NOSONAR
	{
		if (entriesNotInPromotions == null)
		{
			synchronized (this)
			{
				if (entriesNotInPromotions == null)
				{
					// Wrap the order entries
					final List<WrappedOrderEntry> wrappedEntries = new ArrayList<>();

					// Remove line items for promotions that have fired
					for (final WrappedOrderEntry entry : getEntriesNotInFiredPromotions())
					{
						// Get all the potential promotions that are associated with this order entry
						final List<PromotionResult> entryPromotionResults = entry.getPromotionResults();
						if (entryPromotionResults == null || entryPromotionResults.isEmpty()) //NOSONAR
						{
							// No potential promotions, therefore all of this entry in not in a promotion
							wrappedEntries.add(entry);
						}
						else
						{
							// Some or all of the quantity of the order entry is consumed by the associated promotions
							// Note that because these are potential rather than fired promotions they are consuming in
							// parallel rather than series, therefore we need to find the maximum number consumed by
							// any associated promotion.
							final long maxConsumedForOrderEntry = getMaxConsumedQuantityForEntry(ctx, entryPromotionResults,
									entry.getBaseOrderEntry());

							// Check if all the quantity of the entry has been consumed
							final long entryQuantity = entry.getQuantity(ctx);
							if (entryQuantity > maxConsumedForOrderEntry)
							{
								// Part of the order entry is not consumed, create a new wrapped order entry for this quantity
								wrappedEntries.add(
										new WrappedOrderEntry(ctx, entry.getBaseOrderEntry(), entryQuantity - maxConsumedForOrderEntry));
							}
						}
					}

					entriesNotInPromotions = Collections.unmodifiableList(wrappedEntries);
				}
			}
		}
		return entriesNotInPromotions;
	}

	/**
	 * Return a list of WrappedOrderEntry objects that were part of any any promotion that could fire.
	 *
	 * @return a list of WrappedOrderEntry objects
	 */
	public List<WrappedOrderEntry> getEntriesWithPotentialPromotions() //NOSONAR
	{
		if (entriesWithPotentialPromotions == null)
		{
			synchronized (this)
			{
				if (entriesWithPotentialPromotions == null)
				{
					// Wrap the order entries
					final List<WrappedOrderEntry> wrappedEntries = new ArrayList<>();

					// Remove line items for promotions that have fired
					for (final WrappedOrderEntry entry : getEntriesNotInFiredPromotions())
					{
						final List<PromotionResult> entryPromotionResults = entry.getPromotionResults();
						if (entryPromotionResults != null && !entryPromotionResults.isEmpty()) //NOSONAR
						{
							// entry has potential promotion results. These promotion results will
							// consume some or all of the quantity of the order entry. As these are
							// potential promotions rather than fired promotions the quantity will
							// be consumed in parallel rather than series.

							final long maxConsumedForOrderEntry = getMaxConsumedQuantityForEntry(ctx, entryPromotionResults,
									entry.getBaseOrderEntry());

							// Check if all the quantity of the entry has been consumed
							final long entryQuantity = entry.getQuantity(ctx);
							if (maxConsumedForOrderEntry <= 0)
							{ //NOPMD
								  // Should not get here
							}
							else if (entryQuantity == maxConsumedForOrderEntry)
							{
								// Whole quantity consumed, just return entry
								wrappedEntries.add(entry);
							}
							else if (maxConsumedForOrderEntry < entryQuantity)
							{
								// Part of the order entry is not consumed, create a new wrapped order entry for this quantity
								wrappedEntries.add(new WrappedOrderEntry(ctx, entry.getBaseOrderEntry(), maxConsumedForOrderEntry,
										entry.getPromotionResults()));
							}

						}
					}

					entriesWithPotentialPromotions = Collections.unmodifiableList(wrappedEntries);
				}
			}
		}
		return entriesWithPotentialPromotions;
	}

	/**
	 * Return a list of WrappedOrderEntry objects that were not part of any any promotion that fired. The
	 * WrappedOrderEntry holds the promotions that could fire for the specified products.
	 *
	 * Note that the results returned from this method are the same as those from combining the results from methods
	 * {@link #getEntriesWithPotentialPromotions} and {@link #getEntriesNotInPromotions} but the WrappedOrderEntry
	 * objects may be consolidated together.
	 *
	 * In general it is preferable to call {@link #getEntriesWithPotentialPromotions} and
	 * {@link #getEntriesNotInPromotions} that than calling this method directly.
	 *
	 * @return a list of WrappedOrderEntry objects
	 */
	public List<WrappedOrderEntry> getEntriesNotInFiredPromotions()
	{
		if (entriesNotInFiredPromotions == null)
		{
			synchronized (this)
			{
				if (entriesNotInFiredPromotions == null)
				{
					// Wrap the order entries
					List<WrappedOrderEntry> wrappedEntries = wrapEntries(ctx, order);

					// Remove line items for promotions that have fired
					for (final PromotionResult promotionResult : getFiredProductPromotions())
					{
						removeConsumedEntries(ctx, wrappedEntries, promotionResult.getConsumedEntries(ctx));
					}

					// Remove empty WrappedOrderEntry objects from list
					wrappedEntries = cleanWrappedEntryList(ctx, wrappedEntries);

					// Associate potential promotions with wrapped entries
					Collection<WrappedOrderEntry> matchingEntries;
					for (final PromotionResult promotionResult : getPotentialProductPromotions())
					{
						matchingEntries = findAllMatchingEntries(ctx, wrappedEntries, promotionResult.getConsumedEntries(ctx));
						for (final WrappedOrderEntry wrappedEntry : matchingEntries) //NOSONAR
						{
							wrappedEntry.addPromotionResult(ctx, promotionResult);
						}
					}

					entriesNotInFiredPromotions = Collections.unmodifiableList(wrappedEntries);
				}
			}
		}
		return entriesNotInFiredPromotions;
	}

	// ----------------------------------------------------------------------------
	// Utility methods to count the number of consumed entries in results mapped to a specific order
	// ----------------------------------------------------------------------------

	protected static long getMaxConsumedQuantityForEntry(final SessionContext ctx, final List<PromotionResult> promotionResults,
			final AbstractOrderEntry orderEntry)
	{
		long maxConsumedForProduct = 0;

		for (final PromotionResult result : promotionResults)
		{
			// For this promotion result calculate the total quantity that are potentially consumed
			// For this we need to examine all the consumed entries that are for the real base order entry
			final long counsumedCountForProductInThisResult = getConsumedQuantityForEntry(ctx, result, orderEntry);

			// Check to see if our sun of consumed quantity is greater than the current maximum
			if (counsumedCountForProductInThisResult > maxConsumedForProduct)
			{
				maxConsumedForProduct = counsumedCountForProductInThisResult;
			}
		}

		return maxConsumedForProduct;
	}

	protected static long getConsumedQuantityForEntry(final SessionContext ctx, final PromotionResult result,
			final AbstractOrderEntry orderEntry)
	{
		long quantityTotal = 0;

		final Collection<PromotionOrderEntryConsumed> consumedEntries = result.getConsumedEntries(ctx);
		if (consumedEntries != null && !consumedEntries.isEmpty())
		{
			for (final PromotionOrderEntryConsumed poec : consumedEntries)
			{
				if (poec.getOrderEntry(ctx) == orderEntry) //NOSONAR
				{
					quantityTotal += poec.getQuantityAsPrimitive(ctx);
				}
			}
		}

		return quantityTotal;
	}

	// ----------------------------------------------------------------------------
	// Utility methods to filter list of order entries
	// ----------------------------------------------------------------------------

	protected static Collection<WrappedOrderEntry> findAllMatchingEntries(final SessionContext ctx,
			final List<WrappedOrderEntry> wrappedEntries, final Collection<PromotionOrderEntryConsumed> consumedEntries)
	{
		final Set<WrappedOrderEntry> matchingEntries = new HashSet<>();

		for (final PromotionOrderEntryConsumed consumedEntry : consumedEntries)
		{
			final AbstractOrderEntry orderEntry = consumedEntry.getOrderEntry(ctx);

			// Look for WrappedOrderEntry that share the same base order entry as the consumedEntry
			for (final WrappedOrderEntry wrappedEntry : wrappedEntries)
			{
				if (wrappedEntry.getBaseOrderEntry().equals(orderEntry))
				{
					matchingEntries.add(wrappedEntry);
				}
			}
		}

		return matchingEntries;
	}


	protected static List<WrappedOrderEntry> wrapEntries(final SessionContext ctx, final AbstractOrder order)
	{
		final List<WrappedOrderEntry> wrappedEntries = new ArrayList<>(10);

		if (order != null)
		{
			final List<AbstractOrderEntry> entries = order.getAllEntries(); //NOSONAR
			for (final AbstractOrderEntry entry : entries)
			{
				wrappedEntries.add(new WrappedOrderEntry(ctx, entry));
			}
		}
		return wrappedEntries;
	}

	protected static void removeConsumedEntries(final SessionContext ctx, final List<WrappedOrderEntry> wrappedEntries,
			final Collection<PromotionOrderEntryConsumed> consumedEntries)
	{
		for (final PromotionOrderEntryConsumed consumedEntry : consumedEntries)
		{
			for (final WrappedOrderEntry wrappedEntry : wrappedEntries)
			{
				if (wrappedEntry.consumePromotionOrderEntryConsumed(ctx, consumedEntry))
				{
					break;
				}
			}
		}
	}

	protected static List<WrappedOrderEntry> cleanWrappedEntryList(final SessionContext ctx,
			final List<WrappedOrderEntry> wrappedEntries)
	{
		final List<WrappedOrderEntry> result = new ArrayList<>(wrappedEntries.size());
		for (final WrappedOrderEntry entry : wrappedEntries)
		{
			if (entry.getQuantity(ctx) > 0)
			{
				result.add(entry);
			}
		}
		return result;
	}
}
