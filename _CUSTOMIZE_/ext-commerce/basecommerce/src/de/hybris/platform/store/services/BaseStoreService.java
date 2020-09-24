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
package de.hybris.platform.store.services;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.store.BaseStore;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;


/**
 * The Interface BaseStoreService for managing base stores.
 *
 * @spring.bean baseStoreService
 */
public interface BaseStoreService
{
	/**
	 * Gets the all base stores.
	 *
	 * @return List of found <code>BaseStoreModel</code> objects.
	 */
	List<BaseStoreModel> getAllBaseStores();

	/**
	 * Gets the base store for uid.
	 *
	 * @param uid
	 *           the uid of base store
	 * @return found <code>BaseStoreModel</code> object
	 * @throws AmbiguousIdentifierException
	 *            thrown when more than one object has been found
	 * @throws UnknownIdentifierException
	 *            thrown when object has not been found
	 */
	BaseStoreModel getBaseStoreForUid(String uid) throws AmbiguousIdentifierException, UnknownIdentifierException;


	/**
	 * Gets current {@link BaseStore} basing on the {@link BaseSiteModel#getStores()} relation.
	 *
	 * @return found <code>BaseStoreModel</code> object
	 */
	BaseStoreModel getCurrentBaseStore();
}
