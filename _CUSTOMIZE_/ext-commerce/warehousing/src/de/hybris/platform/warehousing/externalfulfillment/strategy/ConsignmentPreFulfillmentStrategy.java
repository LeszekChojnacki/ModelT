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
package de.hybris.platform.warehousing.externalfulfillment.strategy;


import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Strategy to be executed whenever the given {@link ConsignmentModel} is not being fulfilled by OMS
 */
public interface ConsignmentPreFulfillmentStrategy
{
	/**
	 * Executes before the fulfillment process of the external system begins
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel}
	 */
	void perform(ConsignmentModel consignment);

	/**
	 * Decides if it execution goes to the next strategy in the list or if it goes to wait step
	 * @param consignment
	 * 		the {@link ConsignmentModel}
	 * @return false, if process have to wait or true if execution can move further
	 */
	boolean canProceedAfterPerform(ConsignmentModel consignment);
}
