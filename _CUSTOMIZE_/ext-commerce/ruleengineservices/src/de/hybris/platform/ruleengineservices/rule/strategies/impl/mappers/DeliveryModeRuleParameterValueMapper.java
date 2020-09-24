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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.DeliveryModeService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


public class DeliveryModeRuleParameterValueMapper implements RuleParameterValueMapper<DeliveryModeModel>
{
	private DeliveryModeService deliveryModeService;

	@Override
	public String toString(final DeliveryModeModel deliveryMode)
	{
		ServicesUtil.validateParameterNotNull(deliveryMode, "Object cannot be null");
		return deliveryMode.getCode();
	}

	@Override
	public DeliveryModeModel fromString(final String deliveryModeCode)
	{
		ServicesUtil.validateParameterNotNull(deliveryModeCode, "String deliveryModeCode cannot be null");

		final DeliveryModeModel deliveryMode = deliveryModeService.getDeliveryModeForCode(deliveryModeCode);
		if (deliveryMode == null)
		{
			throw new RuleParameterValueMapperException("Cannot find delivery mode with code: " + deliveryModeCode);
		}

		return deliveryMode;
	}

	public DeliveryModeService getDeliveryModeService()
	{
		return deliveryModeService;
	}

	@Required
	public void setDeliveryModeService(final DeliveryModeService deliveryModeService)
	{
		this.deliveryModeService = deliveryModeService;
	}

}
