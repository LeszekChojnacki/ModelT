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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.fraud.model.ProductOrderLimitModel;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;


/**
 *
 */
public class OrderEntriesSymptom extends AbstractOrderFraudSymptomDetection
{

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection#recognizeSymptom(de.hybris.platform.fraud
	 * .impl.FraudServiceResponse, de.hybris.platform.core.model.order.AbstractOrderModel)
	 */
	@Override
	public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudResponse, final AbstractOrderModel order)
	{
		for (final AbstractOrderEntryModel orderEntry : order.getEntries())
		{
			final ProductOrderLimitModel limits = orderEntry.getProduct().getProductOrderLimit();
			if (null != limits && null != limits.getMaxNumberPerOrder()
					&& orderEntry.getQuantity().longValue() > limits.getMaxNumberPerOrder().longValue())
			{
				fraudResponse
						.addSymptom(new FraudSymptom(getSymptomName(), getIncrement(), "Product :" + orderEntry.getProduct().getCode()
								+ " quantity:" + orderEntry.getQuantity() + " allowed quantity:" + limits.getMaxNumberPerOrder()));
			}
		}

		return fraudResponse;
	}
}
