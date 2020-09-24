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
package de.hybris.platform.basecommerce.strategies.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.strategies.BaseStoreSelectorStrategy;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link BaseStoreSelectorStrategy}. This implementation retrieves the first baseStore from
 * the current {@link BaseSiteModel}.
 */
public class DefaultBaseStoreSelectorStrategy implements BaseStoreSelectorStrategy
{
	private BaseSiteService siteService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.commerceservices.strategies.BaseStoreSelectorStrategy#getCurrentBaseStore()
	 */
	@Override
	public BaseStoreModel getCurrentBaseStore()
	{
		BaseStoreModel result = null;
		final BaseSiteModel currentSite = siteService.getCurrentBaseSite();
		if (currentSite != null)
		{
			final List<BaseStoreModel> storeModels = currentSite.getStores();
			if (CollectionUtils.isNotEmpty(storeModels))
			{
				result = storeModels.get(0);
			}
		}
		return result;
	}

	/**
	 * @param siteService
	 *           the baseSiteService to set
	 */
	@Required
	public void setBaseSiteService(final BaseSiteService siteService)
	{
		this.siteService = siteService;
	}


}
