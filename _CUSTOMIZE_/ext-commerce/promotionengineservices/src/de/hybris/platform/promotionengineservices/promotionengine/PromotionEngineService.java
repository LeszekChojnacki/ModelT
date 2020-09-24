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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.ruleengine.RuleEvaluationResult;

import java.util.Collection;
import java.util.Date;


/**
 * Interface of Promotion Engine Service that evaluates Promotion for a Product depending on it implementation.
 */
public interface PromotionEngineService
{
	/**
	 * Evaluates promotion against a product taking into context which is set by promotions group
	 * @param product
	 *           to evaluate promotions for
	 * @param promotionGroups
	 *           collection of promotion groups to apply promotions for
	 * @return Map containing Promotion data as its values along with corresponding identifiers as its keys
	 */
	RuleEvaluationResult evaluate(ProductModel product, Collection<PromotionGroupModel> promotionGroups);

	/**
	 * evaluate the promotions for the given cart or order and for now. This does not apply the result to the cart/order.
	 * 
	 * @param order
	 *           the cart or order to evaluate
	 * @param promotionGroups
	 *           collection of promotion groups to apply promotions for
	 * @return the result of the evaluation
	 */
	RuleEvaluationResult evaluate(AbstractOrderModel order, Collection<PromotionGroupModel> promotionGroups);

	/**
	 * evaluate the promotions for the given cart or order and for the date. This does not apply the result to the
	 * cart/order.
	 * 
	 * @param order
	 *           the cart or order to evaluate
	 * @param promotionGroups
	 *           collection of promotion groups to apply promotions for
	 * @param date
	 *           the moment of evaluation
	 * @return the result of the evaluation
	 */
	RuleEvaluationResult evaluate(AbstractOrderModel order, Collection<PromotionGroupModel> promotionGroups, Date date);
}
