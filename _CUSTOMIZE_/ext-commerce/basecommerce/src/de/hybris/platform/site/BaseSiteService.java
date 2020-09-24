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
package de.hybris.platform.site;

import de.hybris.platform.basecommerce.exceptions.BaseSiteActivationException;
import de.hybris.platform.basecommerce.jalo.site.BaseSite;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.strategies.ActivateBaseSiteInSessionStrategy;
import de.hybris.platform.catalog.model.CatalogModel;

import java.util.Collection;
import java.util.List;


/**
 * CMS unaware site related service
 *
 * @spring.bean baseSiteService
 * @since 4.5
 */
public interface BaseSiteService
{
	/**
	 * Returns all {@link BaseSite} instances.
	 *
	 * @return all BaseSite instances
	 */
	Collection<BaseSiteModel> getAllBaseSites();


	/**
	 * Returns site for the given site uid.
	 *
	 * @param siteUid
	 * 		the site's uid
	 * @return the current site
	 */
	BaseSiteModel getBaseSiteForUID(String siteUid);


	/**
	 * Returns current site from the session for current user.
	 *
	 * @return the current site
	 */
	BaseSiteModel getCurrentBaseSite();

	/**
	 * Sets the given site as current and possibly performs an additional
	 * {@link ActivateBaseSiteInSessionStrategy#activate(BaseSiteModel)} logic
	 *
	 * @param baseSiteModel
	 * 		The base site to set as the current site in the session
	 * @param activateAdditionalSessionAdjustments
	 * 		- boolean indicating whether session adjustments should be applied
	 * @throws BaseSiteActivationException
	 * 		if activation failed, might occur if activateAdditionalSessionAdjustments is true
	 */
	void setCurrentBaseSite(BaseSiteModel baseSiteModel, boolean activateAdditionalSessionAdjustments);

	/**
	 * Sets the given site as current and possibly performs an additional
	 * {@link ActivateBaseSiteInSessionStrategy#activate(BaseSiteModel)} logic
	 *
	 * @param siteUid
	 * 		The base site siteUid to load and set as the current site in the session
	 * @param activateAdditionalSessionAdjustments
	 * 		- boolean indicating whether session adjustments should be applied
	 * @throws BaseSiteActivationException
	 * 		if activation failed, might occur if activateAdditionalSessionAdjustments is true
	 */
	void setCurrentBaseSite(String siteUid, boolean activateAdditionalSessionAdjustments);

	/**
	 * Gets the product catalogs for <code>BaseSiteModel</code> object.
	 *
	 * @param site
	 * 		the <code>BaseSiteModel</code> object for which catalogs will be obtained.
	 * @return the list of <code>CatalogModel</code> objects.
	 */
	List<CatalogModel> getProductCatalogs(BaseSiteModel site);
}
