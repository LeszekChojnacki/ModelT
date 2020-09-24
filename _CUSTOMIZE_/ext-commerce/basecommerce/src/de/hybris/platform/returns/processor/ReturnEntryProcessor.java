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
package de.hybris.platform.returns.processor;

import de.hybris.platform.returns.model.ReturnEntryModel;

import java.util.List;


/**
 * By implementing you have the handle your final Returns Entry processing. For example for handling consignment
 * creation
 * 
 */
public interface ReturnEntryProcessor
{
	/**
	 * Here you have the chance handle your final Returns Entry processing. For example for handling consignment creation
	 * 
	 * @param entries
	 *           the entries to be process
	 */
	void process(List<ReturnEntryModel> entries);
}
