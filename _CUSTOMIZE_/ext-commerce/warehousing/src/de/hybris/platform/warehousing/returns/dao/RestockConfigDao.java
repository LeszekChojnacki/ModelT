/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.returns.dao;

import de.hybris.platform.warehousing.model.RestockConfigModel;
import de.hybris.platform.warehousing.returns.RestockException;


/**
 * The restock config Dao
 */
public interface RestockConfigDao
{
	/**
	 * Retrieves a  {@link de.hybris.platform.warehousing.model.RestockConfigModel}
	 *
	 * @return the unique {@link de.hybris.platform.warehousing.model.RestockConfigModel}
	 */
	RestockConfigModel getRestockConfig() throws RestockException;


}
