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
package de.hybris.platform.promotionengineservices.action.impl;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MAXIMUM_RULE_EXECUTIONS;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MODULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_CODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_EXCLUSIVE;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionActionService;
import de.hybris.platform.promotionengineservices.util.ActionUtils;
import de.hybris.platform.promotionengineservices.util.PromotionResultUtils;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengine.RuleActionMetadataHandler;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.order.dao.ExtendedOrderDao;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract base class for Rule Action Strategy.
 */
public abstract class AbstractRuleActionStrategy<A extends AbstractRuleBasedPromotionActionModel>
		implements RuleActionStrategy, BeanNameAware
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRuleActionStrategy.class);

	private ExtendedOrderDao extendedOrderDao;
	private ModelService modelService;
	private PromotionActionService promotionActionService;
	private CalculationService calculationService;
	private Class<A> promotionAction;

	private Boolean forceImmediateRecalculation = Boolean.FALSE;

	private String beanName;
	private Map<String, List<RuleActionMetadataHandler>> ruleActionMetadataHandlers;
	private PromotionResultUtils promotionResultUtils;
	private ActionUtils actionUtils;

	/**
	 * Creates an instance of the configured {@code promotionAction} (which is a subtype of {@code RuleBasedPromotionActionModel},
	 * sets a new unique UUID, sets this strategy's id, marks the action as
	 * applied, attaches the given {@code promotionResult} and tries to lookup the rule which created the given
	 * {@code AbstractRuleActionRAO}.
	 * Note: the action is created through the {@code modelService}, but it is <b>not</b> saved.
	 * @param promotionResult
	 *           the promotionResult that the to-be-created promotion action belongs to
	 * @param action
	 *           the action rao
	 * @return the newly created instance
	 */
	protected A createPromotionAction(final PromotionResultModel promotionResult, final AbstractRuleActionRAO action)
	{
		final A result = getModelService().create(promotionAction);
		result.setPromotionResult(promotionResult);
		result.setGuid(getActionUtils().createActionUUID());
		result.setRule(getPromotionActionService().getRule(action));
		result.setMarkedApplied(Boolean.TRUE);
		result.setStrategyId(getStrategyId());
		return result;
	}

	protected void handleActionMetadata(final AbstractRuleActionRAO action,
			final AbstractRuleBasedPromotionActionModel actionModel)
	{
		if (action.getMetadata() != null)
		{
			for (final Map.Entry<String, String> mdEntry : action.getMetadata().entrySet())
			{
				for (final RuleActionMetadataHandler mdHandler : getMetadataHandlers(mdEntry.getKey()))
				{
					mdHandler.handle(actionModel, mdEntry.getValue());
				}
			}
		}
	}

	protected void handleUndoActionMetadata(final AbstractRuleBasedPromotionActionModel action)
	{
		if (action.getMetadataHandlers() != null)
		{
			for (final String mdHandlerId : action.getMetadataHandlers())
			{
				for (final RuleActionMetadataHandler mdHandler : getMetadataHandlers(mdHandlerId))
				{
					mdHandler.undoHandle(action);
				}
			}
		}
	}

	protected List<RuleActionMetadataHandler> getMetadataHandlers(final String mdKey)
	{
		if (getRuleActionMetadataHandlers().containsKey(mdKey))
		{
			return getRuleActionMetadataHandlers().get(mdKey);
		}
		else
		{
			// log error if its not the default ruleCode meta data
			if (!RULEMETADATA_RULECODE.equals(mdKey) && !RULEMETADATA_MODULENAME.equals(mdKey)
					&& !RULEMETADATA_MAXIMUM_RULE_EXECUTIONS.equals(mdKey) && !RULEMETADATA_RULEGROUP_CODE.equals(mdKey)
					&& !RULEMETADATA_RULEGROUP_EXCLUSIVE.equals(mdKey))
			{
				LOG.error("RuleActionMetadataHandler for {} not found", mdKey);
			}
			return Collections.emptyList();
		}
	}

	/**
	 * removes the given {@code action} (if it is not already removed), its associated DiscountValues and its
	 * PromotionResult (if the PromotionResult has no other actions associated with it).
	 *
	 * @param action
	 *           the action to undo
	 * @return the action's associated order (or null if the action is already removed)
	 */
	protected AbstractOrderModel undoInternal(final A action)
	{
		final PromotionResultModel promoResult = action.getPromotionResult();
		final AbstractOrderModel order = getPromotionResultUtils().getOrder(promoResult);
		final List<ItemModel> modifiedItems = getPromotionActionService().removeDiscountValue(action.getGuid(), order);
		getModelService().remove(action);
		if (promoResult.getAllPromotionActions().stream().filter(promoAction -> !getModelService().isRemoved(promoAction))
				.collect(Collectors.toSet()).isEmpty())
		{
			getModelService().remove(promoResult);
		}
		getModelService().saveAll(modifiedItems);
		return order;
	}

	/**
	 * recalculates the given {@code order} if the {@code #forceImmediateRecalculation} flag is set to {@code true}.
	 *
	 * @param order
	 *           the order to recalculate
	 * @return false if the recalculation failed, otherwise true
	 */
	protected boolean recalculateIfNeeded(final AbstractOrderModel order)
	{
		if (BooleanUtils.isTrue(getForceImmediateRecalculation()))
		{
			try
			{
				getCalculationService().calculateTotals(order, true);
			}
			catch (final CalculationException e)
			{
				LOG.error(String.format("Recalculation of order with code '%s' failed.", order.getCode()), e);
				order.setCalculated(Boolean.FALSE);
				getModelService().save(order);
				return false;
			}
		}
		return true;
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

	protected ModelService getModelService()
	{
		return this.modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected Class<A> getPromotionAction()
	{
		return promotionAction;
	}

	@Required
	public void setPromotionAction(final Class<A> promotionAction)
	{
		this.promotionAction = promotionAction;
		if (promotionAction != null)
		{
			try
			{
				promotionAction.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new SystemException("could not instantiate class " + promotionAction.getSimpleName(), e);
			}
		}
	}

	protected PromotionActionService getPromotionActionService()
	{
		return promotionActionService;
	}

	@Required
	public void setPromotionActionService(final PromotionActionService promotionActionService)
	{
		this.promotionActionService = promotionActionService;
	}

	protected Boolean getForceImmediateRecalculation()
	{
		return forceImmediateRecalculation;
	}

	public void setForceImmediateRecalculation(final Boolean forceImmediateRecalculation)
	{
		this.forceImmediateRecalculation = forceImmediateRecalculation;
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

	@Override
	public void setBeanName(final String beanName)
	{
		this.beanName = beanName;
	}

	@Override
	public String getStrategyId()
	{
		return beanName;
	}

	protected Map<String, List<RuleActionMetadataHandler>> getRuleActionMetadataHandlers()
	{
		return ruleActionMetadataHandlers;
	}

	@Required
	public void setRuleActionMetadataHandlers(final Map<String, List<RuleActionMetadataHandler>> ruleActionMetadataHandlers)
	{
		this.ruleActionMetadataHandlers = ruleActionMetadataHandlers;
	}

	protected PromotionResultUtils getPromotionResultUtils()
	{
		return promotionResultUtils;
	}

	@Required
	public void setPromotionResultUtils(final PromotionResultUtils promotionResultUtils)
	{
		this.promotionResultUtils = promotionResultUtils;
	}

	protected ActionUtils getActionUtils()
	{
		return actionUtils;
	}

	@Required
	public void setActionUtils(final ActionUtils actionUtils)
	{
		this.actionUtils = actionUtils;
	}
}
