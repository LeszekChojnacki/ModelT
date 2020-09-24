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
package de.hybris.platform.storelocator.location.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.basecommerce.enums.PointOfServiceTypeEnum;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.PointOfServiceDao;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoLocatorException;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.exception.LocationInstantiationException;
import de.hybris.platform.storelocator.exception.LocationMapServiceException;
import de.hybris.platform.storelocator.exception.LocationServiceException;
import de.hybris.platform.storelocator.exception.MapServiceException;
import de.hybris.platform.storelocator.exception.PointOfServiceDaoException;
import de.hybris.platform.storelocator.impl.DefaultGPS;
import de.hybris.platform.storelocator.impl.GeometryUtils;
import de.hybris.platform.storelocator.location.DistanceAwareLocation;
import de.hybris.platform.storelocator.location.Location;
import de.hybris.platform.storelocator.location.LocationMapService;
import de.hybris.platform.storelocator.location.LocationService;
import de.hybris.platform.storelocator.map.Map;
import de.hybris.platform.storelocator.map.MapService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for LocationService.
 */
public class DefaultLocationService implements LocationService
{

	private static final Logger LOG = Logger.getLogger(DefaultLocationService.class.getName());

	private LocationMapService locationMapService;
	private GeoWebServiceWrapper geoServiceWrapper;
	private CommonI18NService commonI18NService;
	private MapService mapService;
	private ModelService modelService;
	private PointOfServiceDao pointOfServiceDao;

	@Override
	public boolean deleteLocation(final Location location)
	{
		if (location == null)
		{
			throw new LocationServiceException("Location cannot be null");
		}
		if (location.getName() == null || "".equals(location.getName().intern()))
		{
			throw new LocationServiceException("Location name cannot be empty");
		}
		try
		{
			final PointOfServiceModel posModel = getPointOfServiceDao().getPosByName(location.getName());
			if (posModel == null)
			{
				LOG.info("Location [" + location.getName() + "] could not be found");
				return false;
			}
			else
			{
				getModelService().remove(posModel);
				return true;
			}
		}
		catch (final ModelRemovalException | PointOfServiceDaoException e)
		{
			LOG.error("Could not delete location (" + location.getName() + ") due to: " + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public List<Location> getLocationsNearby(final GPS gps, final double distance)
	{
		return getLocationsNearby(gps, distance, null);
	}

	@Override
	public List<Location> getLocationsNearby(final GPS gps, final double distance, final BaseStoreModel baseStore)
	{
		try
		{
			return models2Locations(getPointOfServiceDao().getAllGeocodedPOS(gps, distance, baseStore));
		}
		catch (final PointOfServiceDaoException e)
		{
			throw new LocationServiceException(e);
		}
	}

	@Override
	public List<DistanceAwareLocation> getSortedLocationsNearby(final GPS gps, final double distance,
			final BaseStoreModel baseStore)
	{
		try
		{
			final List<DistanceAwareLocation> result = new ArrayList<>();
			for (final PointOfServiceModel posModel : getPointOfServiceDao().getAllGeocodedPOS(gps, distance, baseStore))
			{
				final double dist = calculateDistance(gps, posModel);
				result.add(new DefaultLocation(posModel, Double.valueOf(dist)));
			}
			Collections.sort(result);
			return result;
		}
		catch (final PointOfServiceDaoException | LocationInstantiationException | GeoLocatorException e)
		{
			throw new LocationServiceException(e.getMessage(), e);
		}
	}

	protected double calculateDistance(final GPS referenceGps, final PointOfServiceModel posModel)
	{
		if (posModel.getLatitude() != null && posModel.getLongitude() != null)
		{
			final GPS positionGPS = new DefaultGPS().create(posModel.getLatitude().doubleValue(),
					posModel.getLongitude().doubleValue());
			return GeometryUtils.getElipticalDistanceKM(referenceGps, positionGPS);
		}
		throw new LocationServiceException(
				"Unable to calculate a distance for PointOfService(" + posModel + ") due to missing  latitude, longitude value");
	}

	@Override
	public boolean saveOrUpdateLocation(final Location location)
	{
		if (location == null)
		{
			throw new LocationServiceException("Location cannot be null");
		}
		if (location.getName() == null || "".equals(location.getName().intern()))
		{
			throw new LocationServiceException("Location name cannot be empty");
		}
		String operation = "update";
		try
		{
			PointOfServiceModel posModel = getPointOfServiceDao().getPosByName(location.getName());
			if (posModel == null)
			{
				operation = "create";
				posModel = getModelService().create(PointOfServiceModel.class);
				posModel.setName(location.getName());
			}
			posModel.setDescription(location.getDescription());
			posModel.setType(PointOfServiceTypeEnum.valueOf(location.getType()));
			if (location.getGPS() != null)
			{
				posModel.setLatitude(Double.valueOf(location.getGPS().getDecimalLatitude()));
				posModel.setLongitude(Double.valueOf(location.getGPS().getDecimalLongitude()));
			}
			AddressModel address = null;
			final AddressData addressData = location.getAddressData();
			if (addressData != null)
			{
				address = getModelService().create(AddressModel.class);
				address.setOwner(posModel);
				address.setStreetname(addressData.getStreet());
				address.setStreetnumber(addressData.getBuilding());
				address.setPostalcode(addressData.getZip());
				address.setTown(addressData.getCity());
				address.setCountry(getCommonI18NService().getCountry(addressData.getCountryCode()));
				posModel.setAddress(address);
			}
			getModelService().save(posModel);
			if (address != null)
			{
				getModelService().save(address);
			}
			return true;
		}
		catch (final Exception e)
		{
			LOG.error("Could not " + operation + " location due to " + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public Location getLocationByName(final String name)
	{
		if (name == null)
		{
			throw new LocationServiceException("Location's name cannot be null");
		}
		try
		{
			final PointOfServiceModel posModel = getPointOfServiceDao().getPosByName(name);
			if (posModel != null)
			{
				return new DistanceUnawareLocation(posModel);
			}
		}
		catch (final LocationInstantiationException | PointOfServiceDaoException e)
		{
			throw new LocationServiceException("Location (" + name + ") could not be fetched, due to : " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Location getLocation(final String streetName, final String streetNumber, final String postalCode, final String town,
			final String countryCode, final boolean geocode)
	{
		final AddressData addressData = new AddressData(streetName, streetNumber, postalCode, town, countryCode);
		GPS gps = null;
		if (geocode)
		{
			try
			{
				gps = getGeoServiceWrapper().geocodeAddress(addressData);
			}
			catch (final GeoServiceWrapperException e)
			{
				throw new LocationServiceException(e.getMessage(), e);
			}
		}
		return new LocationDtoWrapper(addressData, gps);
	}

	private List<Location> models2Locations(final Collection<PointOfServiceModel> models)
	{
		try
		{
			List<Location> result = null;
			for (final PointOfServiceModel posModel : models)
			{
				if (result == null)
				{
					result = new ArrayList<>();
				}
				result.add(new DistanceUnawareLocation(posModel));
			}
			return result == null ? Collections.emptyList() : result;
		}
		catch (final LocationInstantiationException e)
		{
			throw new LocationServiceException(e);
		}
	}

	@Override
	public List<Location> getLocationsForPostcode(final String postalCode, final String countryCode, final int limitLocationsCount,
			final BaseStoreModel baseStore)
	{
		validateInputData(postalCode, "Postal code cannot be empty!");
		validateInputData(countryCode, "Country code cannot be empty!");
		try
		{
			final Map map = getMapOfLocationsForPostcode(postalCode, countryCode, limitLocationsCount, baseStore);
			return map.getPointsOfInterest();
		}
		catch (final LocationMapServiceException lmpse)
		{
			throw new LocationServiceException(lmpse);
		}
	}

	@Override
	public List<Location> getLocationsForTown(final String town, final int limitLocationsCount, final BaseStoreModel baseStore)
	{
		validateInputData(town, "Town name cannot be empty!");
		try
		{
			final Map map = getMapOfLocationsForTown(town, limitLocationsCount, baseStore);
			return map.getPointsOfInterest();
		}
		catch (final LocationMapServiceException lmpse)
		{
			throw new LocationServiceException(lmpse);
		}
	}

	@Override
	public List<Location> getLocationsForSearch(final String searchTerm, final String countryCode, final int limitLocationsCount,
			final BaseStoreModel baseStore)
	{
		final List<Location> resultList = new ArrayList<>();
		resultList.addAll(getLocationsForTown(searchTerm, limitLocationsCount, baseStore));
		if (resultList.isEmpty())
		{
			resultList.addAll(getLocationsForPostcode(searchTerm, countryCode, limitLocationsCount, baseStore));
		}
		return resultList;
	}

	@Override
	public List<Location> getLocationsForPoint(final GPS gps, final int limitLocationsCount, final BaseStoreModel baseStore)
	{
		validateInputData(gps, "GPS coordinates cannot be null!");
		final Map map;
		try
		{
			map = getLocationMapService().getMapOfLocations(gps, limitLocationsCount, baseStore);
		}
		catch (final MapServiceException e)
		{
			throw new LocationServiceException(e);
		}
		return map.getPointsOfInterest();
	}

	/**
	 * Validation for input parameters.
	 *
	 * @param input
	 *           parameter to validate
	 * @param message
	 *           message to be thrown
	 */
	protected void validateInputData(final Object input, final String message)
	{
		validateParameterNotNull(input, message);
	}

	protected MapService getMapService()
	{
		return mapService;
	}

	@Required
	public void setMapService(final MapService mapService)
	{
		this.mapService = mapService;
	}

	protected LocationMapService getLocationMapService()
	{
		return locationMapService;
	}

	public void setLocationMapService(final LocationMapService locationMapService)
	{
		this.locationMapService = locationMapService;
	}

	private Map getMapOfLocationsForTown(final String town, final int limitLocationsCount, final BaseStoreModel baseStore)
	{
		return getLocationMapService().getMapOfLocationsForTown(town, limitLocationsCount, baseStore);
	}

	private Map getMapOfLocationsForPostcode(final String postalCode, final String countryCode, final int limitLocationsCount,
			final BaseStoreModel baseStore)
	{
		return getLocationMapService().getMapOfLocationsForPostcode(postalCode, countryCode, limitLocationsCount, baseStore);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected GeoWebServiceWrapper getGeoServiceWrapper()
	{
		return geoServiceWrapper;
	}

	@Required
	public void setGeoServiceWrapper(final GeoWebServiceWrapper geoServiceWrapper)
	{
		this.geoServiceWrapper = geoServiceWrapper;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
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
