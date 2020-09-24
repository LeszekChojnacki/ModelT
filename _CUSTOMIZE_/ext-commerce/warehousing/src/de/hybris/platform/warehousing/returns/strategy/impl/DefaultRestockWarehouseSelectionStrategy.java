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
package de.hybris.platform.warehousing.returns.strategy.impl;

import com.google.common.collect.Sets;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.warehousing.returns.strategy.RestockWarehouseSelectionStrategy;
import de.hybris.platform.warehousing.sourcing.filter.SourcingFilterProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Optional;
import java.util.Set;


/**
 * Default implementation of the restock warehouse selection strategy service. If no warehouse can be found will return null.
 */
public class DefaultRestockWarehouseSelectionStrategy implements RestockWarehouseSelectionStrategy
{
	private SourcingFilterProcessor restockFilterProcessor;
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRestockWarehouseSelectionStrategy.class);


	@Override
	public WarehouseModel performStrategy(final ReturnRequestModel returnRequestModel)
	{
		ServicesUtil.validateParameterNotNull(returnRequestModel, "Parameter returnRequestModel cannot be null.");
		ServicesUtil.validateParameterNotNull(returnRequestModel.getReturnEntries(),"Parameter returnEntries cannot be null.");

		final Optional<ConsignmentEntryModel> consignmentEntryModel = returnRequestModel.getReturnEntries().stream()
				.flatMap(returnEntry -> returnEntry.getOrderEntry().getConsignmentEntries().stream())
				.filter(consignmentEntry -> Boolean.TRUE.equals(consignmentEntry.getConsignment().getWarehouse().getIsAllowRestock())).findFirst();

		if (consignmentEntryModel.isPresent())
		{
			return consignmentEntryModel.get().getConsignment().getWarehouse();
		}
		else
		{
			final Set<WarehouseModel> locations = Sets.newHashSet();
			getRestockFilterProcessor().filterLocations(returnRequestModel.getOrder(), locations);


			  if (CollectionUtils.isNotEmpty(locations))
			  {
				  return locations.stream().findFirst().get();
			  }
		}

		LOG.info("cannot find any warehouse for restock");
		return null;
	}

	@Required
	public void setRestockFilterProcessor(SourcingFilterProcessor restockFilterProcessor)
	{
		this.restockFilterProcessor = restockFilterProcessor;
	}

	protected SourcingFilterProcessor getRestockFilterProcessor()
	{
		return restockFilterProcessor;
	}
}
