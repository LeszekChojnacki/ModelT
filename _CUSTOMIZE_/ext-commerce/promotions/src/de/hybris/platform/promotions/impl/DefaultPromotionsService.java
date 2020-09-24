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
package de.hybris.platform.promotions.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.jalo.PromotionGroup;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.AbstractPromotionRestrictionModel;
import de.hybris.platform.promotions.model.OrderPromotionModel;
import de.hybris.platform.promotions.model.ProductPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;


public class DefaultPromotionsService extends AbstractPromotionsService implements PromotionsService // NOSONAR
{

	protected PromotionsManager getPromotionsManager()
	{
		return PromotionsManager.getInstance();
	}

	@Override
	public void cleanupCart(final CartModel cart)
	{
		getPromotionsManager().cleanupCart(getCart(cart));
	}

	@Override
	public PromotionGroupModel getDefaultPromotionGroup()
	{
		final PromotionGroup defaultPromotionGroup = getPromotionsManager().getDefaultPromotionGroup();
		if (defaultPromotionGroup == null)
		{
			return null;
		}
		else
		{
			return getModelService().get(defaultPromotionGroup);
		}
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups)
	{
		return getModelService().getAll(
				getPromotionsManager()
						.getOrderPromotions(getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>())),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups, final Date date)
	{
		return getModelService().getAll(
				getPromotionsManager()
						.getOrderPromotions(getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), date),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		return getModelService().getAll(
				getPromotionsManager().getOrderPromotions(
						getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getProduct(product)),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product, final Date date)
	{
		return getModelService().getAll(
				getPromotionsManager().getOrderPromotions(
						getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getProduct(product), date),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions)
	{
		return getModelService().getAll(
				getPromotionsManager().getOrderPromotions(
						getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), evaluateRestrictions),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final Date date)
	{
		return getModelService().getAll(
				getPromotionsManager().getOrderPromotions(
						getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), evaluateRestrictions, date),
				new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final ProductModel product)
	{
		return getModelService().getAll(getPromotionsManager().getOrderPromotions(
				getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), evaluateRestrictions,
				getProduct(product)), new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final ProductModel product, final Date date)
	{
		return getModelService().getAll(getPromotionsManager().getOrderPromotions(
				getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), evaluateRestrictions,
				getProduct(product), date), new ArrayList<OrderPromotionModel>());
	}

	@Override
	public List<ProductPromotionModel> getProductPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		return getModelService().getAll(
				getPromotionsManager().getProductPromotions(
						getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getProduct(product)),
				new ArrayList<ProductPromotionModel>());
	}

	@Override
	public List<ProductPromotionModel> getProductPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product, final boolean evaluateRestrictions, final Date date)
	{
		return getModelService().getAll(getPromotionsManager().getProductPromotions(
				getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getProduct(product),
				evaluateRestrictions, date), new ArrayList<ProductPromotionModel>());
	}

	@Override
	public List<? extends AbstractPromotionModel> getAbstractProductPromotions(
			final Collection<PromotionGroupModel> promotionGroups, final ProductModel product)
	{
		return getProductPromotions(promotionGroups, product);
	}

	@Override
	public List<? extends AbstractPromotionModel> getAbstractProductPromotions(
			final Collection<PromotionGroupModel> promotionGroups, final ProductModel product, final boolean evaluateRestrictions,
			final Date date)
	{
		return getProductPromotions(promotionGroups, product, evaluateRestrictions, date);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PromotionGroupModel getPromotionGroup(final String identifier)
	{
		final PromotionGroup promotionGroup = getPromotionsManager().getPromotionGroup(identifier);
		if (promotionGroup == null)
		{
			return null;
		}
		else
		{
			return getModelService().get(promotionGroup);
		}
	}

	@Override
	public PromotionOrderResults getPromotionResults(final AbstractOrderModel order)
	{
		return getPromotionsManager().getPromotionResults(getOrder(order));
	}

	@Override
	public PromotionOrderResults getPromotionResults(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		return getPromotionsManager().getPromotionResults(getSessionContext(),
				getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getOrder(order),
				evaluateRestrictions, productPromotionMode, orderPromotionMode, date);
	}

	@Override
	public void transferPromotionsToOrder(final AbstractOrderModel source, final OrderModel target,
			final boolean onlyTransferAppliedPromotions)
	{
		saveIfModified(source);
		getPromotionsManager().transferPromotionsToOrder(getOrder(source), getOrder(target), onlyTransferAppliedPromotions);

		final List<ItemModel> toRefresh = CollectionUtils.isEmpty(target.getAllPromotionResults()) ? new ArrayList<ItemModel>(1)
				: new ArrayList<ItemModel>(target.getAllPromotionResults());
		toRefresh.add(target);

		refreshModifiedModelsAfter(toRefresh);
	}

	@Override
	public PromotionOrderResults updatePromotions(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order)
	{
		saveIfModified(order);
		final PromotionOrderResults result = getPromotionsManager()
				.updatePromotions(getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getOrder(order));
		refreshOrder(order);
		return result;
	}

	@Override
	public PromotionOrderResults updatePromotions(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		saveIfModified(order);
		final PromotionOrderResults result = getPromotionsManager().updatePromotions(getSessionContext(),
				getModelService().getAllSources(promotionGroups, new ArrayList<PromotionGroup>()), getOrder(order),
				evaluateRestrictions, productPromotionMode, orderPromotionMode, date);
		refreshOrder(order);
		return result;
	}

	protected void refreshOrder(final AbstractOrderModel order)
	{
		List<ItemModel> toRefresh = new ArrayList<ItemModel>(1);
		toRefresh.add(order);
		refreshModifiedModelsAfter(toRefresh);

		toRefresh = CollectionUtils.isEmpty(order.getEntries()) ? new ArrayList<ItemModel>()
				: new ArrayList<ItemModel>(order.getEntries());
		refreshModifiedModelsAfter(toRefresh);
	}

	protected void saveIfModified(final AbstractOrderModel order)
	{
		if (getModelService().isModified(order))
		{
			getModelService().save(order);
		}
		if (order.getEntries() != null)
		{
			final Collection<AbstractOrderEntryModel> orderEntries = order.getEntries().stream()
					.filter(oe -> getModelService().isModified(oe)).collect(Collectors.toList());
			if (!orderEntries.isEmpty())
			{
				getModelService().saveAll(orderEntries);
			}
		}
	}

	@Override
	public Collection<AbstractPromotionRestrictionModel> getRestrictions(final AbstractPromotionModel promotion)
	{
		return getModelService().getAll(getPromotion(promotion).getRestrictions(),
				new ArrayList<AbstractPromotionRestrictionModel>());
	}

	@Override
	public String getPromotionDescription(final AbstractPromotionModel promotion)
	{
		return promotion.getDescription();
	}
}
