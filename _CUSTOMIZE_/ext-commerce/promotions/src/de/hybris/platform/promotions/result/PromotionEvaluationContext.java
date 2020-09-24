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
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.jalo.AbstractPromotion;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.util.CompositeProduct;
import de.hybris.platform.promotions.util.Helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.log4j.Logger;


/**
 * PromotionEvaluationContext.
 *
 *
 */
public class PromotionEvaluationContext
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PromotionEvaluationContext.class.getName());

	private final List<PromotionOrderEntry> orderEntries = new ArrayList<>();
	private final ConsumptionLogger logger = new ConsumptionLogger();
	private AbstractOrder order = null;
	private boolean observeRestrictions = true;
	private final Date date;

	/**
	 * Create an context for evaluating promotions on the given order.
	 *
	 * @param order
	 *           The order the context is evaluating
	 * @param observeRestrictions
	 *           Whether restrictions should be observed during evaluation
	 * @param date
	 *           The effective date to use when evaluating promotions
	 */
	public PromotionEvaluationContext(final AbstractOrder order, final boolean observeRestrictions, final Date date)
	{
		this.order = order;
		this.observeRestrictions = observeRestrictions;
		this.date = date;

		// For each order entry in the order create a PromotionOrderEntry wrapper to track operations on that order
		// entry
		for (final AbstractOrderEntry entry : (List<AbstractOrderEntry>) this.order.getAllEntries()) //NOSONAR
		{
			orderEntries.add(new PromotionOrderEntry(entry, logger));
		}
	}

	/**
	 * Start recording all of the consumed items in any view. This will remember any items consumed from any view that
	 * currently exist, and any view created.
	 *
	 * @param promo
	 *           The promotion requesting the recording
	 */
	public void startLoggingConsumed(final AbstractPromotion promo)
	{
		this.getLogger().startLogging(promo);
	}

	/**
	 * Complete the logging operation and return a list of all order entry consumptions since logging began. This
	 * operation moves the current set of operations to the LOG of all operations since the context was created.
	 *
	 * @param promo
	 *           The promotion requesting to complete the logging operation
	 * @param removeFromOrder
	 *           Consume all the entries marked such that they cannot be used in another promotion
	 * @return A List of all consumption operations made to any view since logged was started
	 */
	public List<PromotionOrderEntryConsumed> finishLoggingAndGetConsumed(final AbstractPromotion promo,
			final boolean removeFromOrder)
	{
		return this.getLogger().completeOperation(promo, removeFromOrder);
	}

	/**
	 * Abandon the current logging operation and throw away all of the consumption objects.
	 *
	 * @param promo
	 *           the promotion
	 */
	public void abandonLogging(final AbstractPromotion promo)
	{
		this.getLogger().abandonOperation(promo);
	}

	/**
	 * Get the consumption logger for this context. Only used internally by the view system.
	 *
	 * @return The logger
	 */
	protected ConsumptionLogger getLogger()
	{
		return this.logger;
	}

	/**
	 * Get the order that this context is working on.
	 *
	 * @return The current order
	 */
	public AbstractOrder getOrder()
	{
		return order;
	}

	/**
	 * Should restrictions be observed when evaluating promotions.
	 *
	 * @return <code>true</code> if restrictions should be observed, <code>false</code> otherwise.
	 */
	public boolean getObserveRestrictions() // NOPMD
	{
		return this.observeRestrictions;
	}

	/**
	 * Create a filtered view of the order containing only the products specified in validProducts. This view shows the
	 * live remaining quantities of items, and is able to determine how many items remain. The view removes consumed
	 * items and as such always shows the current available state.
	 *
	 * The products in the cart and their base products (if variant or composite) are matched against the validProducts
	 * list. This allows a variant product in the cart to be matched against its base product in the list of valid
	 * products.
	 *
	 * Variant products extend VariantProduct and composite products implement the {@link CompositeProduct} interface.
	 *
	 * @param ctx
	 *           The hybris session context
	 * @param promotion
	 *           The promotion creating the view
	 * @param validProducts
	 *           The products that should be included in the view
	 * @return A filtered view of the order showing only products from the list of valid products
	 */
	public PromotionOrderView createView(final SessionContext ctx, final AbstractPromotion promotion, //NOSONAR
			final List<Product> validProducts) //NOSONAR
	{
		final List<PromotionOrderEntry> validEntries = new ArrayList<>();
		for (final PromotionOrderEntry entry : orderEntries)
		{
			// Match against both the product and its base product (if it has one)
			final Product product = entry.getProduct(ctx); //NOSONAR
			final List<Product> baseProducts = Helper.getBaseProducts(ctx, product); //NOSONAR
			boolean validProductsContainsOneBaseProduct = false;
			if (baseProducts != null && validProducts != null)
			{
				for (final Product baseProduct : baseProducts) //NOSONAR
				{
					if (validProducts.contains(baseProduct)) //NOSONAR
					{
						validProductsContainsOneBaseProduct = true;
						break;
					}
				}
			}
			final boolean productIsInBaseProducts = (baseProducts != null) ? baseProducts.contains(product) : false;

			if (validProducts == null || validProductsContainsOneBaseProduct
					|| ((!productIsInBaseProducts) && validProducts.contains(product)))
			{
				validEntries.add(entry);
			}

		}
		return new PromotionOrderView(promotion, validEntries);
	}

	/**
	 * Get the effective date from the evaluation context. The date exists so that the effect of promotions can be seen
	 * on dates other than the current system time.
	 *
	 * @return The date that the promotions are being evaluated using
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Create a comparator for the current context that orders by price in natural order (lowest to highest). The head of
	 * a list sorted by this comparator will have the lowest price at index <i>0</i> and the highest at index
	 * <i>length-1</i>
	 *
	 * @param ctx
	 *           The current hybris session context
	 * @return A comparator to sort PromotionOrderEntry objects by price
	 */
	public static Comparator<PromotionOrderEntry> createPriceComparator(final SessionContext ctx)
	{
		return new Comparator<PromotionOrderEntry>() //NOSONAR
		{
			@Override
			public int compare(final PromotionOrderEntry a, final PromotionOrderEntry b)
			{
				return a.getBasePrice(ctx).compareTo(b.getBasePrice(ctx));
			}
		};
	}


	/**
	 * Consumptionlogger keeps track of all consumptions during an evaluation run.
	 */
	protected static class ConsumptionLogger
	{
		private final List<PromotionOrderEntryConsumed> history = new ArrayList<>();
		private final List<PromotionOrderEntryConsumed> currentOperationLog = new ArrayList<>();
		private AbstractPromotion activePromotion = null;

		public List<PromotionOrderEntryConsumed> getHistory()
		{
			return history;
		}

		public void logOperation(final PromotionOrderEntryConsumed entry)
		{
			if (activePromotion == null)
			{
				throw new PromotionException("Attempt to LOG a consumption operation without starting logging for promotion");
			}
			currentOperationLog.add(entry);
		}

		/**
		 * Get every operation performed since the logger was created.
		 *
		 * @return A list of all consumptions that have been made during the lifetime of this context
		 */
		public Iterable<PromotionOrderEntryConsumed> getAllOperations()
		{
			final IteratorChain chain = new IteratorChain();
			chain.addIterator(history.iterator());
			chain.addIterator(currentOperationLog.iterator());

			return new Iterable<PromotionOrderEntryConsumed>() //NOSONAR
			{
				@Override
				public Iterator<PromotionOrderEntryConsumed> iterator()
				{
					return chain;
				}
			};

		}

		public synchronized void startLogging(final AbstractPromotion promotion)
		{
			if (activePromotion == null)
			{
				activePromotion = promotion;
			}
			else
			{
				throw new PromotionException("Cannot start logging operation [" + promotion + "] when already logging for operation ["
						+ activePromotion + "]");
			}
		}

		public synchronized List<PromotionOrderEntryConsumed> completeOperation(final AbstractPromotion promotion,
				final boolean removeFromOrder)
		{
			if (activePromotion == null || !promotion.equals(activePromotion))
			{
				throw new PromotionException("Logging has not started for promotion [" + promotion + "]");
			}

			for (final PromotionOrderEntryConsumed poec : currentOperationLog)
			{
				poec.setRemovedFromOrder(removeFromOrder);
			}

			history.addAll(currentOperationLog);
			final List<PromotionOrderEntryConsumed> retval = new ArrayList<>(currentOperationLog);
			currentOperationLog.clear();
			activePromotion = null;
			return retval;
		}

		public synchronized void abandonOperation(final AbstractPromotion promotion)
		{
			if (activePromotion == null || !promotion.equals(activePromotion))
			{
				throw new PromotionException("Logging has not started for promotion [" + promotion + "]");
			}

			currentOperationLog.clear();
			activePromotion = null;
		}
	}

}
