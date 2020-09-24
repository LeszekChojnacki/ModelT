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
package de.hybris.platform.fraud.symptom.impl;


import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;
import de.hybris.platform.fraud.strategy.OrderFraudSymptomDetection;

import org.springframework.beans.factory.annotation.Required;


/**
 * DifferentAddressesSymptom implements a symptom for detecting different addresses.
 */
public class DifferentAddressesSymptom extends AbstractOrderFraudSymptomDetection
{

	private boolean firstTimeOrderRule;
	private boolean countryOnly;

	private OrderFraudSymptomDetection firstOrderSymptomDetection; //NOPMD

	public OrderFraudSymptomDetection getFirstOrderSymptomDetection()
	{
		return firstOrderSymptomDetection;
	}

	@Required
	public void setFirstOrderSymptomDetection(final OrderFraudSymptomDetection firstOrderSymptomDetection) //NOPMD
	{
		this.firstOrderSymptomDetection = firstOrderSymptomDetection;
	}

	public boolean isFirstTimeOrderRule()
	{
		return firstTimeOrderRule;
	}

	public void setFirstTimeOrderRule(final boolean firstTimeOrderRule)
	{
		this.firstTimeOrderRule = firstTimeOrderRule;
	}

	public boolean isCountryOnly()
	{
		return countryOnly;
	}

	public void setCountryOnly(final boolean countryOnly)
	{
		this.countryOnly = countryOnly;
	}

	@Override
	public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudResponse, final AbstractOrderModel order)
	{
		final AddressModel shippingAddress = order.getDeliveryAddress();
		final AddressModel billingAddress = order.getPaymentAddress();

		final boolean addresesDifferent = !verifyAddresses(shippingAddress, billingAddress);

		if ((addresesDifferent && !firstTimeOrderRule) || (addresesDifferent && firstTimeOrderRule && fraudResponse // NOSONAR
				.getScore(((AbstractOrderFraudSymptomDetection) getFirstOrderSymptomDetection()).getSymptomName()) > 0))
		{
			fraudResponse.addSymptom(new FraudSymptom(getSymptomName(), super.getIncrement()));
		}
		else
		{
			fraudResponse.addSymptom(new FraudSymptom(getSymptomName(), 0));
		}

		return fraudResponse;
	}

	protected boolean verifyAddresses(final AddressModel shipping, final AddressModel delivery)
	{
		if (shipping == null || delivery == null)
		{
			return true;
		}

		if (nonNull(shipping.getCountry()) && !shipping.getCountry().equals(delivery.getCountry()))
		{
			return false;
		}
		if (!countryOnly)
		{
			return verifyAddressesSame(shipping, delivery);
		}
		return true;
	}

	protected boolean verifyAddressesSame(final AddressModel shipping, final AddressModel delivery)
	{
		if (isNotEqual(shipping.getTown(), delivery.getTown()))
		{
			return false;
		}
		else if (isNotEqual(shipping.getStreetname(), delivery.getStreetname()))
		{
			return false;
		}
		else if (isNotEqual(shipping.getBuilding(), delivery.getBuilding()))
		{
			return false;
		}
		return true;
	}

	protected boolean isNotEqual(final String input, final String expected)
	{
		return nonNull(input) && !input.equals(expected);
	}

}
