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
package de.hybris.platform.promotionenginebackoffice.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleTemplateModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleTemplateModel;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.servicelayer.exceptions.SystemException;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.testing.AbstractActionUnitTest;


@UnitTest
public class RuleCreatePromotionFromTemplateActionTest extends AbstractActionUnitTest<RuleCreatePromotionFromTemplateAction>
{
	@Mock
	private PromotionSourceRuleTemplateModel ruleTemplateModel;
	@Mock
	private RuleService ruleService;

	@InjectMocks
	private final RuleCreatePromotionFromTemplateAction action = new RuleCreatePromotionFromTemplateAction();

	@Override
	public RuleCreatePromotionFromTemplateAction getActionInstance()
	{
		return action;
	}

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testTemplateNullErrorResult()
	{
		final ActionContext<PromotionSourceRuleTemplateModel> context = new ActionContext<PromotionSourceRuleTemplateModel>(null,
				null, Collections.EMPTY_MAP, Collections.EMPTY_MAP);

		assertTrue(action.canPerform(context));
		final ActionResult result = action.perform(context);
		assertEquals(ActionResult.ERROR, result.getResultCode());
	}

	@Test
	public void testErrorResult()
	{
		final ActionContext<PromotionSourceRuleTemplateModel> context = new ActionContext<PromotionSourceRuleTemplateModel>(
				ruleTemplateModel, null, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
		when(ruleService.createRuleFromTemplate((AbstractRuleTemplateModel) ruleTemplateModel))
				.thenThrow(new SystemException("Something went very wrong!"));

		assertTrue(action.canPerform(context));
		final ActionResult result = action.perform(context);
		assertEquals(ActionResult.ERROR, result.getResultCode());
	}
}
