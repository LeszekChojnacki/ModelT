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

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.promotions.result.PromotionException;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.promotions.util.Helper;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;


/**
 * Created with IntelliJ IDEA. User: gary Date: 08/05/2013 Time: 09:26 To change this template use File | Settings |
 * File Templates.
 */
public class CachingPromotionsManager extends PromotionsManager
{

	private static final Logger LOG = Logger.getLogger(CachingPromotionsManager.class);

	protected static final String CACHING_ALLOWED = "de.hybris.platform.promotions.jalo.cachingAllowed";

	private CachingStrategy cache; //NOSONAR


	@Override
	public PromotionOrderResults updatePromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order)
	{
		return updatePromotions(ctx, promotionGroups, order, true, AutoApplyMode.APPLY_ALL, AutoApplyMode.KEEP_APPLIED,
				Helper.getDateNowRoundedToMinute());
	}

	@Override
	public PromotionOrderResults updatePromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		return applyWithCachingEnabled(ctx, order, (c, o) -> doUpdatePromotions(c, promotionGroups, o, evaluateRestrictions,
				productPromotionMode, orderPromotionMode, date));
	}

	@Override
	public PromotionOrderResults getPromotionResults(final SessionContext ctx, final AbstractOrder order)
	{
		return applyWithCachingEnabled(ctx, order, (c, o) -> super.getPromotionResults(c, o)); //NOSONAR
	}

	@Override
	public void cleanupCart(final SessionContext ctx, final Cart cart)
	{
		acceptWithCachingEnabled(ctx, cart, (c, o) -> super.cleanupCart(c, (Cart) o));
	}

	@Override
	public void transferPromotionsToOrder(final SessionContext ctx, final AbstractOrder source, final Order target,
			final boolean onlyTransferAppliedPromotions)
	{
		try
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("transferPromotionsToOrder from [" + source + "] to [" + target + "] onlyTransferAppliedPromotions=["
						+ onlyTransferAppliedPromotions + "]");

				LOG.debug("Dump Source Order\r\n" + Helper.dumpOrder(ctx, source));
				LOG.debug("Dump Target Order\r\n" + Helper.dumpOrder(ctx, target));
			}

			// Get all the promotion results that we need to transfer
			final List<PromotionResult> promotionResults = applyWithCachingEnabled(ctx, source,
					(c, o) -> getPromotionResultsInternal(c, o)); // NOSONAR

			if (promotionResults != null && !promotionResults.isEmpty())
			{
				promotionResults.stream().filter(r -> !onlyTransferAppliedPromotions || r.isApplied(ctx))
						.forEach(r -> r.transferToOrder(ctx, target));
			}

			if (LOG.isDebugEnabled())
			{
				LOG.debug("transferPromotionsToOrder completed");
				LOG.debug("Dump Target Order after transfer\r\n" + Helper.dumpOrder(ctx, target));
			}
		}
		catch (final Exception ex)
		{
			LOG.error("Failed to transferPromotionsToOrder", ex);
		}
	}

	@Override
	protected List<PromotionResult> getPromotionResultsInternal(final SessionContext ctx, final AbstractOrder order)
	{

		List<PromotionResult> results;

		if (isCachingAllowed(ctx))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Using Promotion Results from Cache");
			}
			results = cache.get(order.getCode(ctx));
			if (isNull(results))
			{
				results = emptyList();
			}
		}
		else
		{
			results = getNonCachedPromotionResultsInternal(ctx, order);
		}
		return results;
	}

	@Override
	protected void deleteStoredPromotionResults(final SessionContext ctx, final AbstractOrder order, final boolean undoActions) //NOSONAR
	{
		if (isCachingAllowed(ctx))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Using Promotion Results from Cache");
			}
			final List<PromotionResult> results = cache.get(order.getCode(ctx));

			if (results != null)
			{

				boolean calculateTotals = false;

				if (undoActions)
				{
					for (final PromotionResult result : results) //NOSONAR
					{
						calculateTotals |= result.undo(ctx);
					}
				}

				if (calculateTotals)
				{
					try //NOSONAR
					{
						order.calculateTotals(true); //NOSONAR
					}
					catch (final JaloPriceFactoryException ex)
					{
						LOG.error("deleteStoredPromotionResult failed to calculateTotals on order [" + order + "]", ex);
					}
				}

				cache.remove(order.getCode(ctx));
			}
		}
		else
		{
			super.deleteStoredPromotionResults(ctx, order, undoActions);
		}
	}

	@Override
	public PromotionOrderEntryConsumed createPromotionOrderEntryConsumed(final SessionContext ctx, final String code,
			final AbstractOrderEntry orderEntry, final long quantity)
	{
		if (isCachingAllowed(ctx))
		{
			// get the current unit price from the orderEntry
			final double unitPrice = orderEntry.getBasePrice(ctx).doubleValue();

			final Map parameters = new HashMap();
			parameters.put(PromotionOrderEntryConsumed.CODE, code);
			parameters.put(PromotionOrderEntryConsumed.ORDERENTRY, orderEntry);
			parameters.put(PromotionOrderEntryConsumed.QUANTITY, Long.valueOf(quantity));

			// Default value for adjusted unit price is the real unit price
			parameters.put(PromotionOrderEntryConsumed.ADJUSTEDUNITPRICE, Double.valueOf(unitPrice));
			return super.createCachedPromotionOrderEntryConsumed(ctx, parameters);
		}
		return super.createPromotionOrderEntryConsumed(ctx, code, orderEntry, quantity);
	}

	@Override
	public PromotionOrderEntryConsumed createPromotionOrderEntryConsumed(final SessionContext ctx, final String code,
			final AbstractOrderEntry orderEntry, final long quantity, final double adjustedUnitPrice)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(PromotionOrderEntryConsumed.CODE, code);
			parameters.put(PromotionOrderEntryConsumed.ORDERENTRY, orderEntry);
			parameters.put(PromotionOrderEntryConsumed.QUANTITY, Long.valueOf(quantity));
			parameters.put(PromotionOrderEntryConsumed.ADJUSTEDUNITPRICE, Double.valueOf(adjustedUnitPrice));
			return super.createCachedPromotionOrderEntryConsumed(ctx, parameters);
		}
		return super.createPromotionOrderEntryConsumed(ctx, code, orderEntry, quantity, adjustedUnitPrice);
	}

	@Override
	public PromotionResult createPromotionResult(final SessionContext ctx, final AbstractPromotion promotion,
			final AbstractOrder order, final float certainty)
	{
		if (promotion == null || order == null || certainty < 0.0F || certainty > 1.0F)
		{
			throw new PromotionException("Invalid attempt to create a promotion result");
		}

		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(PromotionResult.PROMOTION, promotion);
			parameters.put(PromotionResult.ORDER, order);
			parameters.put(PromotionResult.CERTAINTY, Float.valueOf(certainty));
			return super.createCachedPromotionResult(ctx, parameters);
		}
		return super.createPromotionResult(ctx, promotion, order, certainty);
	}

	@Override
	public PromotionOrderAdjustTotalAction createPromotionOrderAdjustTotalAction(final SessionContext ctx,
			final double totalAdjustment)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderAdjustTotalAction.AMOUNT, Double.valueOf(totalAdjustment));
			return super.createCachedPromotionOrderAdjustTotalAction(ctx, parameters);
		}
		return super.createPromotionOrderAdjustTotalAction(ctx, totalAdjustment);
	}

	@Override
	public PromotionOrderAddFreeGiftAction createPromotionOrderAddFreeGiftAction(final SessionContext ctx, final Product product, // NOSONAR
			final PromotionResult result)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderAddFreeGiftAction.FREEPRODUCT, product);
			parameters.put(AbstractPromotionAction.PROMOTIONRESULT, result);
			return super.createCachedPromotionOrderAddFreeGiftAction(ctx, parameters);
		}
		return super.createPromotionOrderAddFreeGiftAction(ctx, product, result);
	}

	@Override
	public PromotionOrderChangeDeliveryModeAction createPromotionOrderChangeDeliveryModeAction(final SessionContext ctx,
			final DeliveryMode deliveryMode)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderChangeDeliveryModeAction.DELIVERYMODE, deliveryMode);
			return super.createCachedPromotionOrderChangeDeliveryModeAction(ctx, parameters);
		}
		return super.createPromotionOrderChangeDeliveryModeAction(ctx, deliveryMode);
	}

	@Override
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx, final Product product, // NOSONAR
			final long quantity, final double adjustment)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, product);
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, Long.valueOf(quantity));
			return super.createCachedPromotionOrderEntryAdjustAction(ctx, parameters);
		}
		return super.createPromotionOrderEntryAdjustAction(ctx, product, quantity, adjustment); // NOSONAR
	}

	@Override
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx,
			final AbstractOrderEntry entry, final long quantity, final double adjustment)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, entry.getProduct(ctx));
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYNUMBER, entry.getEntryNumber());
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, Long.valueOf(quantity));
			return super.createCachedPromotionOrderEntryAdjustAction(ctx, parameters);
		}
		return super.createPromotionOrderEntryAdjustAction(ctx, entry, quantity, adjustment);
	}

	@Override
	public PromotionOrderEntryAdjustAction createPromotionOrderEntryAdjustAction(final SessionContext ctx,
			final AbstractOrderEntry entry, final double adjustment)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			parameters.put(PromotionOrderEntryAdjustAction.AMOUNT, Double.valueOf(adjustment));
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYPRODUCT, entry.getProduct(ctx));
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYNUMBER, entry.getEntryNumber());
			parameters.put(PromotionOrderEntryAdjustAction.ORDERENTRYQUANTITY, entry.getQuantity(ctx));
			return super.createCachedPromotionOrderEntryAdjustAction(ctx, parameters);
		}
		return super.createPromotionOrderEntryAdjustAction(ctx, entry, adjustment);
	}

	@Override
	public PromotionNullAction createPromotionNullAction(final SessionContext ctx)
	{
		if (isCachingAllowed(ctx))
		{
			final Map parameters = new HashMap();
			parameters.put(AbstractPromotionAction.GUID, makeActionGUID());
			return super.createCachedPromotionNullAction(ctx, parameters);
		}
		return super.createPromotionNullAction(ctx);
	}

	@Override
	public void removeFromAllPromotionResults(final SessionContext ctx, final AbstractOrder item, final PromotionResult value)
	{
		acceptWithCachingEnabled(ctx, item, (c, o) ->
		{
			if (isCachingAllowed(c))
			{
				cache.remove(o.getCode(c));
			}
			else
			{
				super.removeFromAllPromotionResults(c, o, value);
			}
		});
	}

	protected PromotionOrderResults doUpdatePromotions(final SessionContext ctx, final Collection<PromotionGroup> promotionGroups,
			final AbstractOrder order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		final PromotionOrderResults orderResults = doUpdatePromotionsOutOfCache(ctx, promotionGroups, order, evaluateRestrictions,
				productPromotionMode, orderPromotionMode, date);
		if (isCachingAllowed(ctx))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Putting the Promotion Results in Cache (OrderCode, PromotionOrderResults.getAllResults() )");
			}
			cache.put(order.getCode(ctx), orderResults.getAllResults());
		}
		return orderResults;
	}

	protected PromotionOrderResults doUpdatePromotionsOutOfCache(final SessionContext ctx,
			final Collection<PromotionGroup> promotionGroups, final AbstractOrder order, final boolean evaluateRestrictions,
			final AutoApplyMode productPromotionMode, final AutoApplyMode orderPromotionMode, final Date date)
	{
		return super.updatePromotions(ctx, promotionGroups, order, evaluateRestrictions, productPromotionMode, orderPromotionMode,
				date);
	}

	protected List<PromotionResult> getNonCachedPromotionResultsInternal(final SessionContext ctx, final AbstractOrder order)
	{
		return super.getPromotionResultsInternal(ctx, order);
	}

	protected <T> T applyWithCachingEnabled(final SessionContext ctx, final AbstractOrder order,
			final BiFunction<SessionContext, AbstractOrder, T> function)
	{
		final JaloSession js = getCurrentJaloSession();
		final SessionContext lCtx = js.createLocalSessionContext(ctx);
		try
		{
			setCachingAllowed(lCtx, order);
			return function.apply(lCtx, order);
		}
		finally
		{
			js.removeLocalSessionContext();
		}
	}

	protected JaloSession getCurrentJaloSession()
	{
		return JaloSession.getCurrentSession();
	}

	protected void acceptWithCachingEnabled(final SessionContext ctx, final AbstractOrder order,
			final BiConsumer<SessionContext, AbstractOrder> consumer)
	{

		final JaloSession js = getCurrentJaloSession();
		final SessionContext lCtx = js.createLocalSessionContext(ctx);
		try
		{
			setCachingAllowed(lCtx, order);
			consumer.accept(lCtx, order);
		}
		finally
		{
			js.removeLocalSessionContext();
		}
	}

	protected void setCachingAllowed(final SessionContext ctx, final AbstractOrder order)
	{
		ctx.setAttribute(CACHING_ALLOWED, Boolean.valueOf(order instanceof Cart));
	}

	protected boolean isCachingAllowed(final SessionContext ctx)
	{
		return isTrue(ctx.getAttribute(CACHING_ALLOWED));
	}

	public CachingStrategy getCache()
	{
		return cache;
	}

	public void setCache(final CachingStrategy cache)
	{
		this.cache = cache;
	}
}
