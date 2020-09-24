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
package de.hybris.platform.basecommerce.jalo;

import de.hybris.platform.basecommerce.constants.BasecommerceConstants;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.constants.CronJobConstants;
import de.hybris.platform.cronjob.enums.JobLogLevel;
import de.hybris.platform.cronjob.jalo.CronJobManager;
import de.hybris.platform.cronjob.jalo.Job;
import de.hybris.platform.cronjob.jalo.Trigger;
import de.hybris.platform.deeplink.jalo.media.BarcodeMedia;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SearchResult;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.enumeration.EnumerationManager;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearchException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloTypeException;
import de.hybris.platform.ordercancel.jalo.OrderCancelConfig;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.servicelayer.internal.jalo.ServicelayerJob;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.storelocator.constants.GeolocationConstants;
import de.hybris.platform.storelocator.jalo.GeocodeAddressesCronJob;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.JspContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * This is the extension manager of the Basecommerce extension.
 */
@SuppressWarnings("deprecation")
public class BasecommerceManager extends GeneratedBasecommerceManager
{
	/** Edit the local|project.properties to change logging behavior (properties 'log4j.*'). */
	private static final Logger LOG = Logger.getLogger(BasecommerceManager.class.getName());
	private static final String NO_OPTION = "no";
	private static final String YES_OPTION = "yes";
	private static final String CREATE_GEOCODECRON_JOB = "create geocoding cron job";

	/*
	 * Some important tips for development:
	 *
	 * Do NEVER use the default constructor of manager's or items. => If you want to do something whenever the manger is
	 * created use the init() or destroy() methods described below
	 *
	 * Do NEVER use STATIC fields in your manager or items! => If you want to cache anything in a "static" way, use an
	 * instance variable in your manager, the manager is created only once in the lifetime of a "deployment" or tenant.
	 */

	/**
	 * Never call the constructor of any manager directly, call getInstance() You can place your business logic here -
	 * like registering a jalo session listener. Each manager is created once for each tenant.
	 */
	public BasecommerceManager() // NOPMD
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("constructor of BasecommerceManager called.");
		}
	}

	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static BasecommerceManager getInstance()
	{
		return (BasecommerceManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(BasecommerceConstants.EXTENSIONNAME);
	}


	/**
	 * Use this method to do some basic work only ONCE in the lifetime of a tenant resp. "deployment". This method is
	 * called after manager creation (for example within startup of a tenant). Note that if you have more than one tenant
	 * you have a manager instance for each tenant.
	 */
	@Override
	@Deprecated
	public void init() //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("init() of BasecommerceManager called. " + getTenant().getTenantID());
		}
	}

	protected void createGeocodingCronJob()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating geocoding cron job");
		}
		try
		{
			final int batchSize = Config.getInt("geocode.cronjob.batchsize", 100);
			final int internalDelay = Config.getInt("geocode.cronjob.internaldelay", 3);
			final int triggerInterval = Config.getInt("geocode.cronjob.triggerinterval", 15);
			createGeocodeAddressesCronJob(Integer.valueOf(batchSize), Integer.valueOf(internalDelay), triggerInterval);
		}
		catch (final NumberFormatException nfe)
		{
			LOG.error("Could not create GeocodeAddressCronJob. Batch size and 'internal delay' parameters must be integers", nfe);
		}
		catch (final JaloTypeException e)
		{
			LOG.error("Could not create GeocodeAddressCronJob due to :" + e.getMessage(), e);
		}
	}

	/**
	 * Implement this method to create data that is used in your project. This method will be called during the system
	 * initialization.
	 *
	 * An example use is to import initial data like currencies or languages for your project from an csv file.
	 *
	 * @param params
	 *           the parameters provided by user for creation of objects for the extension
	 * @param jspc
	 *           the jsp context; you can use it to write progress information to the jsp page during creation
	 */
	@Override
	public void createProjectData(final Map<String, String> params, final JspContext jspc)
	{
		//create order cancel config
		createOrderCancelConfig(3600);

		//initialize geo-coding cronjob
		if (params.get(CREATE_GEOCODECRON_JOB) == null || params.get(CREATE_GEOCODECRON_JOB).equals(YES_OPTION))
		{
			createGeocodingCronJob();
		}
	}

	public GeocodeAddressesCronJob createGeocodeAddressesCronJob(final Integer batchSize, final Integer internalDelay,
			final int triggerInterval) throws JaloTypeException
	{
		final String code = CronJobManager.getInstance().getNextCronjobNumber() + "-" + GeolocationConstants.GEOCODECRONJOB_CODE;
		return this.createGeocodeAddressesCronJob(code, batchSize, internalDelay, triggerInterval);
	}

	public GeocodeAddressesCronJob createGeocodeAddressesCronJob(final String code, final Integer batchSize,
			final Integer internalDelay, final int triggerInterval) throws JaloTypeException
	{
		final JaloSession jaloSession = JaloSession.getCurrentSession();
		final ComposedType geocodeAddressesCronJob = jaloSession.getTypeManager().getComposedType(GeocodeAddressesCronJob.class); // NOSONAR
		final Job job = getGeocodeAddressesJob();

		final Map<String, Object> values = new HashMap<>();
		values.put(GeocodeAddressesCronJob.CODE, code);
		values.put(GeocodeAddressesCronJob.BATCHSIZE, batchSize);
		values.put(GeocodeAddressesCronJob.INTERNALDELAY, internalDelay);
		values.put(GeocodeAddressesCronJob.ACTIVE, Boolean.TRUE);
		values.put(GeocodeAddressesCronJob.LOGTODATABASE, Boolean.FALSE);
		values.put(GeocodeAddressesCronJob.LOGTOFILE, Boolean.FALSE);
		values.put(GeocodeAddressesCronJob.LOGLEVELDATABASE,
				EnumerationManager.getInstance().getEnumerationValue(JobLogLevel._TYPECODE, JobLogLevel.WARNING.name())); // NOSONAR
		values.put(GeocodeAddressesCronJob.LOGLEVELFILE,
				EnumerationManager.getInstance().getEnumerationValue(JobLogLevel._TYPECODE, JobLogLevel.WARNING.name())); // NOSONAR
		values.put(GeocodeAddressesCronJob.JOB, job);

		final GeocodeAddressesCronJob cronJob = (GeocodeAddressesCronJob) geocodeAddressesCronJob.newInstance(values);

		final CronJobManager cronjobManager = (CronJobManager) getSession().getExtensionManager()
				.getExtension(CronJobConstants.EXTENSIONNAME); // NOSONAR
		final Trigger trigger = cronjobManager.createTrigger(cronJob, 0, triggerInterval, 0, 0, true); // NOSONAR
		trigger.setActive(true);
		return cronJob;
	}

	public ServicelayerJob getGeocodeAddressesJob()
	{
		final FlexibleSearch flexibleSearch = FlexibleSearch.getInstance(); // NOSONAR
		final String query = " select {PK} from {ServicelayerJob} where {CODE} = ?code ";
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", GeolocationConstants.GEOCODEJOB_BEAN_ID);
		final List<ServicelayerJob> result = flexibleSearch.search(query, parameters, ServicelayerJob.class).getResult();
		if (result == null || result.isEmpty())
		{
			throw new FlexibleSearchException("Job definition (" + GeolocationConstants.GEOCODEJOB_BEAN_ID + ") wasn't found");
		}
		if (result.size() != 1)
		{
			throw new FlexibleSearchException("Code " + GeolocationConstants.GEOCODEJOB_BEAN_ID + " is ambiguous.");
		}
		return result.get(0);
	}

	@Override
	public Collection<BarcodeMedia> getBarcodes(final SessionContext ctx, final Product item) // NOSONAR
	{
		final String queryString = "SELECT {barcode.PK} FROM {BarcodeMedia as barcode} WHERE {barcode.contextItem} = ?contextItem";
		final Map params = new HashMap();
		params.put("contextItem", item);
		final SearchResult result = FlexibleSearch.getInstance().search(queryString, params, // NOSONAR
				Collections.singletonList(BarcodeMedia.class), true, true, 0, -1);

		return result.getResult();
	}

	protected void createOrderCancelConfig(final int queueLength)
	{
		final SearchResult result = FlexibleSearch.getInstance().search( // NOSONAR
				"SELECT {PK} FROM {" + OrderCancelConfigModel._TYPECODE + "}", null,
				Collections.singletonList(OrderCancelConfig.class), true, true, 0, -1);

		if (result.getCount() == 0)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating order cancel configuration");
			}
			final ModelService modelService = (ModelService) Registry.getApplicationContext().getBean("modelService");
			final OrderCancelConfigModel config = modelService.create(OrderCancelConfigModel.class);
			config.setOrderCancelAllowed(true);
			config.setPartialCancelAllowed(true);
			config.setPartialOrderEntryCancelAllowed(true);
			config.setCancelAfterWarehouseAllowed(true);
			config.setQueuedOrderWaitingTime(queueLength);
			modelService.save(config);
		}
	}

	@Override
	public Collection<String> getCreatorParameterNames()
	{
		final List<String> parameterNames = new ArrayList<>();
		parameterNames.add(CREATE_GEOCODECRON_JOB);
		return parameterNames;
	}

	@Override
	public List<String> getCreatorParameterPossibleValues(final String param)
	{
		final List<String> parameterNames = new ArrayList<>();
		parameterNames.add(NO_OPTION);
		parameterNames.add(YES_OPTION);
		return parameterNames;
	}

}
