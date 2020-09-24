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
package de.hybris.platform.promotions.attributehandlers;

import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;


public class PromotionOrderEntryConsumedOrderEntryNumberAttributeHandler extends
		AbstractDynamicAttributeHandler<Integer, PromotionOrderEntryConsumedModel>
{
	@Override
	public Integer get(final PromotionOrderEntryConsumedModel model)
	{
		return model.getOrderEntry() != null ? model.getOrderEntry().getEntryNumber() : model.getOrderEntryNumber();
	}
}
