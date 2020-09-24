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
package de.hybris.platform.warehousingbackoffice.labels.strategy;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Strategy used to print an appropriate document based on a {@link ConsignmentModel#STATUS}
 */
public interface ConsignmentPrintDocumentStrategy
{
	/**
	 * Prints a document based on the {@link ConsignmentModel#STATUS}
	 * .
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} for which a document is to be printed
	 */
	void printDocument(ConsignmentModel consignmentModel);
}
