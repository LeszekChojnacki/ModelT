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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.promotions.jalo.OrderPromotion;
import de.hybris.platform.promotions.jalo.ProductPromotion;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.jalo.PromotionsManager.AutoApplyMode;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.AbstractPromotionRestrictionModel;
import de.hybris.platform.promotions.model.OrderPromotionModel;
import de.hybris.platform.promotions.model.ProductPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * The service for the Promotions extension.
 *
 * Manages applying promotions to carts and orders.
 * <ul>
 * <li>Use the {@link #updatePromotions} methods to evaluate the promotions that can be applied to a cart or order.</li>
 * <li>Use the {@link #getPromotionResults} methods to retrieve the promotions calculated for an order.</li>
 * <li>Use the {@link #getProductPromotions} methods to retrieve the promotions that a {@link ProductModel} can be part
 * of.</li>
 * <li>The promotions extension stores additional database items for the cart and order items. When a cart is removed
 * from the system it is necessary to call the {@link #cleanupCart} method to removed these items.</li>
 * </ul>
 */
public interface PromotionsService
{

	/**
	 * Get the ordered list of {@link ProductPromotion} instances that are related to the {@link ProductModel} specified.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product that the promotions are related to
	 * @return The list of {@link ProductPromotion} related to the {@link ProductModel} specified
	 */
	List<ProductPromotionModel> getProductPromotions(Collection<PromotionGroupModel> promotionGroups, ProductModel product);

	/**
	 * Get the ordered list of {@link ProductPromotion} instances that are related to the {@link ProductModel} specified.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product that the promotions are related to
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link ProductPromotion} related to the {@link ProductModel} specified
	 */
	List<ProductPromotionModel> getProductPromotions(Collection<PromotionGroupModel> promotionGroups, ProductModel product,
			boolean evaluateRestrictions, Date date);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, Date date);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product to pass to restrictions
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, ProductModel product);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product to pass to restrictions
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, ProductModel product, Date date);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, boolean evaluateRestrictions);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, boolean evaluateRestrictions,
			Date date);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @param product
	 *           The product to pass to restrictions if evaluateRestrictions is true
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, boolean evaluateRestrictions,
			ProductModel product);

	/**
	 * Get the ordered list of {@link OrderPromotion} instances.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @param product
	 *           The product to pass to restrictions if evaluateRestrictions is true
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link OrderPromotion}
	 */
	List<OrderPromotionModel> getOrderPromotions(Collection<PromotionGroupModel> promotionGroups, boolean evaluateRestrictions,
			ProductModel product, Date date);

	/**
	 * Get the ordered list of {@link AbstractPromotionModel} instances that are related to the {@link ProductModel}
	 * specified.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product that the promotions are related to
	 * @return The list of {@link AbstractPromotionModel} related to the {@link ProductModel} specified
	 */
	List<? extends AbstractPromotionModel> getAbstractProductPromotions(Collection<PromotionGroupModel> promotionGroups, //NOSONAR
			ProductModel product);

	/**
	 * Get the ordered list of {@link AbstractPromotionModel} instances that are related to the {@link ProductModel}
	 * specified.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param product
	 *           The product that the promotions are related to
	 * @param evaluateRestrictions
	 *           Flag, pass false to ignore any restrictions specified on the promotions, pass true to observe the
	 *           restrictions.
	 * @param date
	 *           The date to check for promotions, typically the current date
	 * @return The list of {@link AbstractPromotionModel} related to the {@link ProductModel} specified
	 */
	List<? extends AbstractPromotionModel> getAbstractProductPromotions(Collection<PromotionGroupModel> promotionGroups, // NOSONAR
			ProductModel product, boolean evaluateRestrictions, Date date);

	/**
	 * Update the promotions on the specified {@link AbstractOrder} object.
	 * <p/>
	 * This method will automatically apply all possible product promotional updates to the cart, but not apply order
	 * level promotions. Any previously applied order level promotions will remain applied. Promotions are evaluated at
	 * the current system time.
	 * <p/>
	 * The promotion results are stored in the database and the same {@link PromotionOrderResults} can be obtained later
	 * by calling {@link #getPromotionResults}.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param order
	 *           The order object to update with the results of the promotions
	 * @return The promotion results
	 */
	PromotionOrderResults updatePromotions(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order);

	/**
	 * Update the promotions on the specified {@link AbstractOrderModel} object.
	 * <p/>
	 * The resulting promotions can be retrieved later by calling {@link #getPromotionResults}. The order must be
	 * calculated before calling this method. {@link #updatePromotions} must be called after calling
	 * {@link AbstractOrder#recalculate()} on the {@link AbstractOrderModel}. Where the {@link AutoApplyMode} is set to
	 * {@link AutoApplyMode#KEEP_APPLIED} the state of any previously applied {@link PromotionResult} is recorded and if
	 * it is still in the fired state ({@link PromotionResult#isApplied()}) after reevaluating the promotions it will be
	 * automatically reapplied.
	 * <p/>
	 * The promotion results are stored in the database and the same {@link PromotionOrderResults} can be obtained later
	 * by calling {@link #getPromotionResults}. After this method is called, please call getModelService().refresh(order)
	 * or getModelService().save(order).
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param order
	 *           The AbstractOrder object to update the promotions for
	 * @param evaluateRestrictions
	 *           If <i>true</i> any promotion restrictions will be observed, if <i>false</i> all promotion restrictions
	 *           are ignored
	 * @param productPromotionMode
	 *           The auto apply mode. This determines whether this method applies any product promotional changes to line
	 *           items or discounts to the overall amount
	 * @param orderPromotionMode
	 *           The auto apply mode. This determines whether this method applies any order promotional changes to line
	 *           items or discounts to the overall amount
	 * @param date
	 *           The effective date for the promotions to check. Use this to to see the effects of promotions in the past
	 *           or future.
	 * @return The promotion results
	 */
	PromotionOrderResults updatePromotions(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order,
			boolean evaluateRestrictions, AutoApplyMode productPromotionMode, AutoApplyMode orderPromotionMode, Date date);

	/**
	 * Get the promotion results for the specified order.
	 * <p/>
	 * These are the promotion results stored in the database for the specified order as generated by the last call to
	 * {@link #updatePromotions} for the same order.
	 * <p/>
	 * If any of the promotion results are invalid then they will be ignored.
	 *
	 * @param order
	 *           The order to get the promotion results for
	 * @return The promotion results
	 */
	PromotionOrderResults getPromotionResults(AbstractOrderModel order);

	/**
	 * Get the promotion results for the specified order.
	 * <p/>
	 * These are the promotion results stored in the database for the specified order as generated by the last call to
	 * {@link #updatePromotions} for the same order.
	 * <p/>
	 * If any of the promotion results are invalid then this method will recalculate the promotions by calling
	 * {@link #updatePromotions}.
	 *
	 * @param promotionGroups
	 *           The promotion groups to evaluate
	 * @param order
	 *           The AbstractOrder object to get the promotions for
	 * @param evaluateRestrictions
	 *           If <i>true</i> any promotion restrictions will be observed, if <i>false</i> all promotion restrictions
	 *           are ignored
	 * @param productPromotionMode
	 *           The auto apply mode. This determines whether this method applies any product promotional changes to line
	 *           items or discounts to the overall amount
	 * @param orderPromotionMode
	 *           The auto apply mode. This determines whether this method applies any order promotional changes to line
	 *           items or discounts to the overall amount
	 * @param date
	 *           The effective date for the promotions to check. Use this to to see the effects of promotions in the past
	 *           or future.
	 * @return The promotion results
	 */
	PromotionOrderResults getPromotionResults(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order,
			boolean evaluateRestrictions, AutoApplyMode productPromotionMode, AutoApplyMode orderPromotionMode, Date date);

	/**
	 * Delete the the stored promotion results for a Cart.
	 *
	 * @param cart
	 *           The {@link Cart} to delete the results for
	 */
	void cleanupCart(CartModel cart);

	/**
	 * Transfer the promotions applied to a cart to a new order. This is used when an order is created from a cart.
	 *
	 * @param source
	 *           The cart that has promotions
	 * @param target
	 *           The order that promotions should be applied to
	 * @param onlyTransferAppliedPromotions
	 *           Flag to indicate that only applied promotions should be transfered. If false all promotion results will
	 *           be transfered
	 */
	void transferPromotionsToOrder(AbstractOrderModel source, final OrderModel target, boolean onlyTransferAppliedPromotions);

	/**
	 * Lookup the default promotion group created by the promotions extension.
	 *
	 * @return the default promotion group
	 */
	PromotionGroupModel getDefaultPromotionGroup();

	/**
	 * Lookup a promotion group with the given identifier.
	 *
	 * @param identifier
	 *           identifier of the promotion group to look for
	 * @return the promotion group or null if no group with the given identifier is found
	 * @throws IllegalArgumentException
	 *            if identifier is null
	 */
	PromotionGroupModel getPromotionGroup(final String identifier);


	/**
	 * Get the collection of {@link AbstractPromotionRestrictionModel} instances.
	 *
	 * @param promotion
	 *           the promotion which restrictions should be returned
	 * @return A collection of {@link AbstractPromotionRestrictionModel} instances attached to this promotion.
	 */
	Collection<AbstractPromotionRestrictionModel> getRestrictions(AbstractPromotionModel promotion);

	/**
	 * Get the description of given promotion.
	 *
	 * @param promotion
	 *           the promotion to get the description for
	 * @return String representing promotion description
	 */
	String getPromotionDescription(AbstractPromotionModel promotion);
}
