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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.google.common.collect.Streams;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotionengineservices.dao.PromotionDao;
import de.hybris.platform.promotionengineservices.dao.PromotionSourceRuleDao;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionEngineService;
import de.hybris.platform.promotionengineservices.validators.RuleBasedPromotionsContextValidator;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.impl.DefaultPromotionsService;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.AbstractPromotionRestrictionModel;
import de.hybris.platform.promotions.model.OrderPromotionModel;
import de.hybris.platform.promotions.model.ProductPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleEvaluationResult;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.dao.RuleEngineContextDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.exception.RuleEngineRuntimeException;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextFinderStrategy;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextForCatalogVersionsFinderStrategy;
import de.hybris.platform.ruleengineservices.action.RuleActionService;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.enums.FactContextType;
import de.hybris.platform.ruleengineservices.rao.providers.FactContextFactory;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rao.providers.impl.FactContext;
import de.hybris.platform.ruleengineservices.util.ProductUtils;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.session.SessionService.SessionAttributeLoader;
import de.hybris.platform.servicelayer.time.TimeService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of Promotion Engine Service that uses Rule Engine Service to evaluate promotions.
 */
public class DefaultPromotionEngineService implements PromotionEngineService, PromotionsService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPromotionEngineService.class);

	private static final String GET_PROMOTIONS_FOR_PRODUCT_PROPERTY = "promotionengineservices.getpromotionsforproduct.disable";
	private static final String NO_RULE_ENGINE_CTX_FOR_ORDER_ERROR_MESSAGE = "No rule engine context could be derived for order [%s]";
	private static final String NO_RULE_ENGINE_CTX_FOR_PRODUCT_ERROR_MESSAGE = "No rule engine context could be derived for product [%s]";

	private RuleEngineService commerceRuleEngineService;
	private CalculationService calculationService;
	private RuleEngineContextDao ruleEngineContextDao;
	private RuleActionService ruleActionService;
	private List<RuleActionStrategy> strategies;
	private EngineRuleDao engineRuleDao;
	private FlexibleSearchService flexibleSearchService;
	private PromotionDao promotionDao;
	private FactContextFactory factContextFactory;
	private DefaultPromotionsService defaultPromotionsService;
	private PromotionSourceRuleDao promotionSourceRuleDao;
	private CategoryService categoryService;
	private ConfigurationService configurationService;
	private CatalogVersionService catalogVersionService;
	private RuleEngineContextForCatalogVersionsFinderStrategy ruleEngineContextForCatalogVersionsFinderStrategy;
	private RuleEngineContextFinderStrategy ruleEngineContextFinderStrategy;
	private RuleBasedPromotionsContextValidator ruleBasedPromotionsContextValidator;
	private SessionService sessionService;
	private ModelService modelService;
	private TimeService timeService;
	private ProductUtils productUtils;

	@Override
	public RuleEvaluationResult evaluate(final ProductModel product, final Collection<PromotionGroupModel> promotionGroups)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Promotion product evaluation triggered for product with code '{}'", product.getCode());
		}

		final List<Object> facts = new ArrayList<>();
		facts.add(product);
		facts.addAll(promotionGroups);
		final RuleEvaluationContext context = prepareContext(
				getFactContextFactory().createFactContext(FactContextType.PROMOTION_PRODUCT, facts),
				determineRuleEngineContext(product));
		return getCommerceRuleEngineService().evaluate(context);
	}

	@Override
	public RuleEvaluationResult evaluate(final AbstractOrderModel order, final Collection<PromotionGroupModel> promotionGroups)
	{
		return evaluate(order, promotionGroups, getTimeService().getCurrentTime());
	}

	@Override
	public RuleEvaluationResult evaluate(final AbstractOrderModel order, final Collection<PromotionGroupModel> promotionGroups,
			final Date date)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Promotion cart evaluation triggered for cart with code '{}'", order.getCode());
		}

		final List<Object> facts = Lists.newArrayList();
		facts.add(order);
		facts.addAll(promotionGroups);
		if (nonNull(date))
		{
			facts.add(date);
		}
		try
		{
			final RuleEvaluationContext context = prepareContext(
					getFactContextFactory().createFactContext(FactContextType.PROMOTION_ORDER, facts),
					determineRuleEngineContext(order));
			return getCommerceRuleEngineService().evaluate(context);
		}
		catch (final IllegalStateException e)
		{
			LOGGER.error("Promotion rule evaluation failed", e);
			final RuleEvaluationResult result = new RuleEvaluationResult();
			result.setErrorMessage(e.getMessage());
			result.setEvaluationFailed(true);
			return result;
		}

	}

	/**
	 * Sets up the rule evaluation context using the provided factContext with facts and rao providers and sets the given
	 * ruleEngineContext.
	 *
	 * @param factContext
	 * 		FactContext to use for the evaluation
	 * @param ruleEngineContext
	 * 		the rule engine context to be used during rule evaluation
	 * @return the rule evaluation context
	 */
	protected RuleEvaluationContext prepareContext(final FactContext factContext,
			final AbstractRuleEngineContextModel ruleEngineContext)
	{
		final Set<Object> convertedFacts = provideRAOs(factContext);
		final RuleEvaluationContext evaluationContext = new RuleEvaluationContext();
		evaluationContext.setRuleEngineContext(ruleEngineContext);
		evaluationContext.setFacts(convertedFacts);
		return evaluationContext;

	}

	/**
	 * determines the rule engine context to be used for the given order. This method uses
	 * {@link RuleEngineContextFinderStrategy#findRuleEngineContext(AbstractOrderModel, RuleType)} to
	 * determine the rule engine context to be used.
	 *
	 * @param order
	 * 		the order to be used
	 * @return the rule engine context to be used
	 */
	protected AbstractRuleEngineContextModel determineRuleEngineContext(final AbstractOrderModel order)
	{
		return getRuleEngineContextFinderStrategy().findRuleEngineContext(order, RuleType.PROMOTION)
				.orElseThrow(() -> new IllegalStateException(String.format(NO_RULE_ENGINE_CTX_FOR_ORDER_ERROR_MESSAGE, order)));
	}

	/**
	 * determines the rule engine context to be used for the given product. This method uses
	 * {@link RuleEngineContextFinderStrategy#findRuleEngineContext(ProductModel, RuleType)} to determine the rule engine
	 * context to be used.
	 *
	 * @param product
	 * 		the product to be used
	 * @return the rule engine context to be used
	 */
	protected AbstractRuleEngineContextModel determineRuleEngineContext(final ProductModel product)
	{
		return getRuleEngineContextFinderStrategy().findRuleEngineContext(product, RuleType.PROMOTION)
				.orElseThrow(
						() -> new IllegalStateException(String.format(NO_RULE_ENGINE_CTX_FOR_PRODUCT_ERROR_MESSAGE, product)));

	}

	/**
	 * Converts the given {@code source} object into an RAO using the configured {@code raoProviders}.
	 *
	 * @param factContext
	 * 		FactContext containing rao providers
	 * @throws RuntimeException
	 * 		if no raoProvider is registered for the given source object
	 */
	protected Set<Object> provideRAOs(final FactContext factContext)
	{
		final Set<Object> result = new HashSet<>();
		for (final Object fact : factContext.getFacts())
		{
			for (final RAOProvider raoProvider : factContext.getProviders(fact))
			{
				result.addAll(raoProvider.expandFactModel(fact));
			}
		}
		return result;
	}

	@Override
	public List<? extends AbstractPromotionModel> getAbstractProductPromotions(
			final Collection<PromotionGroupModel> promotionGroups, final ProductModel product)
	{
		return Collections.emptyList();
	}

	@Override
	public List<? extends AbstractPromotionModel> getAbstractProductPromotions(
			final Collection<PromotionGroupModel> promotionGroups, final ProductModel product,
			final boolean evaluateRestrictions,
			final Date date)
	{
		return getPromotionsForProduct(promotionGroups, product);
	}

	@Override
	public List<ProductPromotionModel> getProductPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		return Collections.emptyList();
	}

	@Override
	public List<ProductPromotionModel> getProductPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product, final boolean evaluateRestrictions, final Date date)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups, final Date date)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product, final Date date)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final Date date)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final ProductModel product)
	{
		return Collections.emptyList();
	}

	@Override
	public List<OrderPromotionModel> getOrderPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final boolean evaluateRestrictions, final ProductModel product, final Date date)
	{
		return Collections.emptyList();
	}

	@Override
	public PromotionOrderResults updatePromotions(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order)
	{
		final Object perSessionLock = getSessionService().getOrLoadAttribute("promotionsUpdateLock",
				(SessionAttributeLoader<Object>) SerializableObject::new);
		synchronized (perSessionLock)
		{
			return updatePromotionsNotThreadSafe(promotionGroups, order);
		}
	}

	protected PromotionOrderResults updatePromotionsNotThreadSafe(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order)
	{
		return updatePromotionsNotThreadSafe(promotionGroups, order, getTimeService().getCurrentTime());
	}

	protected PromotionOrderResults updatePromotionsNotThreadSafe(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final Date date)
	{
		cleanupAbstractOrder(order);
		List<PromotionResult> actionApplicationResults;
		try
		{
			final RuleEvaluationResult ruleEvaluationResult = evaluate(order, promotionGroups, date);
			if (!ruleEvaluationResult.isEvaluationFailed())
			{
				final List<ItemModel> applyAllActionModels = getRuleActionService().applyAllActions(ruleEvaluationResult.getResult());
				final List<PromotionResultModel> applyAllActionsPromotionResultModel = applyAllActionModels.stream()
						.filter(item -> item instanceof PromotionResultModel).map(item -> (PromotionResultModel) item)
						.collect(Collectors.toList());
				actionApplicationResults = getModelService().getAllSources(applyAllActionsPromotionResultModel, new ArrayList());
			}
			else
			{
				actionApplicationResults = Lists.newArrayList();
			}
		}
		catch (final RuleEngineRuntimeException rere)
		{
			LOGGER.error(rere.getMessage(), rere);
			actionApplicationResults = new ArrayList<>();
		}

		getModelService().refresh(order);
		return new PromotionOrderResults(
				JaloSession.getCurrentSession().getSessionContext(), (AbstractOrder) getModelService().getSource(order),
				actionApplicationResults, 0.0D);
	}

	@Override
	public PromotionOrderResults updatePromotions(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{
		final Object perSessionLock = getSessionService().getOrLoadAttribute("promotionsUpdateLock",
				(SessionAttributeLoader<Object>) SerializableObject::new);
		synchronized (perSessionLock)
		{
			return updatePromotionsNotThreadSafe(promotionGroups, order, date);
		}
	}

	@Override
	public PromotionOrderResults getPromotionResults(final AbstractOrderModel order)
	{
		final Set<PromotionResultModel> promotionResultModels = order.getAllPromotionResults();
		final List<PromotionResult> promotionResults = getModelService().getAllSources(promotionResultModels, new ArrayList());

		return new PromotionOrderResults(JaloSession.getCurrentSession().getSessionContext(),
				(AbstractOrder) getModelService().getSource(order), promotionResults, 0.0D);
	}

	@Override
	public PromotionOrderResults getPromotionResults(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final boolean evaluateRestrictions, final AutoApplyMode productPromotionMode,
			final AutoApplyMode orderPromotionMode, final Date date)
	{

		final Set<PromotionResultModel> promotionResultModels = order.getAllPromotionResults();

		if (isNotEmpty(promotionResultModels)
				&& promotionResultModels.iterator().next().getPromotion() instanceof RuleBasedPromotionModel)
		{
			return null;
		}
		else
		{
			return getDefaultPromotionsService().getPromotionResults(promotionGroups, order, evaluateRestrictions,
					productPromotionMode, orderPromotionMode, date);
		}
	}

	@Override
	public void cleanupCart(final CartModel cart)
	{
		cleanupAbstractOrder(cart);
	}

	protected void cleanupAbstractOrder(final AbstractOrderModel cart)
	{
		final Set<PromotionResultModel> promotionResultModels = cart.getAllPromotionResults();

		if (Objects.isNull(promotionResultModels))
		{
			return;
		}
		for (final PromotionResultModel promoResultModel : promotionResultModels)
		{
			for (final AbstractPromotionActionModel action : promoResultModel.getActions())
			{
				if (action instanceof AbstractRuleBasedPromotionActionModel)
				{
					undoPromotionAction(action);
				}
			}
			getModelService().removeAll(promoResultModel.getConsumedEntries());
			getModelService().removeAll(promoResultModel.getActions());
			getModelService().remove(promoResultModel);
		}
		getModelService().refresh(cart);
		recalculateCart(cart);
	}

	protected void undoPromotionAction(final AbstractPromotionActionModel action)
	{
		final AbstractRuleBasedPromotionActionModel promoAction = (AbstractRuleBasedPromotionActionModel) action;
		final RuleActionStrategy ruleActionStrategy = getRuleActionStrategy(promoAction.getStrategyId());
		if (ruleActionStrategy != null)
		{
			ruleActionStrategy.undo(promoAction);
		}
	}

	@Override
	public void transferPromotionsToOrder(final AbstractOrderModel source, final OrderModel target,
			final boolean onlyTransferAppliedPromotions)
	{
		final Set<PromotionResultModel> sourcePromotionResults = source.getAllPromotionResults();
		if (isNotEmpty(sourcePromotionResults))
		{
			final List toSave = newArrayList();
			for (final PromotionResultModel sourcePromoResult : sourcePromotionResults)
			{
				final PromotionResultModel targetPromoResult = getModelService().clone(sourcePromoResult);
				toSave.add(targetPromoResult);
				targetPromoResult.setOrder(target);
				targetPromoResult.setOrderCode(target.getCode());

				final List<AbstractPromotionActionModel> targetActions = new ArrayList<>();
				for (final AbstractPromotionActionModel sourceAction : sourcePromoResult.getActions())
				{
					final AbstractPromotionActionModel targetAction = getModelService().clone(sourceAction);
					targetAction.setPromotionResult(targetPromoResult);
					targetActions.add(targetAction);
					toSave.add(targetAction);
				}
				targetPromoResult.setActions(targetActions);

				final List<PromotionOrderEntryConsumedModel> targetEntriesConsumed = new ArrayList<>();
				for (final PromotionOrderEntryConsumedModel sourceEntryConsumed : sourcePromoResult.getConsumedEntries())
				{
					final PromotionOrderEntryConsumedModel targetEntryConsumed = getModelService().clone(sourceEntryConsumed);
					targetEntryConsumed.setOrderEntry(target.getEntries().stream()
							.filter(entry -> entry.getEntryNumber() != null
									&& entry.getEntryNumber().equals(sourceEntryConsumed.getOrderEntryNumberWithFallback()))
							.findFirst().orElse(null));
					targetEntryConsumed.setPromotionResult(targetPromoResult);
					targetEntriesConsumed.add(targetEntryConsumed);
					toSave.add(targetEntryConsumed);
				}

				targetPromoResult.setConsumedEntries(targetEntriesConsumed);
				targetPromoResult.setPromotion(sourcePromoResult.getPromotion());
			}

			getModelService().saveAll(toSave);
			toSave.forEach(o -> getModelService().refresh(o));
		}
	}

	protected String getDataUniqueKey(final AbstractPromotionModel sourcePromotion)
	{
		final SessionContext ctx = JaloSession.getCurrentSession().getSessionContext();
		final StringBuilder builder = new StringBuilder();
		builder.append(sourcePromotion.getClass().getSimpleName()).append('|');
		if (sourcePromotion.getPromotionGroup() != null)
		{
			builder.append(sourcePromotion.getPromotionGroup().getIdentifier()).append('|');
		}
		builder.append(sourcePromotion.getCode()).append('|').append(sourcePromotion.getPriority()).append('|')
				.append(ctx.getLanguage().getIsocode()).append('|');

		final Date startDate = sourcePromotion.getStartDate();
		if (startDate == null)
		{
			builder.append("x|");
		}
		else
		{
			builder.append(startDate.getTime()).append('|');
		}

		final Date endDate = sourcePromotion.getEndDate();
		if (endDate == null)
		{
			builder.append("x|");
		}
		else
		{
			builder.append(endDate.getTime()).append('|');
		}

		if (sourcePromotion instanceof RuleBasedPromotionModel)
		{
			final AbstractRuleEngineRuleModel rule = ((RuleBasedPromotionModel) sourcePromotion).getRule();
			if (rule != null && rule.getRuleContent() != null)
			{
				builder.append(rule.getRuleContent()).append('|');
			}
			else
			{
				builder.append("x|");
			}
		}

		return builder.toString();
	}

	static String buildMD5Hash(final String message)
	{
		return DigestUtils.md5Hex(message);
	}

	protected <T> T findImmutablePromotionByUniqueKey(final String immutableKeyHash, final Predicate<T> immutableKeyPredicate)
	{
		final Map<String, String> params = new HashMap<>();
		params.put("immutableKeyHash", immutableKeyHash);

		final String query = "SELECT {" + Item.PK + "} " + "FROM   {" + AbstractPromotionModel._TYPECODE + "} " + "WHERE  {"
				+ "immutableKeyHash" + "} = ?immutableKeyHash";
		final SearchResult<T> searchResult = getFlexibleSearchService().search(query, params);
		if (!searchResult.getResult().isEmpty())
		{
			return searchResult.getResult().stream().filter(immutableKeyPredicate).findFirst().orElse(null);
		}
		return null;
	}

	@Override
	public PromotionGroupModel getDefaultPromotionGroup()
	{
		return getPromotionDao().findDefaultPromotionGroup();
	}

	@Override
	public PromotionGroupModel getPromotionGroup(final String identifier)
	{
		return getPromotionDao().findPromotionGroupByCode(identifier);
	}

	@Override
	public Collection<AbstractPromotionRestrictionModel> getRestrictions(final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return Collections.emptyList();
		}
		else
		{
			return getDefaultPromotionsService().getRestrictions(promotion);
		}
	}

	@Override
	public String getPromotionDescription(final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return ((RuleBasedPromotionModel) promotion).getPromotionDescription();
		}
		else
		{
			return promotion.getDescription();
		}
	}

	/**
	 * returns the {@code RuleActionStrategy} defined in the {@code strategies} attribute of this service by looking up
	 * it's bean id.
	 *
	 * @param strategyId
	 * 		the bean id of the RuleActionStrategy to look up
	 * @return the found bean id
	 */
	protected RuleActionStrategy getRuleActionStrategy(final String strategyId)
	{
		if (strategyId == null)
		{
			LOGGER.error("strategyId is not defined!");
			return null;
		}
		if (getStrategies() != null)
		{
			for (final RuleActionStrategy strategy : getStrategies())
			{
				if (strategyId.equals(strategy.getStrategyId()))
				{
					return strategy;
				}
			}
			LOGGER.error("cannot find RuleActionStrategy for given strategyId:{}", strategyId);
		}
		else
		{
			LOGGER.error(
					"cannot call getRuleActionStrategy(\"{}\"), no strategies are defined! Please configure your {} bean to contain strategies.",
					strategyId, this.getClass().getSimpleName());
		}
		return null;
	}

	protected List<RuleActionStrategy> getStrategies()
	{
		return strategies;
	}

	@Required
	public void setStrategies(final List<RuleActionStrategy> strategies)
	{
		this.strategies = strategies;
	}

	protected boolean recalculateCart(final AbstractOrderModel order)
	{
		try
		{
			getCalculationService().calculateTotals(order, true);
		}
		catch (final CalculationException e)
		{
			LOGGER.error(String.format("Recalculation of order with code '%s' failed.", order.getCode()), e);
			order.setCalculated(Boolean.FALSE);
			getModelService().save(order);
			return false;
		}

		return true;
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

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected PromotionDao getPromotionDao()
	{
		return promotionDao;
	}

	@Required
	public void setPromotionDao(final PromotionDao promotionDao)
	{
		this.promotionDao = promotionDao;
	}

	protected FactContextFactory getFactContextFactory()
	{
		return factContextFactory;
	}

	@Required
	public void setFactContextFactory(final FactContextFactory factContextFactory)
	{
		this.factContextFactory = factContextFactory;
	}

	public DefaultPromotionsService getDefaultPromotionsService()
	{
		return defaultPromotionsService;
	}

	@Required
	public void setDefaultPromotionsService(final DefaultPromotionsService defaultPromotionsService)
	{
		this.defaultPromotionsService = defaultPromotionsService;
	}

	protected RuleEngineService getCommerceRuleEngineService()
	{
		return commerceRuleEngineService;
	}

	@Required
	public void setCommerceRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.commerceRuleEngineService = ruleEngineService;
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

	protected RuleActionService getRuleActionService()
	{
		return ruleActionService;
	}

	@Required
	public void setRuleActionService(final RuleActionService ruleActionService)
	{
		this.ruleActionService = ruleActionService;
	}

	protected RuleEngineContextDao getRuleEngineContextDao()
	{
		return ruleEngineContextDao;
	}

	@Required
	public void setRuleEngineContextDao(final RuleEngineContextDao ruleEngineContextDao)
	{
		this.ruleEngineContextDao = ruleEngineContextDao;
	}

	/**
	 * Get all promotions, sorted by priority, that could be applied for the given product and promotion groups. If
	 * property promotionengineservices.getpromotionsforproduct.disable is set to true, returns an empty list.
	 *
	 * @param promotionGroups
	 * 		collection of promotion groups to apply promotions for
	 * @param product
	 * 		product to get promotions for
	 * @return List of {@link RuleBasedPromotionModel} which could be applied to the given product and are sorted by
	 * priority or empty list if promotionengineservices.getpromotionsforproduct.disable is set to true
	 */
	protected List<RuleBasedPromotionModel> getPromotionsForProduct(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		if (getConfigurationService().getConfiguration().getBoolean(GET_PROMOTIONS_FOR_PRODUCT_PROPERTY))
		{
			LOGGER.info("Promotions for product are disabled. If you want to enable them, please change the property {} to false.",
					GET_PROMOTIONS_FOR_PRODUCT_PROPERTY);
			return Collections.emptyList();
		}

		return Streams.concat(getPromotions(promotionGroups, product).stream(),
				getProductUtils().getAllBaseProducts(product).stream().flatMap(b -> getPromotions(promotionGroups, b).stream()))
				.distinct()
				.filter(p -> isApplicable(product, p))
				.sorted(comparing(RuleBasedPromotionModel::getPriority))
				.collect(Collectors.toList());
	}

	protected ArrayList<RuleBasedPromotionModel> getPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final ProductModel product)
	{
		return Lists.newArrayList(getPromotionSourceRuleDao()
				.findPromotions(promotionGroups, product.getCode(), getCategoryCodesForProduct(product)));
	}

	protected boolean isApplicable(final ProductModel product, final RuleBasedPromotionModel promotion)
	{
		return Objects.nonNull(promotion) && getRuleBasedPromotionsContextValidator()
				.isApplicable(promotion, product.getCatalogVersion(), RuleType.PROMOTION);
	}

	protected Set<String> getCategoryCodesForProduct(final ProductModel product)
	{
		final Set<CategoryModel> allCategories = new HashSet<>();
		final Collection<CategoryModel> supercategories = product.getSupercategories();
		if (isNotEmpty(supercategories))
		{
			allCategories.addAll(supercategories);
			for (final CategoryModel category : supercategories)
			{
				allCategories.addAll(getCategoryService().getAllSupercategoriesForCategory(category));
			}
		}

		final Set<String> allCategoryCodes = new HashSet<>();
		allCategories.forEach(cat -> allCategoryCodes.add(cat.getCode()));
		return allCategoryCodes;
	}

	protected PromotionSourceRuleDao getPromotionSourceRuleDao()
	{
		return promotionSourceRuleDao;
	}

	@Required
	public void setPromotionSourceRuleDao(final PromotionSourceRuleDao promotionSourceRuleDao)
	{
		this.promotionSourceRuleDao = promotionSourceRuleDao;
	}

	protected CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	private static class SerializableObject implements Serializable
	{
		// empty class to provide simple serializable object
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected RuleEngineContextForCatalogVersionsFinderStrategy getRuleEngineContextForCatalogVersionsFinderStrategy()
	{
		return ruleEngineContextForCatalogVersionsFinderStrategy;
	}

	@Required
	public void setRuleEngineContextForCatalogVersionsFinderStrategy(
			final RuleEngineContextForCatalogVersionsFinderStrategy ruleEngineContextForCatalogVersionsFinderStrategy)
	{
		this.ruleEngineContextForCatalogVersionsFinderStrategy = ruleEngineContextForCatalogVersionsFinderStrategy;
	}

	protected RuleEngineContextFinderStrategy getRuleEngineContextFinderStrategy()
	{
		return ruleEngineContextFinderStrategy;
	}

	@Required
	public void setRuleEngineContextFinderStrategy(
			final RuleEngineContextFinderStrategy ruleEngineContextFinderStrategy)
	{
		this.ruleEngineContextFinderStrategy = ruleEngineContextFinderStrategy;
	}

	protected RuleBasedPromotionsContextValidator getRuleBasedPromotionsContextValidator()
	{
		return ruleBasedPromotionsContextValidator;
	}

	@Required
	public void setRuleBasedPromotionsContextValidator(
			final RuleBasedPromotionsContextValidator ruleBasedPromotionsContextValidator)
	{
		this.ruleBasedPromotionsContextValidator = ruleBasedPromotionsContextValidator;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	protected ProductUtils getProductUtils()
	{
		return productUtils;
	}

	@Required
	public void setProductUtils(final ProductUtils productUtils)
	{
		this.productUtils = productUtils;
	}
}
