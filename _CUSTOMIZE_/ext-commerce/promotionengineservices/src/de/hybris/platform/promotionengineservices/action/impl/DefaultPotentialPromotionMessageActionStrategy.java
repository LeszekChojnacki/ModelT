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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.model.PromotionActionParameterModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPotentialPromotionMessageActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulates logic of displaying potential promotion message.
 *
 */
public class DefaultPotentialPromotionMessageActionStrategy extends
		AbstractRuleActionStrategy<RuleBasedPotentialPromotionMessageActionModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPotentialPromotionMessageActionStrategy.class);

	/**
	 * If the parameter action is of type {@link DisplayMessageRAO} PromotionResultModel for potential promotion message
	 * is created.
	 *
	 * @return list of {@link PromotionResultModel} as a result of the {@link DisplayMessageRAO} application.
	 */
	@Override
	public List<PromotionResultModel> apply(final AbstractRuleActionRAO action)
	{
		if (!(action instanceof DisplayMessageRAO))
		{
			LOG.error("cannot apply {}, action is not of type DisplayMessageRAO", this.getClass().getSimpleName());
			return Collections.emptyList();
		}

		final PromotionResultModel promoResult = getPromotionActionService().createPromotionResult(action);
		if (promoResult == null)
		{
			LOG.error("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
			return Collections.emptyList();
		}

		final AbstractOrderModel order = getPromotionResultUtils().getOrder(promoResult);
		if (order == null)
		{
			LOG.error("cannot apply {}, order not found", this.getClass().getSimpleName());
			// detach the promotion result if its not saved yet.
			if (getModelService().isNew(promoResult))
			{
				getModelService().detach(promoResult);
			}
			return Collections.emptyList();
		}

		final RuleBasedPotentialPromotionMessageActionModel actionModel = createPromotionAction(promoResult, action);
		handleActionMetadata(action, actionModel);

		supplementMessageActionModelWithParameters((DisplayMessageRAO) action, actionModel);

		getModelService().saveAll(promoResult, actionModel);

		getPromotionActionService().recalculateFiredPromotionMessage(promoResult);

		return Collections.singletonList(promoResult);

	}

	protected void supplementMessageActionModelWithParameters(final DisplayMessageRAO action,
			final RuleBasedPotentialPromotionMessageActionModel actionModel)
	{
		if (MapUtils.isNotEmpty(action.getParameters()))
		{
			actionModel.setParameters(action.getParameters().entrySet().stream().map(this::convertToActionParameterModel)
					.collect(Collectors.toList()));

		}
	}

	protected PromotionActionParameterModel convertToActionParameterModel(final Entry<String, Object> actionParameterEntry)
	{
		final PromotionActionParameterModel actionParameterModel = getModelService().create(PromotionActionParameterModel.class);
		actionParameterModel.setUuid(actionParameterEntry.getKey());
		actionParameterModel.setValue(actionParameterEntry.getValue());
		return actionParameterModel;
	}

	@Override
	public void undo(final ItemModel item)
	{
		if (item instanceof RuleBasedPotentialPromotionMessageActionModel)
		{
			final RuleBasedPotentialPromotionMessageActionModel action = (RuleBasedPotentialPromotionMessageActionModel) item;
			handleUndoActionMetadata(action);
			removeMessageActionModelParameters(action);
			undoInternal(action);
		}
	}

	protected void removeMessageActionModelParameters(final RuleBasedPotentialPromotionMessageActionModel action)
	{
		if (CollectionUtils.isNotEmpty(action.getParameters()))
		{
			action.getParameters().stream().forEach(param -> getModelService().remove(param));
		}
	}
}
