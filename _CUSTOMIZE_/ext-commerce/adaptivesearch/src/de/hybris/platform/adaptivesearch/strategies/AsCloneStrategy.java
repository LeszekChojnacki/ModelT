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

import de.hybris.platform.core.model.ItemModel;


/**
 * Strategy for cloning.
 */
public interface AsCloneStrategy
{
	/**
	 * Creates a deep copy of the given model. The resulting object is not persisted yet in order to allow modifications
	 * like unique key adjustments etc.
	 *
	 * @param objectToClone
	 *           - the original model
	 *
	 * @return the deep copy of the model
	 */
	<T extends ItemModel> T clone(T objectToClone);
}
