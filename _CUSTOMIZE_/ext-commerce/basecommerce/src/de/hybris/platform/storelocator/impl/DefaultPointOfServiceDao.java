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
package de.hybris.platform.storelocator.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Collections.emptyList;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.PointOfServiceDao;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.PointOfServiceDaoException;
import de.hybris.platform.storelocator.model.OpeningScheduleModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * DefaultPointOfServiceDao is the default implementation for PointOfServiceDao.
 */
public class DefaultPointOfServiceDao extends AbstractItemDao implements PointOfServiceDao
{
	private static final String BASE_STORE = "baseStore";

	private static final Logger LOG = Logger.getLogger(DefaultPointOfServiceDao.class);
	private static final String COUNTRY_ISO_CODE_QUERY_PARAM = "countryisocode";
	private static final String REGION_ISO_CODE_QUERY_PARAM = "regionisocode";

	@Override
	public Collection<PointOfServiceModel> getAllPos()
	{
		final String query = "SELECT {PK} FROM {" + PointOfServiceModel._TYPECODE + "}"; //NOSONAR

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final SearchResult<PointOfServiceModel> result = search(fQuery);
		return result.getResult();
	}

	@Override
	public PointOfServiceModel getPosByName(final String name)
	{
		if (name == null || "".equals(name))
		{
			throw new PointOfServiceDaoException("POS name cannot be null");
		}
		final String query =
				"SELECT {PK} FROM {" + PointOfServiceModel._TYPECODE + "} WHERE " + "{" + PointOfServiceModel.NAME + "} =?name";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter("name", name);
		final SearchResult<PointOfServiceModel> result = search(fQuery);
		return result.getResult() != null && result.getResult().size() == 1 ? result.getResult().get(0) : null;
	}


	@Override
	public Collection<PointOfServiceModel> getPosToGeocode(final int size)
	{
		if (size < 0)
		{
			throw new PointOfServiceDaoException("Batch size must be positive number");
		}
		final String query = "SELECT {pos.PK} FROM {" + PointOfServiceModel._TYPECODE + " as pos join Address as add on {pos."
				+ PointOfServiceModel.ADDRESS + "} = {add.pk}} WHERE ({pos." + PointOfServiceModel.GEOCODETIMESTAMP
				+ "} is null OR {pos." + PointOfServiceModel.GEOCODETIMESTAMP + "} < {add." + AddressModel.MODIFIEDTIME + "})";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final SearchResult<PointOfServiceModel> result = search(fQuery);
		final List<PointOfServiceModel> all = result.getResult();
		if (all == null)
		{
			return emptyList();
		}
		if (size == 0)
		{
			return all;
		}
		return all.size() <= size ? all : all.subList(0, size);
	}


	@Override
	public Collection<PointOfServiceModel> getPosToGeocode()
	{
		try
		{
			return getPosToGeocode(0);
		}
		catch (final PointOfServiceDaoException e)
		{
			LOG.warn("getPosToGeocode(0) failed", e);
		}
		return emptyList();
	}


	@Override
	public Collection<PointOfServiceModel> getAllGeocodedPOS(final GPS center, final double radius)
	{
		return getAllGeocodedPOS(center, radius, null);
	}

	@Override
	public Collection<PointOfServiceModel> getAllGeocodedPOS(final GPS center, final double radius, final BaseStoreModel baseStore)
	{
		final FlexibleSearchQuery fQuery = buildQuery(center, radius, baseStore);

		final SearchResult<PointOfServiceModel> result = search(fQuery);
		return result.getResult();
	}

	@Override
	public Map<CountryModel, Integer> getPointOfServiceCountPerCountryForStore(final BaseStoreModel baseStore)
	{
		validateParameterNotNullStandardMessage(BASE_STORE, baseStore);

		final String pointOfServicePerCountryQuery =
				"SELECT {co." + CountryModel.PK + "}, COUNT({pos." + PointOfServiceModel.PK + "})" + " FROM {"
						+ CountryModel._TYPECODE + " as co " + " JOIN " + AddressModel._TYPECODE + " as add ON {add:"
						+ AddressModel.COUNTRY + "} = {co:" + CountryModel.PK + "} " + " JOIN " + PointOfServiceModel._TYPECODE
						+ " as pos ON {pos:" + PointOfServiceModel.ADDRESS + "} = {add:" + AddressModel.PK + "} AND {pos:"
						+ PointOfServiceModel.BASESTORE + "} = ?baseStore  " + "}" + " GROUP BY {co." + CountryModel.PK + "}";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(pointOfServicePerCountryQuery);
		fQuery.setResultClassList(Arrays.asList(CountryModel.class, Integer.class));
		fQuery.addQueryParameter(PointOfServiceModel.BASESTORE, baseStore);
		final SearchResult<List> result = search(fQuery);
		final Map<CountryModel, Integer> resultMap = new HashMap<>();
		result.getResult().forEach(row -> resultMap.putIfAbsent((CountryModel) row.get(0), (Integer) row.get(1)));
		return resultMap;
	}

	@Override
	public Map<RegionModel, Integer> getPointOfServiceRegionCountForACountryAndStore(final CountryModel country,
			final BaseStoreModel baseStore)
	{
		validateParameterNotNullStandardMessage("country", country);
		validateParameterNotNullStandardMessage(BASE_STORE, baseStore);

		final String pointOfServicePerRegionQuery =
				"SELECT {re." + RegionModel.PK + "}, COUNT({pos." + PointOfServiceModel.PK + "})" + " FROM {"
						+ PointOfServiceModel._TYPECODE + " as pos " + " JOIN " + AddressModel._TYPECODE + " as add ON {pos:"
						+ PointOfServiceModel.ADDRESS + "} = {add:" + AddressModel.PK + "} " + " JOIN " + CountryModel._TYPECODE
						+ " as co ON {add:" + AddressModel.COUNTRY + "} = {co:" + CountryModel.PK + "} " + " JOIN "
						+ RegionModel._TYPECODE + " as re ON {add:" + AddressModel.REGION + "} = {re:" + RegionModel.PK + "} }"
						+ " WHERE  {co:" + CountryModel.ISOCODE + "} = ?isocode " + "AND {" + PointOfServiceModel.BASESTORE
						+ "} = ?baseStore " + " GROUP BY {re." + RegionModel.PK + "}";

		final Map<RegionModel, Integer> resultMap = new HashMap<>();
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(pointOfServicePerRegionQuery);
		fQuery.addQueryParameter(CountryModel.ISOCODE, country.getIsocode());
		fQuery.addQueryParameter(PointOfServiceModel.BASESTORE, baseStore);
		fQuery.setResultClassList(Arrays.asList(RegionModel.class, Integer.class));
		final SearchResult<List> result = search(fQuery);
		result.getResult().forEach(row -> resultMap.putIfAbsent((RegionModel) row.get(0), (Integer) row.get(1)));
		return resultMap;
	}

	@Override
	public List<PointOfServiceModel> getPosForCountry(final String countryIsoCode, final BaseStoreModel baseStore)
	{
		validateParameterNotNullStandardMessage("countryIsoCode", countryIsoCode);
		validateParameterNotNullStandardMessage(BASE_STORE, baseStore);

		final String pointsOfServicePerCountryQuery =
				"SELECT {" + PointOfServiceModel.PK + "} FROM {" + PointOfServiceModel._TYPECODE + " as pos  " + "JOIN "
						+ AddressModel._TYPECODE + " as addr ON {pos:address} = {addr:pk} " + "JOIN " + CountryModel._TYPECODE
						+ " as co ON {addr:country} = {co:pk} " + "JOIN " + OpeningScheduleModel._TYPECODE
						+ " as sched ON {pos:openingSchedule} = {sched:pk} " + "}" + "WHERE {co:" + CountryModel.ISOCODE
						+ "} = ?isocode" + " AND {pos:" + PointOfServiceModel.BASESTORE + "} = ?baseStore";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(pointsOfServicePerCountryQuery);
		fQuery.addQueryParameter(CountryModel.ISOCODE, countryIsoCode);
		fQuery.addQueryParameter(PointOfServiceModel.BASESTORE, baseStore);
		fQuery.setResultClassList(Arrays.asList(PointOfServiceModel.class));
		final SearchResult<PointOfServiceModel> result = search(fQuery);

		return result.getResult();
	}

	@Override
	public List<PointOfServiceModel> getPosForRegion(final String countryIsoCode, final String regionIsoCode,
			final BaseStoreModel baseStore)
	{
		validateParameterNotNullStandardMessage("countryIsoCode", countryIsoCode);
		validateParameterNotNullStandardMessage("regionIsoCode", regionIsoCode);
		validateParameterNotNullStandardMessage(BASE_STORE, baseStore);

		final String pointsOfServicePerCountryQuery =
				"SELECT {" + PointOfServiceModel.PK + "} FROM {" + PointOfServiceModel._TYPECODE + " as pos  " + "JOIN "
						+ AddressModel._TYPECODE + " as addr ON {pos:address} = {addr:pk} " + "JOIN " + CountryModel._TYPECODE
						+ " as co ON {addr:country} = {co:pk} " + "JOIN " + RegionModel._TYPECODE + " as re ON {addr:region} = {re:pk} "
						+ "JOIN " + OpeningScheduleModel._TYPECODE + " as sched ON {pos:openingSchedule} = {sched:pk} " + "}"
						+ "WHERE  {co:" + CountryModel.ISOCODE + "} = ?" + COUNTRY_ISO_CODE_QUERY_PARAM + " AND {re:"
						+ RegionModel.ISOCODE + "} = ?" + REGION_ISO_CODE_QUERY_PARAM + " AND {pos:" + PointOfServiceModel.BASESTORE
						+ "} = ?baseStore";

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(pointsOfServicePerCountryQuery);
		fQuery.addQueryParameter(COUNTRY_ISO_CODE_QUERY_PARAM, countryIsoCode);
		fQuery.addQueryParameter(REGION_ISO_CODE_QUERY_PARAM, regionIsoCode);
		fQuery.addQueryParameter(PointOfServiceModel.BASESTORE, baseStore);
		fQuery.setResultClassList(Arrays.asList(PointOfServiceModel.class));
		final SearchResult<PointOfServiceModel> result = search(fQuery);

		return result.getResult();
	}

	/**
	 * @param center
	 * @param radius
	 * @return FlexibleSearchQuery
	 * @throws PointOfServiceDaoException
	 */
	protected FlexibleSearchQuery buildQuery(final GPS center, final double radius)
	{
		return buildQuery(center, radius, null);
	}

	protected FlexibleSearchQuery buildQuery(final GPS center, final double radius, final BaseStoreModel baseStore)
	{
		try
		{
			final List<GPS> corners = GeometryUtils.getSquareOfTolerance(center, radius);
			if (corners == null || corners.isEmpty() || corners.size() != 2)
			{
				throw new PointOfServiceDaoException("Could not fetch locations from database. Unexpected neighborhood");
			}
			final Double latMax = Double.valueOf(corners.get(1).getDecimalLatitude());
			final Double lonMax = Double.valueOf(corners.get(1).getDecimalLongitude());
			final Double latMin = Double.valueOf(corners.get(0).getDecimalLatitude());
			final Double lonMin = Double.valueOf(corners.get(0).getDecimalLongitude());
			final StringBuilder query = new StringBuilder();
			query.append("SELECT {PK} FROM {").append(PointOfServiceModel._TYPECODE).append("} WHERE {")
					.append(PointOfServiceModel.LATITUDE).append("} is not null AND {").append(PointOfServiceModel.LONGITUDE)
					.append("} is not null AND {").append(PointOfServiceModel.LATITUDE).append("} >= ?latMin AND {")
					.append(PointOfServiceModel.LATITUDE).append("} <= ?latMax AND {").append(PointOfServiceModel.LONGITUDE)
					.append("} >= ?lonMin AND {").append(PointOfServiceModel.LONGITUDE).append("} <= ?lonMax");
			if (baseStore != null)
			{
				query.append(" AND {").append(PointOfServiceModel.BASESTORE).append("} = ?baseStore");
			}

			final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query.toString());
			fQuery.addQueryParameter("latMax", latMax);
			fQuery.addQueryParameter("latMin", latMin);
			fQuery.addQueryParameter("lonMax", lonMax);
			fQuery.addQueryParameter("lonMin", lonMin);
			if (baseStore != null)
			{
				fQuery.addQueryParameter(BASE_STORE, baseStore);
			}

			return fQuery;
		}
		catch (final GeoLocatorException e)
		{
			throw new PointOfServiceDaoException("Could not fetch locations from database, due to :" + e.getMessage(), e);
		}
	}
}
