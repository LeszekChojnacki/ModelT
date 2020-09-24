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
package de.hybris.platform.warehousingbackoffice.actions.ship;

import com.google.common.collect.Sets;
import com.hybris.cockpitng.actions.ActionContext;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class ConfirmShippedConsignmentActionTest
{
	@InjectMocks
	private ConfirmShippedConsignmentAction action;
	@Mock
	private WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;
	@Mock
	private ActionContext context;
	@Mock
	private ConsignmentModel consignment;
	@Mock
	private ConsignmentEntryModel entry1;
	@Mock
	private ConsignmentEntryModel entry2;
	@Mock
	private Object externalFulfillmentConfiguration;

	@Before
	public void setUp()
	{
		when(context.getData()).thenReturn(consignment);
	}

	@Test
	public void returnFalse_notInstanceOfConsignmentModel()
	{
		when(context.getData()).thenReturn("test");

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_nullConsignment()
	{
		when(context.getData()).thenReturn(null);

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_pickUpConsignment()
	{
		when(consignment.getDeliveryMode()).thenReturn(new PickUpDeliveryModeModel());

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_nullConsignmentEntries()
	{
		when(consignment.getConsignmentEntries()).thenReturn(null);

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_nullQuantityPending()
	{
		when(entry1.getQuantityPending()).thenReturn(null);
		when(entry2.getQuantityPending()).thenReturn(null);
		when(consignment.getConsignmentEntries()).thenReturn(Sets.newHashSet(entry1, entry2));

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_noQuantityPending()
	{
		when(entry1.getQuantityPending()).thenReturn(Long.valueOf(0L));
		when(entry2.getQuantityPending()).thenReturn(Long.valueOf(0L));
		when(consignment.getConsignmentEntries()).thenReturn(Sets.newHashSet(entry1, entry2));

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnFalse_externalFulfillmentConfiguration()
	{
		when(consignment.getFulfillmentSystemConfig()).thenReturn(externalFulfillmentConfiguration);

		final boolean result = action.canPerform(context);
		assertFalse(result);
	}

	@Test
	public void returnTrue_remainingQuantityPending()
	{
		when(entry1.getQuantityPending()).thenReturn(Long.valueOf(0L));
		when(entry2.getQuantityPending()).thenReturn(Long.valueOf(5L));
		when(consignment.getConsignmentEntries()).thenReturn(Sets.newHashSet(entry1, entry2));

		final boolean result = action.canPerform(context);
		assertTrue(result);
	}
}
