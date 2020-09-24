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
package de.hybris.platform.warehousing.sourcing.ban.service;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.model.SourcingBanModel;

import java.util.Collection;


/**
 *
 *  The service is used create and fetch Bans by warehouses
 *
 */
public interface SourcingBanService
{
	/**
	 * creates Sourcing Ban
	 * @param warehouseModel warehouse that we want to ban
	 * @return SourcingBanModel linked to the passed Warehouse, this ban will expire in 1 day by default
	 */
	SourcingBanModel createSourcingBan(WarehouseModel warehouseModel);

	/**
	 * gets SourcingBan by passing Warehouse
	 * @param warehouseModels </warehouseModel> collection of warehouse that we want to get bans for
	 * @return collection of SourcingBans that for warehouses that is active within configurable period (1 day by default)
	 */
	Collection<SourcingBanModel> getSourcingBan(Collection<WarehouseModel> warehouseModels);

}
