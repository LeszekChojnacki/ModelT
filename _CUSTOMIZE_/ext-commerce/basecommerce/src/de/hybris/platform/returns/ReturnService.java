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
package de.hybris.platform.returns;

import de.hybris.platform.basecommerce.enums.RefundReason;
import de.hybris.platform.basecommerce.enums.ReplacementReason;
import de.hybris.platform.basecommerce.enums.ReturnAction;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.order.OrderEntry;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReplacementEntryModel;
import de.hybris.platform.returns.model.ReplacementOrderEntryModel;
import de.hybris.platform.returns.model.ReplacementOrderModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.returns.strategy.ReturnableCheck;

import java.util.List;
import java.util.Map;



/**
 * Service for handling 'Order Returns'. It offers ...
 * <ul>
 * <li>Configurable "Return Merchandise Authorization" aka "Return Material Authorization" (RMA) handling
 * <li>Replacement Order handling
 * <li>Refund handling incl. calculation
 * <li>Configurable 'is returnable' check
 * </ul>
 */
public interface ReturnService
{
	/**
	 * Creates an "return request" object (@link ReturnRequest} for the order to be returned.
	 *
	 * @param order
	 * 		the order which should be returned
	 * @return ReturnRequest instance, which contains among others the RMA code.
	 */
	ReturnRequestModel createReturnRequest(OrderModel order);

	/**
	 * Returns the "return request" for the specified order
	 *
	 * @param orderCode
	 * 		code of the order we ask for the "return request"
	 * @return "return request" of the order
	 */
	List<ReturnRequestModel> getReturnRequests(final String orderCode);

	/**
	 * returns a "Return Merchandise Authorization" aka "Return Material Authorization" (RMA).
	 *
	 * @param request
	 * 		reference of the related ReturnRequest
	 * @return Return Merchandise Authorization code
	 */
	String getRMA(final ReturnRequestModel request);

	/**
	 * creates "Return Merchandise Authorization" aka "Return Material Authorization" (RMA) and assigns it to the
	 * assigned {@link ReturnRequestModel}
	 *
	 * @param request
	 * 		reference of the related "ReturnRequest" for which this RMA will be created
	 * @return Return Merchandise Authorization code
	 */
	String createRMA(final ReturnRequestModel request);

	/**
	 * Returns the {@link ReplacementOrderModel} by the specified 'RMA value'
	 *
	 * @param rma
	 * 		rma value
	 * @return replacement order
	 */
	ReplacementOrderModel getReplacementOrder(String rma);

	/**
	 * Creates a {@link ReplacementOrderModel}
	 *
	 * @param request
	 * 		the return request to which the order will be assigned
	 * @return the Replacement Order'
	 */
	public ReplacementOrderModel createReplacementOrder(final ReturnRequestModel request);

	/**
	 * Adds {@link ReplacementOrderEntryModel} entries from the specified {@link ReplacementEntryModel}.
	 *
	 * @param order
	 * 		the replacement order
	 * @param replacements
	 * 		the 'replacement' entries (instances which are on HOLD will be ignored)
	 */
	void addReplacementOrderEntries(final ReplacementOrderModel order, final List<ReplacementEntryModel> replacements);

	/**
	 * Creates a Replacement based on the assigned OrderEntry instance
	 *
	 * @param request
	 * 		we use this for the verification process (order)
	 * @param entry
	 * 		the original OrderEntry
	 * @param notes
	 * 		some notes
	 * @param expectedQuantity
	 * 		the amount of products which the customer wants to return.
	 * @param action
	 * 		action which indicates if the 'returns process' will be executed immediately or is still on hold
	 * 		(waiting for the article to be send back)
	 * @param reason
	 * 		reason for the replacement
	 * @return Replacement Entry instance
	 */
	ReplacementEntryModel createReplacement(final ReturnRequestModel request, final AbstractOrderEntryModel entry,
			final String notes, final Long expectedQuantity, final ReturnAction action, final ReplacementReason reason);

	/**
	 * Creates a Refund based on the assigned OrderEntry instance
	 *
	 * @param request
	 * 		we use this for the verification process (order)
	 * @param entry
	 * 		the original OrderEntry
	 * @param notes
	 * 		some notes
	 * @param expectedQuantity
	 * 		the amount of products which the customer wants to refund.
	 * @param action
	 * 		action which indicates if the 'return process' will be executed immediately or is still on hold (waiting
	 * 		for the article to be send back)
	 * @param reason
	 * 		reason for the refund
	 * @return Refund entry instance
	 */
	RefundEntryModel createRefund(final ReturnRequestModel request, final AbstractOrderEntryModel entry, final String notes,
			final Long expectedQuantity, final ReturnAction action, final RefundReason reason);


	/**
	 * Returns the ReturnEntries for the specified product
	 *
	 * @param product
	 * 		the product
	 * @return the Return entry instance
	 */
	List<ReturnEntryModel> getReturnEntries(ProductModel product);

	/**
	 * Returns the ReturnEntries for the specified order entry
	 *
	 * @param entry
	 * 		the OrderEntry
	 * @return the return entry instance
	 */
	List<ReturnEntryModel> getReturnEntry(final AbstractOrderEntryModel entry);

	/**
	 * Returns all Replacements for the specified return request
	 *
	 * @param request
	 * 		the ReturnRequestModel
	 * @return the replacements
	 */
	List<ReplacementEntryModel> getReplacements(final ReturnRequestModel request);

	/**
	 * Determines if the product is 'returnable' by using the injected {@link ReturnableCheck} impl.
	 *
	 * @param order
	 * 		the related original order
	 * @param entry
	 * 		the ordered product which will be returned
	 * @param returnQuantity
	 * 		the quantity of entries to be returned
	 * @return true if product is 'returnable'
	 */
	boolean isReturnable(final OrderModel order, AbstractOrderEntryModel entry, long returnQuantity);

	/**
	 * Here you have the chance to 'inject' your final Return Entry processing. For example for handling consignment
	 * creation
	 *
	 * @param entries
	 * 		the entries to be processed
	 */
	void processReturnEntries(List<ReturnEntryModel> entries);

	/**
	 * Here you have the chance to 'inject' your final Replacement order processing. For example for handling consignment
	 * creation
	 *
	 * @param order
	 * 		the order to be processed
	 */
	void processReplacementOrder(ReplacementOrderModel order);

	/**
	 * Here you have the chance to 'inject' your final Refund order processing. For example for handling consignment
	 * creation or initiating the final payment transaction.
	 *
	 * @param order
	 * 		the order to be processed
	 */
	void processRefundOrder(OrderModel order);

	/**
	 * Returns all returnable {@link OrderEntry}'s
	 *
	 * @param order
	 * 		the related order
	 * @return all returnable {@link OrderEntry} and their returnable quantity
	 */
	Map<AbstractOrderEntryModel, Long> getAllReturnableEntries(final OrderModel order);

	/**
	 * Gets order return record for a given order.
	 *
	 * @param order
	 * 		instance of {@link OrderModel} to get the return record for
	 * @return order return record for the given order
	 * @throws OrderReturnException
	 * 		in the case of error during service call
	 */
	OrderReturnRecordModel getOrderReturnRecordForOrder(OrderModel order) throws OrderReturnException;

	/**
	 * Request manual payment reversal for the (@link ReturnRequest}.
	 *
	 * @param returnRequest
	 * 		the return request for which payment should be manually reversed
	 * @throws OrderReturnException
	 * 		in the case of error during service call
	 */
	void requestManualPaymentReversalForReturnRequest(ReturnRequestModel returnRequest) throws OrderReturnException;

	/**
	 * Request manual tax reversal for the (@link ReturnRequest}.
	 *
	 * @param returnRequest
	 * 		the return request for which tax should be manually reversed
	 * @throws OrderReturnException
	 * 		in the case of error during service call
	 */
	void requestManualTaxReversalForReturnRequest(ReturnRequestModel returnRequest) throws OrderReturnException;
}
