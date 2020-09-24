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
package de.hybris.platform.store.daos;

import de.hybris.platform.store.BaseStoreModel;

import java.util.List;


/**
 * The Interface BaseStoreDao for finding <code>BaseStoreModel</code> objects.
 * 
 * @spring.bean baseStoreDao
 */
public interface BaseStoreDao
{
	/**
	 * Find <code>BaseStoreModel</code> objects for uid.
	 * 
	 * @param uid
	 *           the uid of <code>BaseStoreModel</code> objects to find.
	 * @return the resulting collection of <code>BaseStoreModel</code> objects or empty list when not found.
	 */
	List<BaseStoreModel> findBaseStoresByUid(String uid);

	/**
	 * Find all <code>BaseStoreModel</code> objects
	 * 
	 * @return the resulting list of <code>BaseStoreModel</code> objects or empty list when not found.
	 */
	List<BaseStoreModel> findAllBaseStores();
}
