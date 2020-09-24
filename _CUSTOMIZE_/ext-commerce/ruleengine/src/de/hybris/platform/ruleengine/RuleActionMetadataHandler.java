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
package de.hybris.platform.ruleengine;


/**
 * The interface declares methods for rule action metadata processors.
 *
 */
public interface RuleActionMetadataHandler<T>
{
	/**
	 * Handle metadata value on rule action.
	 * 
	 * @param processingContext
	 *           data to process for the metadata value
	 * @param metadataValue
	 */
	void handle(final T processingContext, final String metadataValue);

	/**
	 * Handle metadata value on rule action undo.
	 * 
	 * @param processingContext
	 */
	void undoHandle(final T processingContext);
}
