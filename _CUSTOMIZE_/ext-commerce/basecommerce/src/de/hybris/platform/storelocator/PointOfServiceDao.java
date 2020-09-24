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
package de.hybris.platform.storelocator;


import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.exception.PointOfServiceDaoException;
import de.hybris.platform.storelocator.jalo.PointOfService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Data Access Object dealing with {@link PointOfService} type
 */
public interface PointOfServiceDao extends Dao
{

	/**
	 * return all POS items
	 *
	 * @return {@link PointOfServiceModel}
	 */
	Collection<PointOfServiceModel> getAllPos();

	/**
	 * return all POS items from the given neighborhood belonging to given store
	 *
	 * @param center
	 * 		of neighborhood
	 * @param radius
	 * 		of neighborhood in kilometers
	 * @param baseStore
	 * 		{@link BaseStoreModel}
	 * @return {@link PointOfServiceModel}
	 * @throws PointOfServiceDaoException
	 */
	Collection<PointOfServiceModel> getAllGeocodedPOS(GPS center, double radius, BaseStoreModel baseStore);

	/**
	 * return all POS items from the given neighborhood
	 *
	 * @param center
	 * 		of neighborhood
	 * @param radius
	 * 		of neighborhood in kilometers
	 * @return {@link PointOfServiceModel}
	 * @throws PointOfServiceDaoException
	 */
	Collection<PointOfServiceModel> getAllGeocodedPOS(GPS center, double radius);

	/**
	 * Returns a Point of Service with the name given
	 *
	 * @param name
	 * @return {@link PointOfServiceModel}
	 * @throws PointOfServiceDaoException
	 */
	PointOfServiceModel getPosByName(String name);

	/**
	 * Get a size limited collection of PointOfServiceModel that need to be geocoded.
	 *
	 * @param size
	 * 		determines how many entries are taken
	 * @return Collection of {@link PointOfServiceModel}
	 * @throws PointOfServiceDaoException
	 */
	Collection<PointOfServiceModel> getPosToGeocode(int size);

	/**
	 * Get collection of PointOfServiceModel that need to be geocoded.
	 *
	 * @return Collection of {@link PointOfServiceModel}
	 */
	Collection<PointOfServiceModel> getPosToGeocode();

	/**
	 * Get the count of PointOfServiceModel per country {@link CountryModel} given a {@link BaseStoreModel}
	 *
	 * @param baseStore
	 * 		given {@link BaseStoreModel}
	 * @return a map representing countries and store {@link PointOfServiceModel} counts
	 */
	Map<CountryModel, Integer> getPointOfServiceCountPerCountryForStore(BaseStoreModel baseStore);

	/**
	 * Get the count of {@link PointOfServiceModel} in each region for a given country {@link CountryModel}
	 * and {@link BaseStoreModel}
	 *
	 * @param country
	 * 		given {@link CountryModel}
	 * @param baseStore
	 * 		given {@link BaseStoreModel}
	 * @return a map representing store {@link PointOfServiceModel} counts per region {@link RegionModel}
	 * for the given country {@link CountryModel} and {@link BaseStoreModel}
	 */
	Map<RegionModel, Integer> getPointOfServiceRegionCountForACountryAndStore(CountryModel country, BaseStoreModel baseStore);

	/**
	 * Gets a list of {@link PointOfServiceModel} in a given country
	 *
	 * @param countryIsoCode
	 * 		{@link CountryModel:ISOCODE}
	 * @param baseStore
	 * 		the active {@link BaseStoreModel}
	 * @return list of {@link PointOfServiceModel}
	 */
	List<PointOfServiceModel> getPosForCountry(String countryIsoCode, BaseStoreModel baseStore);

	/**
	 * Gets a list of {@link PointOfServiceModel} in a given country and region
	 *
	 * @param countryIsoCode
	 * 		{@link CountryModel:ISOCODE}
	 * @param regionIsoCode
	 * 		{@link RegionModel:ISOCODE}
	 * @param baseStore
	 * 		the active {@link BaseStoreModel}
	 * @return list of {@link PointOfServiceModel}
	 */
	List<PointOfServiceModel> getPosForRegion(String countryIsoCode, String regionIsoCode, BaseStoreModel baseStore);
}
