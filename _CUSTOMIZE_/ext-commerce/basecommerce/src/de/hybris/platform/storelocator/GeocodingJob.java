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

import static java.util.Optional.empty;
import static java.util.Optional.of;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.exception.GeoServiceWrapperException;
import de.hybris.platform.storelocator.location.impl.DistanceUnawareLocation;
import de.hybris.platform.storelocator.model.GeocodeAddressesCronJobModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.apache.log4j.Logger;


/**
 * Job that takes a batch of POS entries that need geocoding and performs it using geoServiceWrapper
 */
public class GeocodingJob extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(GeocodingJob.class.getName());

	private PointOfServiceDao pointOfServiceDao;
	private GeoWebServiceWrapper geoServiceWrapper;

	@Override
	public PerformResult perform(final CronJobModel cronJob)
	{
		cronJob.setLogToDatabase(Boolean.TRUE);
		cronJob.setLogToFile(Boolean.TRUE);

		try
		{
			final Optional<GeocodeAddressesCronJobModel> cronJobModelOptional = validateCronJob(cronJob);
			if (cronJobModelOptional.isPresent())
			{
				final GeocodeAddressesCronJobModel cronJobModel = cronJobModelOptional.get();
				final Collection<PointOfServiceModel> posBatch = pointOfServiceDao.getPosToGeocode(cronJobModel.getBatchSize());
				for (final PointOfServiceModel pos : posBatch)
				{
					// Check if the job has been aborted
					if (clearAbortRequestedIfNeeded(cronJob)) //NOSONAR
					{
						return new PerformResult(CronJobResult.UNKNOWN, CronJobStatus.ABORTED);
					}

					storeDistanceUnawareLocation(pos);

					// Check if the job has been aborted
					if (clearAbortRequestedIfNeeded(cronJob)) //NOSONAR
					{
						return new PerformResult(CronJobResult.UNKNOWN, CronJobStatus.ABORTED);
					}
					if (!processInternalDelay(cronJobModel)) //NOSONAR
					{
						return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
					}
				}
			}
			else
			{
				return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Unexpected error", e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 * wait for the defined internal delay
	 */
	protected boolean processInternalDelay(final GeocodeAddressesCronJobModel cronJob)
	{
		try
		{
			Thread.sleep(cronJob.getInternalDelay() * 1000L);
			return true;
		}
		catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
			LOG.error("CronJob: " + cronJob.getCode() + " interrupted: " + e.getMessage());
			return false;
		}
	}

	/**
	 * calculates and stores the GPS information for the given point of service
	 */
	protected void storeDistanceUnawareLocation(final PointOfServiceModel pos)
	{
		final DistanceUnawareLocation location = new DistanceUnawareLocation(pos);
		try
		{
			final GPS gps = getGeoServiceWrapper().geocodeAddress(location);
			pos.setLatitude(Double.valueOf(gps.getDecimalLatitude()));
			pos.setLongitude(Double.valueOf(gps.getDecimalLongitude()));
			pos.setGeocodeTimestamp(new Date());
			modelService.save(pos);
		}
		catch (final GeoServiceWrapperException e)
		{
			LOG.error(buildErrorMessage(pos, location, e));
		}
	}

	protected Optional<GeocodeAddressesCronJobModel> validateCronJob(final CronJobModel cronJob)
	{
		if (cronJob instanceof GeocodeAddressesCronJobModel)
		{
			final GeocodeAddressesCronJobModel cronJobModel = (GeocodeAddressesCronJobModel) cronJob;
			if (cronJobModel.getBatchSize() == 0)
			{
				LOG.warn("Batch size should be greater than 0");
			}
			if (cronJobModel.getInternalDelay() == 0)
			{
				LOG.warn("Internal delay should be greater than 0");
			}
			return of(cronJobModel);
		}
		else
		{
			LOG.error("Unexpected cronjob type: " + cronJob);
		}
		return empty();
	}


	protected String buildErrorMessage(final PointOfServiceModel pos, final DistanceUnawareLocation location,
			final GeoServiceWrapperException geoServiceWrapperException)
	{
		final AddressData address = location.getAddressData();

		final StringBuilder buf = new StringBuilder();

		buf.append("Failed to Geocode POS (").append(pos.getPk()).append(") ").append(pos.getName());
		buf.append(" Error: ")
				.append(geoServiceWrapperException.getGoogleResponseCode() == null ? geoServiceWrapperException.getMessage()
						: " Google response code: " + geoServiceWrapperException.getGoogleResponseCode());
		buf.append(" Address: ").append(address.getStreet()).append(", ").append(address.getZip()).append(", ")
				.append(address.getCity()).append(", ").append(address.getCountryCode());

		return buf.toString();
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}

	protected PointOfServiceDao getPointOfServiceDao()
	{
		return pointOfServiceDao;
	}

	/**
	 * @param pointOfServiceDao
	 *           the pointOfServiceDao to set
	 */
	public void setPointOfServiceDao(final PointOfServiceDao pointOfServiceDao)
	{
		this.pointOfServiceDao = pointOfServiceDao;
	}

	protected GeoWebServiceWrapper getGeoServiceWrapper()
	{
		return geoServiceWrapper;
	}

	/**
	 * @param geoServiceWrapper
	 *           the geoServiceWrapper to set
	 */
	public void setGeoServiceWrapper(final GeoWebServiceWrapper geoServiceWrapper)
	{
		this.geoServiceWrapper = geoServiceWrapper;
	}
}
