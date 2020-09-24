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
package de.hybris.platform.warehousing.inventoryevent.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.warehousing.inventoryevent.dao.InventoryEventDao;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The default implementation of {@link InventoryEventDao}
 */
public class DefaultInventoryEventDao extends AbstractItemDao implements InventoryEventDao
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInventoryEventDao.class);

	protected static final String ALLOCATION_EVENTS_FOR_CONSIGNMENT_ENTRIES_QUERY =
			"SELECT {" + AllocationEventModel.PK + "} FROM {" + AllocationEventModel._TYPECODE + "} WHERE {"
					+ AllocationEventModel.CONSIGNMENTENTRY + "} IN (?consignmentEntries)";

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForConsignmentEntry(final ConsignmentEntryModel consignmentEntry)
	{
		final String query = "SELECT {" + AllocationEventModel.PK + "} FROM {" + AllocationEventModel._TYPECODE + "} WHERE {"
				+ AllocationEventModel.CONSIGNMENTENTRY + "} = ?consignmentEntry";

		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);
		fsQuery.addQueryParameter("consignmentEntry", consignmentEntry);

		return getInventoryEvents(fsQuery);
	}

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForOrderEntry(final OrderEntryModel orderEntry)
	{
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(ALLOCATION_EVENTS_FOR_CONSIGNMENT_ENTRIES_QUERY);

		fsQuery.addQueryParameter("consignmentEntries", orderEntry.getConsignmentEntries());

		return getInventoryEvents(fsQuery);
	}

	@Override
	public <T extends InventoryEventModel> Collection<T> getInventoryEventsForStockLevel(final StockLevelModel stockLevel,
			final Class<T> eventClassType)
	{
		try
		{
			final Field eventType = eventClassType.getDeclaredField("_TYPECODE");
			eventType.setAccessible(true);
			final String eventClassTypeString = (String) eventType.get(null);
			final String query = "SELECT PK FROM {" + eventClassTypeString + "} WHERE {stocklevel} = ?stockLevel";

			final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query);
			fsQuery.addQueryParameter("stockLevel", stockLevel);

			return getInventoryEvents(fsQuery);
		}
		catch (final NoSuchFieldException | IllegalAccessException e) //NOSONAR
		{
			LOGGER.info("Invalid inventory event type {}", eventClassType);
		}
		return Collections.emptyList();
	}

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForConsignment(final ConsignmentModel consignment)
	{
		final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(ALLOCATION_EVENTS_FOR_CONSIGNMENT_ENTRIES_QUERY);

		fsQuery.addQueryParameter("consignmentEntries", consignment.getConsignmentEntries());

		return getInventoryEvents(fsQuery);
	}

	protected <T extends InventoryEventModel> Collection<T> getInventoryEvents(final FlexibleSearchQuery query)
	{
		final SearchResult<T> result = getFlexibleSearchService().search(query);
		return result.getResult();
	}
}
