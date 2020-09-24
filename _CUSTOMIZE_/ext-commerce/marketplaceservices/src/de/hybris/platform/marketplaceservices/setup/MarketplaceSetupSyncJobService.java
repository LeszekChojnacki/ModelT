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
package de.hybris.platform.marketplaceservices.setup;

import de.hybris.platform.catalog.jalo.CatalogManager;
import de.hybris.platform.catalog.jalo.SyncItemJob;
import de.hybris.platform.catalog.jalo.synchronization.CatalogVersionSyncJob;
import de.hybris.platform.commerceservices.setup.SetupSyncJobService;
import de.hybris.platform.commerceservices.setup.data.EditSyncAttributeDescriptorData;
import de.hybris.platform.commerceservices.setup.impl.DefaultSetupSyncJobService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link SetupSyncJobService}, overrides {@link DefaultSetupSyncJobService} and defines new edit sync
 * descriptors for globalMarketplaceProductCatalog
 */
public class MarketplaceSetupSyncJobService extends DefaultSetupSyncJobService
{
	private static final Logger LOG = Logger.getLogger(MarketplaceSetupSyncJobService.class);
	private Map<String, Map<Class<?>, List<EditSyncAttributeDescriptorData>>> productCatalogEditSyncDescriptorsMapping = new LinkedHashMap<>();

	@Override
	public void createProductCatalogSyncJob(final String catalogId)
	{
		final SyncItemJob syncItemJob = getCatalogSyncJob(catalogId);
		if (Objects.isNull(syncItemJob))
		{
			LOG.info("Creating product sync item job for [" + catalogId + "]");

			final Map args = new HashMap();
			args.put(CatalogVersionSyncJob.CODE, createJobIdentifier(catalogId));
			args.put(CatalogVersionSyncJob.SOURCEVERSION,
					getCatalogVersionService().getCatalogVersion(catalogId, CatalogManager.OFFLINE_VERSION).getItemModelContext()
							.getSource());
			args.put(CatalogVersionSyncJob.TARGETVERSION,
					getCatalogVersionService().getCatalogVersion(catalogId, CatalogManager.ONLINE_VERSION).getItemModelContext()
							.getSource());
			args.put(CatalogVersionSyncJob.CREATENEWITEMS, Boolean.TRUE);
			args.put(CatalogVersionSyncJob.REMOVEMISSINGITEMS, Boolean.TRUE);
			final CatalogVersionSyncJob syncJob = CatalogManager.getInstance().createCatalogVersionSyncJob(args);
			processRootTypes(syncJob, catalogId, getProductCatalogRootTypeCodes());
			processEditSyncAttributeDescriptors(syncJob, catalogId, getProductCatalogEditSyncDescriptors(catalogId));

			LOG.info("Created product sync item job [" + syncJob.getCode() + "]");
		}
		else if (getProductCatalogEditSyncDescriptorsMapping().containsKey(catalogId))
		{
			processEditSyncAttributeDescriptors(syncItemJob, catalogId, getProductCatalogEditSyncDescriptorsMapping().get(catalogId));
		}
	}

	protected Map<Class<?>, List<EditSyncAttributeDescriptorData>> getProductCatalogEditSyncDescriptors(final String catalogId)
	{
		if (getProductCatalogEditSyncDescriptorsMapping().containsKey(catalogId))
		{
			return getProductCatalogEditSyncDescriptorsMapping().get(catalogId);
		}
		return getProductCatalogEditSyncDescriptors();
	}

	protected Map<String, Map<Class<?>, List<EditSyncAttributeDescriptorData>>> getProductCatalogEditSyncDescriptorsMapping()
	{
		return productCatalogEditSyncDescriptorsMapping;
	}

	@Required
	public void setProductCatalogEditSyncDescriptorsMapping(
			final Map<String, Map<Class<?>, List<EditSyncAttributeDescriptorData>>> productCatalogEditSyncDescriptorsMapping)
	{
		this.productCatalogEditSyncDescriptorsMapping = productCatalogEditSyncDescriptorsMapping;
	}
}
