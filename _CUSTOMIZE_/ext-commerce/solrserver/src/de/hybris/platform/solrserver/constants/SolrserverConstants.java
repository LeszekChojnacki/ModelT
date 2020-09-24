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
package de.hybris.platform.solrserver.constants;

public class SolrserverConstants
{
	public static final String EXTENSIONNAME = "solrserver";

	public static final String SOLR_SERVER_VERSION_PROPERTY = "solrserver.solr.server.version";
	public static final String SOLR_CUSTOMIZATIONS_VERSIONS_PROPERTY = "solrserver.solr.customizations.versions";

	public static final String SOLRSERVER_CONFIGURATION_PREFIX = "solrserver.";
	public static final String SOLRSERVER_INSTANCES_REGEX = "solrserver.instances\\.(.*)";

	public static final String INSTANCE_PREFIX = "instance.";
	public static final String INSTANCE_REGEX = "instance\\.(.*)";

	public static final String INSTANCE_NAME_PROPERTY = "instance.name";
	public static final String INSTANCE_NAME_DEFAULT_VALUE = "default";

	public static final String HYBRIS_CONFIG_PATH_PROPERTY = "HYBRIS_CONFIG_PATH";
	public static final String HYBRIS_DATA_PATH_PROPERTY = "HYBRIS_DATA_PATH";
	public static final String HYBRIS_LOG_PATH_PROPERTY = "HYBRIS_LOG_PATH";

	public static final String SOLR_SERVER_PATH_PROPERTY = "SOLR_SERVER_PATH";

	private SolrserverConstants()
	{
		//empty
	}
}
