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
package de.hybris.platform.warehousingbackoffice.actions.printpicklist;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.warehousing.labels.service.PrintMediaService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.testing.AbstractActionUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class PrintPickListActionTest extends AbstractActionUnitTest<PrintPickListAction>
{
	@Spy
	private final PrintPickListAction printPickListAction = new PrintPickListAction();
	@Mock
	private ActionContext actionContext;
	@Mock
	private PrintMediaService printMediaService;
	@Mock
	private ConsignmentModel consignmentModel;
	@Mock
	private MediaModel mediaModel;
	@Mock
	private ConsignmentPrintDocumentStrategy consignmentPrintPickSlipStrategy;
	@Mock
	private Object externalFulfillmentConfiguration;

	@Override
	public PrintPickListAction getActionInstance()
	{
		return printPickListAction;
	}

	@Before
	public void setUp()
	{
		when(actionContext.getData()).thenReturn(consignmentModel);
		when(printPickListAction.getConsignmentPrintPickSlipStrategy()).thenReturn(consignmentPrintPickSlipStrategy);
		when(printMediaService.getMediaForTemplate(anyString(), any(ConsignmentProcessModel.class))).thenReturn(mediaModel);
		when(printMediaService.generatePopupScriptForMedia(any(MediaModel.class), anyString(), anyString(), anyString()))
				.thenReturn("TEST");
	}

	@Test
	public void shouldNotRequireConfirmation()
	{
		assertFalse(printPickListAction.needsConfirmation(actionContext));
	}

	@Test
	public void shouldReturnNullConfirmationMessage()
	{
		assertEquals(null, printPickListAction.getConfirmationMessage(actionContext));
	}

	@Test
	public void cannotPerform_nullData()
	{
		when(actionContext.getData()).thenReturn(null);
		assertFalse(printPickListAction.canPerform(actionContext));
	}

	@Test
	public void cannotPerform_notInstanceOfConsignmentModel()
	{
		when(actionContext.getData()).thenReturn("test");
		assertFalse(printPickListAction.canPerform(actionContext));
	}

	@Test
	public void cannotPerform_externalFulfillmentConfiguration()
	{
		when(consignmentModel.getFulfillmentSystemConfig()).thenReturn(externalFulfillmentConfiguration);
		assertFalse(printPickListAction.canPerform(actionContext));
	}

	@Test
	public void canPerform()
	{
		assertTrue(printPickListAction.canPerform(actionContext));
	}

	@Test
	public void shouldPerformPrintAction()
	{

		final ActionResult<ConsignmentModel> result = printPickListAction.perform(actionContext);

		assertNotNull(result);
		assertEquals(ActionResult.SUCCESS, result.getResultCode());
		verify(consignmentPrintPickSlipStrategy).printDocument(any(ConsignmentModel.class));
	}

}
