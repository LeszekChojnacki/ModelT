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
package de.hybris.platform.storelocator.pos;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.pojo.StoreCountInfo;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.List;


/**
 * Interface for point of service look up functionality.
 */
public interface PointOfServiceService
{
	/**
	 * Returns point of service by its name.
	 *
	 * @param name
	 * 		the name of POS
	 * @return the point of service
	 * @throws UnknownIdentifierException
	 * 		the unknown identifier exception when no POS with given name was found
	 * @throws IllegalArgumentException
	 * 		the illegal argument exception when given name is null
	 */
	PointOfServiceModel getPointOfServiceForName(String name) throws UnknownIdentifierException, IllegalArgumentException;

	/**
	 * Returns a list of {@link StoreCountInfo} for all countries that have stores {@link PointOfServiceModel}
	 * and a list of {@link StoreCountInfo} for each region (if any) within a country case that region have stores {@link PointOfServiceModel}
	 * @param baseStore
	 * 		{@link BaseStoreModel}
	 * @return the count of the stores per Country and Region if any
	 */
	List<StoreCountInfo> getPointOfServiceCounts(BaseStoreModel baseStore);
}
