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
package de.hybris.platform.promotions.backoffice.actions;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotions.PromotionsService;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.testing.AbstractActionUnitTest;


@UnitTest
public class CalculateWithPromotionsActionTest extends AbstractActionUnitTest<CalculateWithPromotionsAction>
{
	private static final String ERROR_MESSAGE = "recalculationFailed";
	private final CalculateWithPromotionsAction action = new CalculateWithPromotionsAction();
	private ActionContext actionContext;
	private AbstractOrderModel abstractOrderModel;
	@Mock
	private PromotionsService promotionsService;
	@Mock
	private CalculationService calculationService;

	@Override
	public CalculateWithPromotionsAction getActionInstance()
	{
		return action;
	}

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		abstractOrderModel = new AbstractOrderModel();
		actionContext = new ActionContext(abstractOrderModel, null, Collections.<String, Object> emptyMap(),
				Collections.<String, Object> emptyMap());
		action.setPromotionsService(promotionsService);
		action.setCalculationService(calculationService);
	}

	@Test
	public void testSuccessfullRecalculation()
	{
		Assert.assertTrue(action.canPerform(actionContext));
		final ActionResult result = action.perform(actionContext);
		Assert.assertEquals(ActionResult.SUCCESS, result.getResultCode());
		Assert.assertEquals(abstractOrderModel, result.getData());
	}

	@Test
	public void testErrorResult()
	{
		try
		{
			final CalculationException exception = new CalculationException(ERROR_MESSAGE);
			Mockito.doThrow(exception).when(calculationService).recalculate(abstractOrderModel);

			Assert.assertTrue(action.canPerform(actionContext));
			final ActionResult result = action.perform(actionContext);
			Assert.assertEquals(ActionResult.ERROR, result.getResultCode());
			Assert.assertEquals(ERROR_MESSAGE, result.getResultMessage());
			Assert.assertEquals(exception, result.getData());
		}
		catch (final CalculationException e)
		{
			Assert.fail();
		}
	}
}