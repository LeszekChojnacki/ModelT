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
package de.hybris.platform.adaptivesearch.strategies;

import java.io.Serializable;


/**
 * Represents a key in the adaptive search cache.
 */
public interface AsCacheKey extends Serializable
{
	/**
	 * Returns the scope of the cache key.
	 *
	 * @return the scope of the cache key
	 */
	AsCacheScope getScope();
}
