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
package de.hybris.platform.solrfacetsearch.provider;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.Map;


/**
 * Generates it's own set of flexible search query parameters that can be injected to the cronjobs
 */
public interface CronJobAwareParameterProvider
{

	/**
	 * Create provider's parameter in the form of Map<String, Object> representing parameter-value map.
	 *
	 * @param cronJob
	 *           - cronJob model object
	 *
	 * @param indexConfig
	 *           - context index configuration
	 *
	 * @param indexedType
	 *           context indexed type
	 * @return Map<String, Object>
	 */
	Map<String, Object> createParameters(CronJobModel cronJob, IndexConfig indexConfig, IndexedType indexedType);
}
