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
package com.hybris.backoffice.solrsearch.setup;

import java.util.Collection;


/**
 * Configuration for system setup (initialize system, or update system)
 */
public interface BackofficeSolrSearchSystemSetupConfig
{

	/**
	 * @return Collection of fully qualified root names of localized impex files to import
	 */
	Collection<String> getLocalizedRootNames();

	/**
	 * @return Collection of fully qualified names of non localized impex files to import
	 */
	Collection<String> getNonLocalizedRootNames();

	/**
	 * @return Configured file encoding for imported files
	 */
	String getFileEncoding();

	/**
	 * @return Literal for separation between root name and language code
	 */
	String getRootNameLanguageSeparator();

	/**
	 * @return Literal for separation of files in configuration
	 */
	String getListSeparator();
}
