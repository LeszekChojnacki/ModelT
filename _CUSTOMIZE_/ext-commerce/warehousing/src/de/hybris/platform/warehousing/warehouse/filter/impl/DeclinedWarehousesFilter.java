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
package de.hybris.platform.warehousing.warehouse.filter.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.model.SourcingBanModel;
import de.hybris.platform.warehousing.sourcing.ban.service.SourcingBanService;
import de.hybris.platform.warehousing.warehouse.filter.WarehousesFilter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * This filter excludes the declined {@link WarehouseModel}(s)(if any) from given set of {@link WarehouseModel}(s)
 */
public class DeclinedWarehousesFilter implements WarehousesFilter
{
	private static Logger LOGGER = LoggerFactory.getLogger(DeclinedWarehousesFilter.class);

	private SourcingBanService sourcingBanService;

	@Override
	public Set<WarehouseModel> applyFilter(final Set<WarehouseModel> warehouses)
	{
		if (CollectionUtils.isNotEmpty(warehouses))
		{
			final Collection<SourcingBanModel> existingBans = getSourcingBanService().getSourcingBan(warehouses);
			final Collection<WarehouseModel> warehousesToExclude = existingBans.stream()
					.map(existingBan -> existingBan.getWarehouse()).collect(Collectors.toList());

			if (!warehousesToExclude.isEmpty())
			{
				LOGGER.info("Filter '{}' excluded '{}' warehouses.", getClass().getSimpleName(), warehousesToExclude.size());
				warehouses.removeAll(warehousesToExclude);
			}
		}
		return warehouses;
	}

	@Required
	public void setSourcingBanService(final SourcingBanService sourcingBanService)
	{
		this.sourcingBanService = sourcingBanService;
	}

	protected SourcingBanService getSourcingBanService()
	{
		return sourcingBanService;
	}
}
