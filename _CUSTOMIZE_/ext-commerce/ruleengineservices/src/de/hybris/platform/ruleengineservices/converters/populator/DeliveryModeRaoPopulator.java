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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;


/**
 * Populates DeliveryModeRAO from DeliveryModeModel.
 *
 */
public class DeliveryModeRaoPopulator implements Populator<DeliveryModeModel, DeliveryModeRAO>
{
	@Override
	public void populate(final DeliveryModeModel source, final DeliveryModeRAO target)
	{
		target.setCode(source.getCode());
	}
}
