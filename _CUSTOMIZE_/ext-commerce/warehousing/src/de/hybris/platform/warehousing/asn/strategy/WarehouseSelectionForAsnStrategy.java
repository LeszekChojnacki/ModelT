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
package de.hybris.platform.warehousing.asn.strategy;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;


/**
 * Strategy describing which {@link WarehouseModel} is selected for given {@link AdvancedShippingNoticeModel}
 */
public interface WarehouseSelectionForAsnStrategy
{

	/**
	 * Return default {@link WarehouseModel} for given {@link AdvancedShippingNoticeModel}
	 *
	 * @param advancedShippingNotice
	 * 		the given {@link AdvancedShippingNoticeModel} - cannot be <tt>null</tt>
	 * @return Selected {@link WarehouseModel} for given {@link AdvancedShippingNoticeModel#POINTOFSERVICE}.<br>
	 * Or <tt>null</tt> when there is a problem with retrieving {@link WarehouseModel} for {@link AdvancedShippingNoticeModel}
	 */
	WarehouseModel getDefaultWarehouse(AdvancedShippingNoticeModel advancedShippingNotice);
}
