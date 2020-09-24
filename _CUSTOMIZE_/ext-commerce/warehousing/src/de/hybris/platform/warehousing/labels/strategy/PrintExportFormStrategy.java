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
package de.hybris.platform.warehousing.labels.strategy;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Strategy used to determine whether or not an Export Form should be printed for a {@link ConsignmentModel}
 */
public interface PrintExportFormStrategy
{
	/**
	 * Decides whether an export form should be printed for the given {@link ConsignmentModel}
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} for which to decide
	 * @return true if the Export Form should be printed; false otherwise
	 */
	boolean canPrintExportForm(ConsignmentModel consignmentModel);
}
