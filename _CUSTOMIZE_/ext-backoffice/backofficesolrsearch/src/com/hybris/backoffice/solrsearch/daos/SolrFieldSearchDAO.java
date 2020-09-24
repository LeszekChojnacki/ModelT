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
package com.hybris.backoffice.solrsearch.daos;


import de.hybris.platform.core.model.ItemModel;

import java.util.List;


/**
 * Data access object used for retrieving final Solr Search results
 */
public interface SolrFieldSearchDAO
{
	/**
	 * Finds Items with PKs matching those passed as parameter
	 *
	 * @param typeCode searched type
	 * @param itemPks sorted list of primary keys long values.
	 * @return list of items ordered by itemsPks order
	 */
	List<ItemModel> findAll( String typeCode,List<Long> itemPks);
}
