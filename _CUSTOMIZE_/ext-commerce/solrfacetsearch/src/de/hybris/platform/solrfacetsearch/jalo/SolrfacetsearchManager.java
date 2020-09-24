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
package de.hybris.platform.solrfacetsearch.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.jalo.CronJobManager;
import de.hybris.platform.cronjob.jalo.Job;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearchException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloTypeException;
import de.hybris.platform.servicelayer.internal.jalo.ServicelayerJob;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.jalo.config.SolrFacetSearchConfig;
import de.hybris.platform.solrfacetsearch.jalo.indexer.cron.SolrIndexerCronJob;
import de.hybris.platform.solrfacetsearch.jalo.indexer.cron.SolrIndexerHotUpdateCronJob;
import de.hybris.platform.util.JspContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;



/**
 * This is the extension manager of the Solrfacetsearch extension.
 */
public class SolrfacetsearchManager extends GeneratedSolrfacetsearchManager
{
	/** Edit the local|project.properties to change logging behavior (properties 'log4j.*'). */
	private static final Logger LOG = Logger.getLogger(SolrfacetsearchManager.class.getName());

	/**
	 * Never call the constructor of any manager directly, call getInstance() You can place your business logic here - like
	 * registering a jalo session listener. Each manager is created once for each tenant.
	 */
	public SolrfacetsearchManager()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("constructor of SolrfacetsearchManager called.");
		}
	}

	/**
	 * Get the valid instance of this manager.
	 *
	 * @return the current instance of this manager
	 */
	public static SolrfacetsearchManager getInstance()
	{
		return (SolrfacetsearchManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
				.getExtension(SolrfacetsearchConstants.EXTENSIONNAME);
	}


	/**
	 * Use this method to do some basic work only ONCE in the lifetime of a tenant resp. "deployment". This method is called
	 * after manager creation (for example within startup of a tenant). Note that if you have more than one tenant you have
	 * a manager instance for each tenant.
	 */
	@Override
	public void init()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("init() of SolrfacetsearchManager called. " + getTenant().getTenantID());
		}
	}

	/**
	 * Use this method as a callback when the manager instance is being destroyed (this happens before system
	 * initialization, at redeployment or if you shutdown your VM). Note that if you have more than one tenant you have a
	 * manager instance for each tenant.
	 */
	@Override
	public void destroy()
	{
		//
	}

	/**
	 * Implement this method to create initial objects. This method will be called by system creator during initialization
	 * and system update. Be sure that this method can be called repeatedly.
	 *
	 * An example usage of this method is to create required cronjobs or modifying the type system (setting e.g some default
	 * values)
	 *
	 * @param params
	 *           the parameters provided by user for creation of objects for the extension
	 * @param jspc
	 *           the jsp context; you can use it to write progress information to the jsp page during creation
	 */
	@Override
	public void createEssentialData(final Map<String, String> params, final JspContext jspc)
	{
		//
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
		// implement here code creating project data
	}


	public SolrFacetSearchConfig getSolrFacetConfig(final String name)
	{
		final FlexibleSearch flexibleSearch = FlexibleSearch.getInstance();
		final String query = "SELECT {pk} FROM {SolrFacetSearchConfig} WHERE {name}=?name";
		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", name);
		final List<SolrFacetSearchConfig> result = flexibleSearch.search(query, parameters, SolrFacetSearchConfig.class)
				.getResult();
		if (result == null || result.isEmpty())
		{
			throw new FlexibleSearchException("Solr configuration [" + name + "] could not be found");
		}
		if (result.size() != 1)
		{
			throw new FlexibleSearchException("Solr configuration [" + name + "] is ambiguous.");
		}
		return result.get(0);
	}

	public SolrIndexerCronJob createSolrIndexerCronJob(final SolrFacetSearchConfig solrFacetSearchConfig,
			final EnumerationValue solrIndexerOperation) throws JaloTypeException
	{

		final String code = CronJobManager.getInstance().getNextCronjobNumber() + "-"
				+ SolrfacetsearchConstants.EXECUTE_INDEXER_OPERATION;
		return this.createSolrIndexerCronJob(code, solrFacetSearchConfig, solrIndexerOperation);
	}

	public SolrIndexerHotUpdateCronJob createSolrIndexerHotUpdateCronJob(final SolrFacetSearchConfig solrFacetSearchConfig,
			final EnumerationValue solrIndexerOperation, final String indexedType, final Collection<Item> items)
			throws JaloTypeException
	{

		final String code = CronJobManager.getInstance().getNextCronjobNumber() + "-"
				+ SolrfacetsearchConstants.EXECUTE_INDEXER_OPERATION;
		return this.createSolrIndexerHotUpdateCronJob(code, solrFacetSearchConfig, solrIndexerOperation, indexedType, items);
	}


	public SolrIndexerCronJob createSolrIndexerCronJob(final String code, final SolrFacetSearchConfig solrFacetSearchConfig,
			final EnumerationValue solrIndexerOperation) throws JaloTypeException
	{

		final JaloSession jaloSession = JaloSession.getCurrentSession();
		final ComposedType solrIndexerCronJob = jaloSession.getTypeManager().getComposedType(SolrIndexerCronJob.class);

		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(SolrIndexerCronJob.CODE, code);
		values.put(SolrIndexerCronJob.INDEXEROPERATION, solrIndexerOperation);
		values.put(SolrIndexerCronJob.FACETSEARCHCONFIG, solrFacetSearchConfig);
		values.put(SolrIndexerCronJob.LOGTODATABASE, Boolean.TRUE);
		final Job job = getSolrIndexerJob();
		values.put(SolrIndexerCronJob.JOB, job);
		return (SolrIndexerCronJob) solrIndexerCronJob.newInstance(values);
	}

	public SolrIndexerHotUpdateCronJob createSolrIndexerHotUpdateCronJob(final String code,
			final SolrFacetSearchConfig solrFacetSearchConfig, final EnumerationValue solrIndexerOperation, final String indexedType,
			final Collection<Item> items) throws JaloTypeException
	{

		final JaloSession jaloSession = JaloSession.getCurrentSession();
		final ComposedType solrIndexerCronJob = jaloSession.getTypeManager().getComposedType(SolrIndexerHotUpdateCronJob.class);

		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(SolrIndexerHotUpdateCronJob.CODE, code);
		values.put(SolrIndexerHotUpdateCronJob.INDEXEROPERATION, solrIndexerOperation);
		values.put(SolrIndexerHotUpdateCronJob.FACETSEARCHCONFIG, solrFacetSearchConfig);
		values.put(SolrIndexerHotUpdateCronJob.INDEXTYPENAME, indexedType);
		values.put(SolrIndexerHotUpdateCronJob.ITEMS, items);
		values.put(SolrIndexerHotUpdateCronJob.LOGTODATABASE, Boolean.TRUE);
		final Job job = getSolrIndexerJob(true);
		values.put(SolrIndexerHotUpdateCronJob.JOB, job);
		return (SolrIndexerHotUpdateCronJob) solrIndexerCronJob.newInstance(values);
	}


	public ServicelayerJob getSolrIndexerJob()
	{
		return getSolrIndexerJob(false);
	}

	/**
	 *
	 * @param hotUpdate
	 *           - set to true to obtain hot-update indexer job
	 * @return ServiceLayerJob
	 */
	public ServicelayerJob getSolrIndexerJob(final boolean hotUpdate)
	{
		final FlexibleSearch flexibleSearch = FlexibleSearch.getInstance();
		final String query = " select {PK} from {ServicelayerJob} where {CODE} = ?code ";
		final Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("code", hotUpdate ? SolrfacetsearchConstants.INDEXER_HOTUPDATE_JOB_SPRING_ID
				: SolrfacetsearchConstants.INDEXER_JOB_SPRING_ID);

		final List<ServicelayerJob> result = flexibleSearch.search(query, parameters, ServicelayerJob.class).getResult();
		if (result == null || result.isEmpty())
		{
			throw new FlexibleSearchException("Job definition wasn't found");
		}
		if (result.size() != 1)
		{
			throw new FlexibleSearchException("Code " + SolrfacetsearchConstants.INDEXER_JOB_SPRING_ID + " is not unambiguous.");
		}
		return result.get(0);
	}

}
