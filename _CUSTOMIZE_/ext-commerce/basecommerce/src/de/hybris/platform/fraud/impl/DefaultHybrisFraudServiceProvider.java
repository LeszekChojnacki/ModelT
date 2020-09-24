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
package de.hybris.platform.fraud.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.strategy.OrderFraudSymptomDetection;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;


/**
 * 
 */
public class DefaultHybrisFraudServiceProvider extends AbstractFraudServiceProvider
{
	private List<OrderFraudSymptomDetection> symptomList;

	public List<OrderFraudSymptomDetection> getSymptomList()
	{
		return symptomList == null ? Collections.emptyList() : symptomList;
	}

	public void setSymptomList(final List<OrderFraudSymptomDetection> symptomList)
	{
		this.symptomList = symptomList;
	}

	@Override
	public FraudServiceResponse recognizeOrderFraudSymptoms(final AbstractOrderModel order)
	{
		FraudServiceResponse response = new FraudServiceResponse(getProviderName());
		for (final OrderFraudSymptomDetection strategy : getSymptomList())
		{
			response = strategy.recognizeSymptom(response, order);
		}
		return response;
	}

	@Override
	public FraudServiceResponse recognizeUserActivitySymptoms(final UserModel order)
	{
		throw new NotImplementedException(getClass());
	}

}
