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
package de.hybris.platform.ordercancel;

import de.hybris.platform.ordercancel.dao.OrderCancelDaoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses(
{
//
		OrderCancelDaoTest.class, //
		OrderCancelRecordsHandlerTest.class, //
		OrderCancelPossibilityTest.class, //
		OrderCancelCompleteTest.class, //
		OrderCancelPartialTest.class, //
		OrderCancelHistoryEntriesTest.class //
})
public class OrderCancelTestSuite
{
	//
}
