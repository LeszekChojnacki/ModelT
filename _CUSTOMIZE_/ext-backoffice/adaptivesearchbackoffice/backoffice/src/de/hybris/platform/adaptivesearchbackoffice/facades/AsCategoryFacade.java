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
package de.hybris.platform.adaptivesearchbackoffice.facades;

import de.hybris.platform.adaptivesearchbackoffice.data.AsCategoryData;

import java.util.List;


/**
 * Facade to retrieve category related information.
 */
public interface AsCategoryFacade
{
	/**
	 * Returns the category hierarchy when no catalog version is available.
	 *
	 * @return the category hierarchy
	 */
	AsCategoryData getCategoryHierarchy();

	/**
	 * Returns the category hierarchy for a given catalog version.
	 *
	 * @param catalogId
	 *           - the catalog id
	 * @param catalogVersionName
	 *           - the catalog version name
	 *
	 * @return the category hierarchy
	 */
	AsCategoryData getCategoryHierarchy(String catalogId, String catalogVersionName);

	/**
	 * Returns breadcrumbs for the category path sent.
	 *
	 * @param categoryPath
	 *           - list of category codes
	 *
	 * @return the category bread crumb
	 */
	List<AsCategoryData> buildCategoryBreadcrumbs(List<String> categoryPath);

	/**
	 * Returns breadcrumbs for the category path sent.
	 *
	 * @param catalogId
	 *           - the catalog id
	 * @param catalogVersionName
	 *           - the catalog version name
	 * @param categoryPath
	 *           - list of category codes
	 *
	 * @return the category bread crumb
	 */
	List<AsCategoryData> buildCategoryBreadcrumbs(String catalogId, String catalogVersionName, List<String> categoryPath);
}
