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
package de.hybris.platform.basecommerce.site.dao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.List;


/**
 * DAO for accessing BaseSites.
 */
public interface BaseSiteDao
{
	/**
	 * Returns list of all BaseSites or null when nothing was found.
	 */
	List<BaseSiteModel> findAllBaseSites();

	/**
	 * Returns BaseSite with given Uid or null when nothing was found.
	 */
	BaseSiteModel findBaseSiteByUID(String siteUid);
}
