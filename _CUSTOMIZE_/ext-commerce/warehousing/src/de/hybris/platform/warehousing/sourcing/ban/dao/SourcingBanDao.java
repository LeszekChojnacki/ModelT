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
package de.hybris.platform.warehousing.sourcing.ban.dao;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.model.SourcingBanModel;

import java.util.Collection;
import java.util.Date;


/**
 * Sourcing Ban Dao
 *
 */
public interface SourcingBanDao
{
	/**
	 * Retrieves an {@link SourcingBanModel} for a {@link WarehouseModel}
	 *
	 * @param warehouseModels
	 *          Warehouses for which we retrieve the SourcingBans
	 * @param  currentDateMinusBannedDays bannedDays default to 1 day
	 * @return collection of SourcingBans linked with the warehouses
	 */
	Collection<SourcingBanModel> getSourcingBan (Collection<WarehouseModel> warehouseModels, Date currentDateMinusBannedDays);
}
