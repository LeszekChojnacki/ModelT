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
package de.hybris.platform.warehousing.atp.dao.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.delivery.dao.StorePickupDao;
import de.hybris.platform.commerceservices.delivery.dao.impl.DefaultStorePickupDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collections;


/**
 * Warehousing default implementation of {@link StorePickupDao} using the warehousing deliveryMode relations to
 * determine if a product is available for pickup.
 */
public class WarehousingStorePickupDao extends DefaultStorePickupDao implements StorePickupDao
{
	protected static final String PICKUP_CODE = "pickup";

	protected static final String PICKUP_WAREHOUSING_CHECK_QUERY = "SELECT 1 FROM {StockLevel as sl "
			+ "JOIN PointOfService as pos ON {pos.baseStore} = ?baseStore "
			+ "JOIN PoS2WarehouseRel as p2w ON {p2w.source} = {pos.pk} AND {p2w.target} = {sl.warehouse} "
			+ "JOIN Warehouse2DeliveryModeRelation as w2d ON {w2d.source} = {sl.warehouse} "
			+ "JOIN DeliveryMode as del ON {del.pk} = {w2d.target}}"
			+ "WHERE {sl.productCode} = ?productCode AND {del.code} = ?pickup ";

	@Override
	public Boolean checkProductForPickup(final String productCode, final BaseStoreModel baseStoreModel)
	{
		ServicesUtil.validateParameterNotNull(productCode, "productCode cannot be null");
		ServicesUtil.validateParameterNotNull(baseStoreModel, "baseStoreModel cannot be null");

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(PICKUP_WAREHOUSING_CHECK_QUERY + IN_STOCK_DEFINITION);
		fQuery.addQueryParameter("productCode", productCode);
		fQuery.addQueryParameter("baseStore", baseStoreModel);
		fQuery.addQueryParameter("pickup", PICKUP_CODE);
		fQuery.addQueryParameter("forceInStock", InStockStatus.FORCEINSTOCK);
		fQuery.addQueryParameter("forceOutOfStock", InStockStatus.FORCEOUTOFSTOCK);
		fQuery.setNeedTotal(false);
		fQuery.setCount(1);
		fQuery.setResultClassList(Collections.singletonList(Integer.class));

		final int resultSize = getFlexibleSearchService().search(fQuery).getResult().size();
		return Boolean.valueOf(resultSize > 0);
	}

}
