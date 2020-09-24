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
package de.hybris.platform.ruleengineservices.calculation.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ruleengineservices.calculation.DeliveryCostEvaluationStrategy;

import java.math.BigDecimal;


/**
 * Default implementation of {@link DeliveryCostEvaluationStrategy}.
 *
 */
public class DefaultDeliveryCostEvaluationStrategy implements DeliveryCostEvaluationStrategy
{
	@Override
	public BigDecimal evaluateCost(final AbstractOrderModel order, final DeliveryModeModel deliveryMode)
	{
		return BigDecimal.ZERO;
	}

}
