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
package de.hybris.platform.promotions;

import de.hybris.platform.promotions.jalo.AbstractPromotion;
import de.hybris.platform.promotions.jalo.AbstractPromotionAction;
import de.hybris.platform.promotions.jalo.PromotionOrderEntryConsumed;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;


/**
 * PromotionResultService.
 * <p/>
 * The result of evaluating a promotion against a cart or order is a set of promotion results. Each promotion my produce
 * zero or more results to express the results of its evaluation. A {@link PromotionResult} is associated with a single
 * {@link AbstractPromotion} and is either fired ({@link #getFired(PromotionResultModel)}) or potentially could fire (
 * {@link #getCouldFire(PromotionResultModel)}).
 * <p/>
 * A result that has fired as met all the requirements of the promotion. A result that potentially could fire has not
 * met all the requirements of the promotion. When the result could fire the promotion also assigns a certainty value to
 * the result to indicate how close the result is to firing. This is a value in the range 0 to 1, where 1 indicates that
 * the promotion has fired. This value can be used to rank potential promotion results.
 * <p/>
 * A promotion result holds a number of {@link PromotionOrderEntryConsumed} instances to represent the entries in the
 * order that have been consumed by the promotion in generating this result. If the promotion has fired then these
 * consumed entries are not available to other promotions. If the promotion has not fired then these consumed entries
 * are just an indication of the entries that will be consumed when the promotion can fire.
 * <p/>
 * If the promotion has fired the promotion result also holds a number of {@link AbstractPromotionAction} instances to
 * represent the actions that the promotion takes. These actions are either applied or not (
 * {@link #isApplied(PromotionResultModel)}). When the promotions are evaluated by
 * {@link PromotionsManager#updatePromotions(de.hybris.platform.jalo.SessionContext, java.util.Collection,
 * de.hybris.platform.jalo.order.AbstractOrder, boolean, de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode,
 * de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode, java.util.Date)}
 * a firing promotion result may be automatically applied depending on the parameters passed to the method. The actions
 * of a specific promotion result can be applied by calling the {@link #apply(PromotionResultModel)} method.
 * <p/>
 * The actions of a specific promotion result can be reversed by calling the {@link #undo(PromotionResultModel)} method.
 */
public interface PromotionResultService
{

	/**
	 * Returns <i>true</i> if the promotion fired and all of its actions have been applied to the order.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return Whether the promotion has been applied
	 */
	boolean isApplied(PromotionResultModel promotionResult);

	/**
	 * Returns <i>true</i> if the promotion fired and all of its actions have been applied to the order.
	 * This method checks that all the actions are still applied to the order.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return Whether the promotion has been applied
	 */
	boolean isAppliedToOrder(PromotionResultModel promotionResult);

	/**
	 * Returns <i>true</i> if the promotion fired and has produced a result.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return Whether the promotion fired
	 */
	boolean getFired(PromotionResultModel promotionResult);

	/**
	 * Is this a potential result.
	 * Returns <i>true</i> if the promotion believes it has a chance of firing, for instance if it requires 3 qualifying
	 * products but can only find 1.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return Whether the promotion could fire
	 */
	boolean getCouldFire(PromotionResultModel promotionResult);

	/**
	 * Get the description of this promotion result.
	 * This method uses the default locale (Locale.getDefault())
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return A description of the promotion result
	 * @see #getDescription(PromotionResultModel, java.util.Locale)
	 */
	String getDescription(PromotionResultModel promotionResult);

	/**
	 * Get the description of this promotion result.
	 * Gets the description for this promotion result. This description is based on the state of the result, the
	 * promotion that generated the result and the user supplied formatting strings.
	 * <p/>
	 * The {@link Locale} specified is used to format any numbers, dates or currencies for display to the user. It is
	 * important that this locale best represents the formatting options appropriate for display to the user. The default
	 * currency for the locale is ignored. The currency is always explicitly taken from the
	 * {@link de.hybris.platform.jalo.order.AbstractOrder#getCurrency()}. The currency is then formatted appropriately in
	 * the locale specified. For example, this does mean that values in the EURO currency will be formatted differently
	 * depending on the locale specified as each locale can specify currency specific formatting.
	 * <p/>
	 * The currency formatting is part of the Java VM configuration to support multiple locales. If the formatting is
	 * incorrect check your VM configuration for the locale and currency combination.
	 *
	 * @param locale
	 * 		The locale to use to format the messages. This locale must support currency formatting, i.e. this should
	 * 		be a region specific local. e.g de_DE, en_US, en_GB
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return A description of the promotion result
	 */
	String getDescription(PromotionResultModel promotionResult, Locale locale);

	/**
	 * Apply all of the actions that this promotion generated to the order.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	boolean apply(PromotionResultModel promotionResult);


	/**
	 * Undo all of the changes that this promotion made to the order.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return <i>true</i> if calculateTotals() should be called to update the order totals.
	 */
	boolean undo(PromotionResultModel promotionResult);

	/**
	 * Get the total number of items consumed by this promotion.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @param includeCouldFirePromotions
	 * 		include could fire promotions
	 * @return The total number of items consumed
	 */
	long getConsumedCount(PromotionResultModel promotionResult, boolean includeCouldFirePromotions);

	/**
	 * Get the total value of all discounts in this result. This result will be the same regardless of the applied state
	 * of this result, i.e. if not applied this is the discount value that would be applied, if it is applied then it is
	 * the value of the discount.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return The double value for the total discount value
	 */
	double getTotalDiscount(PromotionResultModel promotionResult);

	/**
	 * Return a list of results for potential promotions that fired and consumed products and have been applied.
	 *
	 * @param promoResult
	 * 		- instance of {@link PromotionOrderResults}
	 * @param promotion
	 * 		- instance of {@link AbstractPromotionModel}
	 * @return A list of the results of product promotions that fired and applied
	 */
	List<PromotionResultModel> getPotentialProductPromotions(PromotionOrderResults promoResult, AbstractPromotionModel promotion);

	/**
	 * Return a list of results for potential promotions that fired and did not consume products and have been applied.
	 *
	 * @param promoResult
	 * 		- instance of {@link PromotionOrderResults}
	 * @param promotion
	 * 		- instance of {@link AbstractPromotionModel}
	 * @return A list of the results of order promotions that fired and applied
	 */
	List<PromotionResultModel> getPotentialOrderPromotions(PromotionOrderResults promoResult, AbstractPromotionModel promotion);

	/**
	 * Return a list of results for promotions that fired and consumed products.
	 *
	 * @param promoResult
	 * 		- instance of {@link PromotionOrderResults}
	 * @param promotion
	 * 		- instance of {@link AbstractPromotionModel}
	 * @return A list of the results of product promotions that fired
	 */
	List<PromotionResultModel> getFiredProductPromotions(PromotionOrderResults promoResult, AbstractPromotionModel promotion);

	/**
	 * Return a list of results for promotions that fired and did not consume products.
	 *
	 * @param promoResult
	 * 		- instance of {@link PromotionOrderResults}
	 * @param promotion
	 * 		- instance of {@link AbstractPromotionModel}
	 * @return A list of the results of promotions that fired
	 */
	List<PromotionResultModel> getFiredOrderPromotions(PromotionOrderResults promoResult, AbstractPromotionModel promotion);

	/**
	 * Return give way coupon code for promotion.
	 *
	 * @param promotionResult
	 * 		- instance of {@link PromotionResultModel}
	 * @return Optional Set of String containing give way coupon code
	 */
	Optional<Set<String>> getCouponCodesFromPromotion(PromotionResultModel promotionResult);
}
