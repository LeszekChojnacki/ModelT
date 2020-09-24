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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.promotionengineservices.constants.PromotionEngineServicesConstants.PromotionCertainty;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionActionService;
import de.hybris.platform.promotions.PromotionResultService;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;
import de.hybris.platform.ruleengineservices.order.dao.ExtendedOrderDao;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.model.order.InMemoryCartModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.DiscountValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link PromotionActionService}.
 *
 */
public class DefaultPromotionActionService implements PromotionActionService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPromotionActionService.class);
	private ModuleVersioningService moduleVersioningService;
	private ProductDao productDao;
	private CalculationService calculationService;
	private ExtendedOrderDao extendedOrderDao;
	private ModelService modelService;
	private EngineRuleDao engineRuleDao;
	private PromotionResultService promotionResultService;
	private CartService cartService;

	@Override
	public void recalculateTotals(final AbstractOrderModel order)
	{
		try
		{
			getCalculationService().calculateTotals(order, true);
		}
		catch (final CalculationException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			// Do something really useful if Calculation fails
			order.setCalculated(Boolean.FALSE);
			getModelService().save(order);
		}
	}

	@Override
	public void recalculateFiredPromotionMessage(final PromotionResultModel promoResult)
	{
		promoResult.setMessageFired(getPromotionResultService().getDescription(promoResult));
		getModelService().save(promoResult);
	}

	@Override
	public PromotionResultModel createPromotionResult(final AbstractRuleActionRAO actionRao)
	{
		AbstractOrderModel order = getOrderInternal(actionRao);
		if (order == null)
		{
			final AbstractOrderEntryModel orderEntry = getOrderEntry(actionRao);
			if (orderEntry != null)
			{
				order = orderEntry.getOrder();
			}
		}

		final AbstractRuleEngineRuleModel engineRule = getRule(actionRao);
		PromotionResultModel promoResult = findExistingPromotionResultModel(engineRule, order);
		if (isNull(promoResult))
		{
			promoResult = getModelService().create(PromotionResultModel.class);
		}
		promoResult.setOrder(order);
		if (nonNull(order))
		{
			promoResult.setOrderCode(order.getCode());
		}
		promoResult.setPromotion(getPromotion(actionRao));
		promoResult.setRulesModuleName(actionRao.getModuleName());

		if(StringUtils.isEmpty(promoResult.getMessageFired()))
		{
			promoResult.setMessageFired(getPromotionResultService().getDescription(promoResult));
		}

		if (nonNull(engineRule))
		{
			promoResult.setRuleVersion(engineRule.getVersion());
			setRuleModuleVersionIfApplicable(promoResult, engineRule);
		}

		final Collection<PromotionOrderEntryConsumedModel> newConsumedEntries = createConsumedEntries(actionRao);

		if (CollectionUtils.isEmpty(promoResult.getConsumedEntries()))
		{
			promoResult.setConsumedEntries(newConsumedEntries);
		}
		else if (CollectionUtils.isNotEmpty(newConsumedEntries))
		{
			final List<PromotionOrderEntryConsumedModel> allConsumedEntries = Lists.newArrayList(promoResult.getConsumedEntries());
			allConsumedEntries.addAll(newConsumedEntries);
			promoResult.setConsumedEntries(allConsumedEntries);
		}
		if (actionRao instanceof DisplayMessageRAO)
		{
			promoResult.setCertainty(PromotionCertainty.POTENTIAL.value());
		}
		else
		{
			promoResult.setCertainty(PromotionCertainty.FIRED.value());
		}

		return promoResult;
	}

	protected void setRuleModuleVersionIfApplicable(final PromotionResultModel promoResult, final AbstractRuleEngineRuleModel rule)
	{
		promoResult.setModuleVersion(moduleVersioningService.getModuleVersion(rule).orElse(null));
	}

	/**
	 * Tries to find an existing promotion result that has been fired by the same rule as the given rule.
	 *
	 * @param rule
	 *           the rule
	 * @param order
	 *           the order
	 * @return an existing promotion result or null if none is found
	 */
	protected PromotionResultModel findExistingPromotionResultModel(final AbstractRuleEngineRuleModel rule,
			final AbstractOrderModel order)
	{
		if (rule != null && order != null)
		{
			final Set<PromotionResultModel> results = order.getAllPromotionResults();
			for (final PromotionResultModel result : results)
			{
				final Collection<AbstractPromotionActionModel> actions = result.getActions();
				if (actions.stream().filter(a -> a instanceof AbstractRuleBasedPromotionActionModel)
						.map(a -> (AbstractRuleBasedPromotionActionModel) a)
						.anyMatch(a -> a.getRule() != null && rule.getPk().equals(a.getRule().getPk())))
				{
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a list of consumed order entries for the given action.
	 *
	 * @param action
	 *           Action for which consumed order entries should be created
	 * @return Collection of created consumed order entries for given action
	 */
	protected Collection<PromotionOrderEntryConsumedModel> createConsumedEntries(final AbstractRuleActionRAO action)
	{
		List<PromotionOrderEntryConsumedModel> promotionOrderEntriesConsumed = null;
		if (nonNull(action) && nonNull(action.getConsumedEntries()))
		{
			final List<OrderEntryConsumedRAO> orderEntryConsumedRAOsForRule = action.getConsumedEntries().stream()
					.filter(oec -> oec.getFiredRuleCode() != null && oec.getFiredRuleCode().equals(action.getFiredRuleCode()))
					.collect(Collectors.toList());

			promotionOrderEntriesConsumed = new ArrayList<>();
			for (final OrderEntryConsumedRAO orderEntryConsumedRAO : orderEntryConsumedRAOsForRule)
			{
				final PromotionOrderEntryConsumedModel promotionOrderEntryConsumed = getModelService().create(
						PromotionOrderEntryConsumedModel.class);

				final AbstractOrderEntryModel orderEntry = getOrderEntry(orderEntryConsumedRAO.getOrderEntry());
				promotionOrderEntryConsumed.setOrderEntry(orderEntry);
				promotionOrderEntryConsumed.setOrderEntryNumber(orderEntry.getEntryNumber());

				promotionOrderEntryConsumed.setQuantity(Long.valueOf(orderEntryConsumedRAO.getQuantity()));
				if (orderEntryConsumedRAO.getAdjustedUnitPrice() != null)
				{
					promotionOrderEntryConsumed.setAdjustedUnitPrice(Double.valueOf(orderEntryConsumedRAO.getAdjustedUnitPrice()
							.doubleValue()));
				}
				promotionOrderEntriesConsumed.add(promotionOrderEntryConsumed);
			}
		}

		return promotionOrderEntriesConsumed;
	}

	@Override
	public void createDiscountValue(final DiscountRAO discountRao, final String code, final AbstractOrderModel order)
	{
		final boolean isAbsoluteDiscount = discountRao.getCurrencyIsoCode() != null;
		final DiscountValue discountValue = new DiscountValue(code, discountRao.getValue().doubleValue(), isAbsoluteDiscount,
				order.getCurrency().getIsocode());
		final List<DiscountValue> globalDVs = new ArrayList<>(order.getGlobalDiscountValues());
		globalDVs.add(discountValue);
		order.setGlobalDiscountValues(globalDVs);
		order.setCalculated(Boolean.FALSE);
	}

	/**
	 * Removes the {@code DiscountValue} with the given code from the given list of {@code DiscountValue}. Then calls
	 * setNewDiscountValues handler to process the newly gotten list (already w/o the removed item) of
	 * {@code DiscountValue}.
	 *
	 * @param setNewDiscountValues
	 *           if null its method accept(..) is not called.
	 * @return true if the DiscountValue is removed, false - otherwise
	 */
	protected boolean removeDiscount(final String code, final List<DiscountValue> discountValuesList,
			final Consumer<List<DiscountValue>> setNewDiscountValues)
	{
		final List<DiscountValue> filteredDVs = discountValuesList.stream().filter(dv -> !code.equals(dv.getCode()))
				.collect(Collectors.toList());
		final boolean changed = filteredDVs.size() != discountValuesList.size();
		if (setNewDiscountValues != null && changed)
		{
			setNewDiscountValues.accept(filteredDVs);
		}
		return changed;
	}

	/**
	 * Removes the {@code DiscountValue} with the given code from the given {@code order}. Note: The Order is not saved!
	 *
	 * @return true if the DiscountValue is removed, false - otherwise
	 */
	protected boolean removeOrderLevelDiscount(final String code, final AbstractOrderModel order)
	{
		return removeDiscount(code, order.getGlobalDiscountValues(), dvs -> order.setGlobalDiscountValues(dvs));
	}

	/**
	 * Removes the {@code DiscountValue} with the given code from the given {@code orderEntry}. Note: The OrderEntry is
	 * not saved!
	 *
	 * @return true if the DiscountValue is removed, false - otherwise
	 */
	protected boolean removeOrderEntryLevelDiscount(final String code, final AbstractOrderEntryModel orderEntry)
	{
		return removeDiscount(code, orderEntry.getDiscountValues(), dvs -> orderEntry.setDiscountValues(dvs));
	}

	/**
	 * Removes the {@code DiscountValue} with the given code from the OrderEntries of the given {@code order}. Note: The
	 * OrderEntries and Order are not saved!
	 *
	 * @return list of affected OrderEntries
	 */
	protected List<ItemModel> removeOrderEntryLevelDiscounts(final String code, final AbstractOrderModel order)
	{
		return order.getEntries().stream().filter(entry -> removeOrderEntryLevelDiscount(code, entry)).collect(Collectors.toList());
	}

	@Override
	public List<ItemModel> removeDiscountValue(final String code, final AbstractOrderModel order)
	{
		final List<ItemModel> modifiedItems = new LinkedList<>();
		if (removeOrderLevelDiscount(code, order))
		{
			modifiedItems.add(order);
		}
		modifiedItems.addAll(removeOrderEntryLevelDiscounts(code, order));

		return modifiedItems;
	}

	@Override
	public void createDiscountValue(final DiscountRAO discountRao, final String code, final AbstractOrderEntryModel orderEntry)
	{
		final boolean isAbsoluteDiscount = Objects.nonNull(discountRao.getCurrencyIsoCode());
		final DiscountValue discountValue = new DiscountValue(code, discountRao.getValue().doubleValue(), isAbsoluteDiscount,
				orderEntry.getOrder().getCurrency().getIsocode());
		final List<DiscountValue> globalDVs = new ArrayList<>(orderEntry.getDiscountValues());
		globalDVs.add(discountValue);
		orderEntry.setDiscountValues(globalDVs);
		orderEntry.setCalculated(Boolean.FALSE);
	}

	@Override
	public AbstractOrderEntryModel getOrderEntry(final AbstractRuleActionRAO action)
	{
		validateParameterNotNull(action, "action must not be null");
		if (!(action.getAppliedToObject() instanceof OrderEntryRAO))
		{
			return null;
		}
		return getOrderEntry((OrderEntryRAO) action.getAppliedToObject());
	}

	protected AbstractOrderEntryModel getOrderEntry(final OrderEntryRAO orderEntryRao)
	{
		validateParameterNotNull(orderEntryRao, "orderEntryRao must not be null");
		validateParameterNotNull(orderEntryRao.getEntryNumber(), "orderEntryRao.entryNumber must not be null");
		validateParameterNotNull(orderEntryRao.getProduct(), "orderEntryRao.product must not be null");
		validateParameterNotNull(orderEntryRao.getProduct().getCode(), "orderEntryRao.product.code must not be null");
		final AbstractOrderModel order = getOrder(orderEntryRao.getOrder());
		if (order == null)
		{
			return null;
		}
		for (final AbstractOrderEntryModel entry : order.getEntries())
		{
			if (orderEntryRao.getEntryNumber().equals(entry.getEntryNumber())
					&& orderEntryRao.getProduct().getCode().equals(entry.getProduct().getCode()))
			{
				return entry;
			}
		}
		return null;

	}

	@Override
	public AbstractOrderModel getOrder(final AbstractRuleActionRAO action)
	{
		final AbstractOrderModel order = getOrderInternal(action);
		if (order == null)
		{
			LOG.error("cannot look-up order for action: {}", action);
		}
		return order;
	}

	/**
	 * tries to look up the order for the given action.
	 *
	 * @param action
	 *           the action
	 * @return the order or null
	 */
	protected AbstractOrderModel getOrderInternal(final AbstractRuleActionRAO action)
	{
		validateParameterNotNull(action, "action rao must not be null");
		AbstractOrderRAO orderRao = null;
		if (action.getAppliedToObject() instanceof OrderEntryRAO)
		{
			final OrderEntryRAO entry = (OrderEntryRAO) action.getAppliedToObject();
			orderRao = entry.getOrder();
		}

		else if (action.getAppliedToObject() instanceof AbstractOrderRAO)
		{
			orderRao = (AbstractOrderRAO) action.getAppliedToObject();
		}
		if (orderRao != null)
		{
			return getOrder(orderRao);
		}
		return null;
	}

	/**
	 * returns the {@code RuleBasedPromotionModel} for the given {@code AbstractRuleActionRAO} by looking up the rule
	 * code as stored in {@link AbstractRuleActionRAO#getFiredRuleCode()}.
	 *
	 * @param abstractRao
	 *           the rao to get the promotion for
	 * @return the promotion or null
	 */
	protected RuleBasedPromotionModel getPromotion(final AbstractRuleActionRAO abstractRao)
	{
		RuleBasedPromotionModel promotionModel = null;
		if (nonNull(abstractRao) && nonNull(abstractRao.getFiredRuleCode()))
		{
			final AbstractRuleEngineRuleModel rule = getRule(abstractRao);
			if (nonNull(rule))
			{
				promotionModel = rule.getPromotion();
			}
			else
			{
				LOG.error("Cannot get promotion for AbstractRuleActionRAO: {}. No rule found for code: {}", abstractRao,
						abstractRao.getFiredRuleCode());
			}
		}
		return promotionModel;
	}

	/**
	 * returns the {@code AbstractRuleEngineRuleModel} for the given {@code AbstractRuleActionRAO} by looking up the rule
	 * code as stored in {@link AbstractRuleActionRAO#getFiredRuleCode()}.
	 *
	 * @param abstractRao
	 *           the rao to get the rule for
	 * @return the rule or null if the rao is null, its firedRuleCode is null or if the rule cannot be found
	 */
	@Override
	public AbstractRuleEngineRuleModel getRule(final AbstractRuleActionRAO abstractRao)
	{
		AbstractRuleEngineRuleModel ruleModel = null;

		if (nonNull(abstractRao) && nonNull(abstractRao.getFiredRuleCode()))
		{
			final String firedRuleCode = abstractRao.getFiredRuleCode();
			try
			{
				final Optional<Long> deployedModuleVersion = getModuleVersioningService().getDeployedModuleVersionForRule(
						firedRuleCode, abstractRao.getModuleName());
				ruleModel = deployedModuleVersion.map(
						v -> getEngineRuleDao().getActiveRuleByCodeAndMaxVersion(firedRuleCode, abstractRao.getModuleName(),
								v.longValue())).orElse(null);
			}
			catch (final ModelNotFoundException e)
			{
				LOG.error("cannot get rule from AbstractRuleActionRAO: {}. No rule found for code: {}", abstractRao, firedRuleCode);
			}
		}
		return ruleModel;
	}

	protected AbstractOrderModel getOrder(final AbstractOrderRAO orderRao)
	{
		AbstractOrderModel order = null;
		final String orderCode = orderRao.getCode();
		try
		{
			order = getExtendedOrderDao().findOrderByCode(orderCode);
		}
		catch (final ModelNotFoundException ex)
		{
			LOG.debug(String
					.format(
							"Cannot look-up AbstractOrder for code '%s', order not found. Trying session cart instead (InMemoryCart support)",
							orderCode));

			if (getCartService().hasSessionCart())
			{
   			final CartModel sessionCart = getCartService().getSessionCart();

   			if (sessionCart instanceof InMemoryCartModel && orderCode.equals(sessionCart.getCode()))
   			{
   				return sessionCart;
   			}
			}

			LOG.error("Cannot find the cart with code {} in the database nor in the session", orderCode);
			return null;
		}
		return order;
	}


	protected ExtendedOrderDao getExtendedOrderDao()
	{
		return extendedOrderDao;
	}

	@Required
	public void setExtendedOrderDao(final ExtendedOrderDao extendedOrderDao)
	{
		this.extendedOrderDao = extendedOrderDao;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected ModelService getModelService()
	{
		return this.modelService;
	}

	protected CalculationService getCalculationService()
	{
		return calculationService;
	}

	@Required
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	protected ProductDao getProductDao()
	{
		return productDao;
	}

	@Required
	public void setProductDao(final ProductDao productDao)
	{
		this.productDao = productDao;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected ModuleVersioningService getModuleVersioningService()
	{
		return moduleVersioningService;
	}

	@Required
	public void setModuleVersioningService(final ModuleVersioningService moduleVersioningService)
	{
		this.moduleVersioningService = moduleVersioningService;
	}

	protected PromotionResultService getPromotionResultService()
	{
		return promotionResultService;
	}

	@Required
	public void setPromotionResultService(final PromotionResultService promotionResultService)
	{
		this.promotionResultService = promotionResultService;
	}

	protected CartService getCartService()
	{
		return cartService;
	}

	@Required
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}
}
