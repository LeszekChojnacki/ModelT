/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.returns.service.impl;

import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.warehousing.returns.service.RefundAmountCalculationService;

import java.math.BigDecimal;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Calculates the refund amount based on the basePrice of products being returned.
 */
public class DefaultRefundAmountCalculationService implements RefundAmountCalculationService
{

	@Override
	public BigDecimal getCustomRefundAmount(final ReturnRequestModel returnRequest)
	{
		validateParameterNotNull(returnRequest, "Parameter returnRequest cannot be null");
		validateParameterNotNull(returnRequest.getReturnEntries(), "Parameter Return Entries cannot be null");

		BigDecimal refundAmount = returnRequest.getReturnEntries().stream()
				.map(returnEntry -> getCustomRefundEntryAmount(returnEntry)).reduce(BigDecimal.ZERO, BigDecimal::add);

		if(returnRequest.getRefundDeliveryCost().booleanValue()){
			refundAmount = refundAmount.add(BigDecimal.valueOf(returnRequest.getOrder().getDeliveryCost().doubleValue()));
		}

		return refundAmount.setScale(getNumberOfDigits(returnRequest), BigDecimal.ROUND_CEILING);
	}

	@Override
	public BigDecimal getCustomRefundEntryAmount(final ReturnEntryModel returnEntryModel)
	{
		validateParameterNotNull(returnEntryModel, "Parameter Return Entry cannot be null");
		BigDecimal itemValue = BigDecimal.ZERO;

		if (returnEntryModel instanceof RefundEntryModel)
		{
			itemValue = getOriginalRefundEntryAmount(returnEntryModel);
		}

		return itemValue;
	}

	@Override
	public BigDecimal getOriginalRefundAmount(ReturnRequestModel returnRequest)
	{
		validateParameterNotNull(returnRequest, "Parameter returnRequest cannot be null");
		validateParameterNotNull(returnRequest.getReturnEntries(), "Parameter Return Entries cannot be null");

		BigDecimal refundAmount = returnRequest.getReturnEntries().stream()
				.map(returnEntry -> getOriginalRefundEntryAmount(returnEntry)).reduce(BigDecimal.ZERO, BigDecimal::add);

		if(returnRequest.getRefundDeliveryCost().booleanValue()){
			refundAmount = refundAmount.add(BigDecimal.valueOf(returnRequest.getOrder().getDeliveryCost().doubleValue()));
		}

		return refundAmount.setScale(getNumberOfDigits(returnRequest), BigDecimal.ROUND_CEILING);
	}

	@Override
	public BigDecimal getOriginalRefundEntryAmount(final ReturnEntryModel returnEntryModel)
	{
		validateParameterNotNull(returnEntryModel, "Parameter Return Entry cannot be null");
		ReturnRequestModel returnRequest = returnEntryModel.getReturnRequest();
		BigDecimal refundEntryAmount = BigDecimal.ZERO;

		if (returnEntryModel instanceof RefundEntryModel)
		{
			final RefundEntryModel refundEntry = (RefundEntryModel) returnEntryModel;

			refundEntryAmount = refundEntry.getAmount();
			refundEntryAmount = refundEntryAmount.setScale(getNumberOfDigits(returnRequest), BigDecimal.ROUND_HALF_DOWN);
		}
		return refundEntryAmount;
	}

	/**
	 * Retrieves the number of digits to use base on a {@link ReturnRequestModel}
	 * @param returnRequest the return request from which we want to find the number of digits to use
	 * @return the number of digits to apply
	 */
	protected int getNumberOfDigits(ReturnRequestModel returnRequest)
	{
		return returnRequest.getOrder().getCurrency().getDigits().intValue();
	}

}
