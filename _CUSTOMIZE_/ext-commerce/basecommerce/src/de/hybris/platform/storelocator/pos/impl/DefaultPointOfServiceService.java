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
package de.hybris.platform.storelocator.pos.impl;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.pojo.StoreCountInfo;
import de.hybris.platform.store.pojo.StoreCountType;
import de.hybris.platform.storelocator.PointOfServiceDao;
import de.hybris.platform.storelocator.exception.PointOfServiceDaoException;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.CollectionUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Implementation for PointOfServiceService - interface for point of service look up functionality.
 */
public class DefaultPointOfServiceService implements PointOfServiceService
{
	private PointOfServiceDao pointOfServiceDao;

	@Override
	public PointOfServiceModel getPointOfServiceForName(final String name) throws UnknownIdentifierException
	{
		validateParameterNotNull(name, "name");
		try
		{
			return getPointOfServiceDao().getPosByName(name);
		}
		catch (final PointOfServiceDaoException e)
		{
			throw new UnknownIdentifierException(e.getMessage(), e);
		}
	}

	@Override
	public List<StoreCountInfo> getPointOfServiceCounts(final BaseStoreModel baseStore)
	{
		validateParameterNotNullStandardMessage("baseStore", baseStore);
		final Map<CountryModel, Integer> countryMap = getPointOfServiceDao().getPointOfServiceCountPerCountryForStore(baseStore);
		return countryMap.entrySet().stream().map(countryModelIntegerEntry -> {
			final StoreCountInfo countryStoreCountInfo = buildStoreCountInfo(StoreCountType.COUNTRY,
					countryModelIntegerEntry.getValue(), countryModelIntegerEntry.getKey().getIsocode(),
					countryModelIntegerEntry.getKey().getName());
			countryStoreCountInfo.setStoreCountInfoList(populateRegionStoreCountInfo(countryModelIntegerEntry.getKey(), baseStore));
			return countryStoreCountInfo;
		}).collect(Collectors.toList());
	}

	/**
	 * Populates a list of {@link StoreCountInfo} with the region information given a country
	 * Return an empty list if the country have no regions
	 *
	 * @param country
	 * 		given	{@link CountryModel}
	 * @return populated list of {@link StoreCountInfo} for a given country
	 */
	protected List<StoreCountInfo> populateRegionStoreCountInfo(final CountryModel country, final BaseStoreModel currentBaseStore)
	{
		List<StoreCountInfo> result = new ArrayList<>();
		if (!CollectionUtils.isEmpty(country.getRegions()))
		{
			final Map<RegionModel, Integer> regionMap = getPointOfServiceDao()
					.getPointOfServiceRegionCountForACountryAndStore(country, currentBaseStore);
			result = regionMap.keySet().stream()
					.map(regionModel -> buildStoreCountInfo(StoreCountType.REGION, regionMap.get(regionModel),
							regionModel.getIsocode(), regionModel.getName())).collect(Collectors.toList());
		}
		return result;
	}

	/**
	 * Builds a {@link StoreCountInfo} object
	 *
	 * @param type
	 * 		Either a {@link StoreCountType#COUNTRY} or {@link StoreCountType#REGION}
	 * @param count
	 * 		the store count
	 * @param isoCode
	 * 		the region or the country's given isoCode
	 * @param name
	 * 		the region or the country's given name
	 * @return {@link StoreCountInfo} built with the given information
	 */
	protected StoreCountInfo buildStoreCountInfo(final StoreCountType type, final Integer count, final String isoCode,
			final String name)
	{
		final StoreCountInfo storeCountInfo = new StoreCountInfo();
		storeCountInfo.setType(type);
		storeCountInfo.setCount(count);
		storeCountInfo.setIsoCode(isoCode);
		storeCountInfo.setName(name);
		return storeCountInfo;
	}

	protected PointOfServiceDao getPointOfServiceDao()
	{
		return pointOfServiceDao;
	}

	@Required
	public void setPointOfServiceDao(final PointOfServiceDao pointOfServiceDao)
	{
		this.pointOfServiceDao = pointOfServiceDao;
	}

}
