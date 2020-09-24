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
package com.hybris.backoffice.solrsearch.events;

import de.hybris.platform.core.PK;

import java.util.List;

/**
 * Solr index synchronization strategy
 */
public interface SolrIndexSynchronizationStrategy
{
	/**
	 * Update item in solr index
	 * @param typecode item typecode
	 * @param pk item pk
	 */
	void updateItem(String typecode, long pk);

	/**
	 * Update item in solr index
	 * @param typecode item typecode
	 * @param pkList list of updated pks
	 */
	void updateItems(String typecode, List<PK> pkList);

	/**
	 * Remove item from solr index
	 * @param typecode item typecode
	 * @param pk item pk
	 */
	void removeItem(String typecode, long pk);


	/**
	 * Remove item from solr index
	 * @param typecode item typecode
	 * @param pkList item pk
	 */
	void removeItems(String typecode, List<PK> pkList);
}
