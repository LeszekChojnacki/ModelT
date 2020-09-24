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
 * Strategy for validation.
 */
public interface AsValidationStrategy
{
	/**
	 * Validates the given model.
	 *
	 * @param model
	 *           - the model to validate
	 *
	 * @return <code>true</code> if the model is valid, <code>false</code> otherwise
	 */
	boolean isValid(ItemModel model);
}
