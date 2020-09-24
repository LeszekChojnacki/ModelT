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
package de.hybris.platform.warehousing.asn.strategy.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.asn.strategy.WarehouseSelectionForAsnStrategy;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;

import org.apache.commons.collections.CollectionUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link WarehouseSelectionForAsnStrategy}. Returns first {@link WarehouseModel} for
 * {@link AdvancedShippingNoticeModel#POINTOFSERVICE}. <br>
 * Or <tt>null</tt>, when {@link PointOfServiceModel#WAREHOUSES} is empty.
 */
public class DefaultWarehouseSelectionForAsnStrategy implements WarehouseSelectionForAsnStrategy
{
	@Override
	public WarehouseModel getDefaultWarehouse(final AdvancedShippingNoticeModel advancedShippingNotice)
	{
		validateParameterNotNullStandardMessage("advancedShippingNotice", advancedShippingNotice);

		final PointOfServiceModel pos = advancedShippingNotice.getPointOfService();
		validateParameterNotNull(pos, "No Point of Service assigned to ASN:" + advancedShippingNotice.getExternalId() + (
				advancedShippingNotice.getInternalId() != null ?
						":" + advancedShippingNotice.getInternalId() : ""));

		return CollectionUtils.isNotEmpty(pos.getWarehouses()) ? pos.getWarehouses().get(0) : null;
	}
}
