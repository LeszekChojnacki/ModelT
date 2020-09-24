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
package de.hybris.platform.promotionengineservices.promotionengine;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;

import java.util.List;


/**
 * Encapsulates logic for taking actions to apply promotions.
 *
 */
public interface PromotionActionService
{
	/**
	 * Recalculates promotion fired message and stores it in provided {@link PromotionResultModel}. This method should be
	 * used for potential promotion only to update computed values displayed in the message
	 * 
	 * @param promoResult
	 *           a promotion result that holds fired promotion message which needs to be updated
	 */
	void recalculateFiredPromotionMessage(PromotionResultModel promoResult);

	/**
	 * Creates a {@code PromotionResultModel} object for the given {@code AbstractRuleActionRAO}.
	 *
	 * @param action
	 *           the action rao to use for the promotion result creation
	 * @return the newly created (not persisted!) {@link PromotionResultModel}.
	 */
	PromotionResultModel createPromotionResult(AbstractRuleActionRAO action);

	/**
	 * Creates a {@code DiscountValue} from the given {@code discountRao} and adds it to the given {@code order}. Note:
	 * The order is not saved!
	 *
	 * @param discountRao
	 *           the discountRao to take the values from
	 * @param code
	 *           the code for the new DiscountValue (e.g. UUID from the respective {@link AbstractPromotionActionModel}
	 *           that creates this discount)
	 * @param order
	 *           the order to apply the discount value to
	 */
	void createDiscountValue(DiscountRAO discountRao, String code, AbstractOrderModel order);

	/**
	 * Removes the {@code DiscountValue} with the given code from the given {@code order} and its order entries. Returns
	 * the list of all modified items (order, order entries). Note: The returned items are not saved!
	 *
	 * @param code
	 *           the code for the DiscountValue to be removed
	 * @param order
	 *           the order to remove the discount value from
	 * @return all items (order, order entries) that have been modified and need saving
	 */
	List<ItemModel> removeDiscountValue(String code, AbstractOrderModel order);

	/**
	 * Creates a {@code DiscountValue} from the given {@code discountRao} and adds it to the given {@code orderEntry}.
	 * Note: The orderEntry is not saved!
	 *
	 * @param discountRao
	 *           the discountRao to take the values from
	 * @param code
	 *           the code for the new DiscountValue (e.g. UUID from the respective {@link AbstractPromotionActionModel}
	 *           that creates this discount)
	 * @param orderEntry
	 *           the orderEntry to apply the discount value to
	 */
	void createDiscountValue(DiscountRAO discountRao, String code, AbstractOrderEntryModel orderEntry);

	/**
	 * Looks up the {@code AbstractOrderEntryModel} based on the given AbstractRuleActionRAO.
	 *
	 * @param action
	 *           the action to use for the look-up
	 * @return the AbstractOrderEntryModel
	 */
	AbstractOrderEntryModel getOrderEntry(AbstractRuleActionRAO action);

	/**
	 * Returns the order for the given {@code action}.
	 *
	 * @param action
	 *           the orderRao
	 * @return the order or null if no order is found
	 */
	AbstractOrderModel getOrder(AbstractRuleActionRAO action);

	/**
	 * Returns the rule for the given {@code action}.
	 *
	 * @param action
	 *           the action rao
	 * @return the rule or null if no rule is found
	 */
	AbstractRuleEngineRuleModel getRule(AbstractRuleActionRAO action);

	/**
	 * Recalculates totals for the given {@code order}.
	 *
	 * @param order
	 *           the order to recalculate its totals
	 */
	void recalculateTotals(AbstractOrderModel order);
}
