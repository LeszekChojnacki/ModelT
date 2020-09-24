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
package de.hybris.platform.ruleengineservices.calculation;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;

import java.math.BigDecimal;


/**
 * Strategy to evaluate delivery cost.
 *
 */
public interface DeliveryCostEvaluationStrategy
{
	BigDecimal evaluateCost(AbstractOrderModel order, DeliveryModeModel deliveryMode);
}
