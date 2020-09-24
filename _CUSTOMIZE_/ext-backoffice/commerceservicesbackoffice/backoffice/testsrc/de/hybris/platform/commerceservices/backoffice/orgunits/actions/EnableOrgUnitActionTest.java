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
package de.hybris.platform.commerceservices.backoffice.orgunits.actions;

import static org.mockito.Mockito.mock;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.commerceservices.organization.strategies.OrgUnitAuthorizationStrategy;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hybris.cockpitng.actions.ActionContext;


@UnitTest
public class EnableOrgUnitActionTest
{
	@Mock
	private OrgUnitService orgUnitService;

	@Mock
	private UserService userService;

	@Mock
	private OrgUnitAuthorizationStrategy orgUnitAuthorizationStrategy;

	@InjectMocks
	private final EnableOrgUnitAction enableOrgUnitAction = new EnableOrgUnitAction();

	private OrgUnitModel orgUnitModel;
	private OrgUnitModel parentOfOrgUnitModel;
	private UserModel currentUser;
	private ActionContext<OrgUnitModel> ctx;


	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		orgUnitModel = mock(OrgUnitModel.class);
		parentOfOrgUnitModel = mock(OrgUnitModel.class);
		currentUser = mock(UserModel.class);
		ctx = mock(ActionContext.class);
	}

	@Test
	public void shouldEnableOrgUnit()
	{
		final Optional<OrgUnitModel> parentUnitOptional = Optional.of(parentOfOrgUnitModel);
		BDDMockito.given(ctx.getData()).willReturn(orgUnitModel);
		BDDMockito.given(orgUnitService.getParent(orgUnitModel)).willReturn(parentUnitOptional);
		BDDMockito.given(parentOfOrgUnitModel.getActive()).willReturn(Boolean.TRUE);
		enableOrgUnitAction.perform(ctx);

		Mockito.verify(orgUnitService, Mockito.times(1)).activateUnit(orgUnitModel);
	}

	@Test
	public void shouldNotEnableOrgUnitIfNull()
	{
		BDDMockito.given(ctx.getData()).willReturn(null);
		enableOrgUnitAction.perform(ctx);

		Mockito.verify(orgUnitService, Mockito.times(0)).activateUnit(Mockito.any(OrgUnitModel.class));
	}

	@Test
	public void shouldNotEnableOrgUnitIfParentIsNotActive()
	{
		parentOfOrgUnitModel.setActive(Boolean.FALSE);
		final Optional<OrgUnitModel> parentUnitOptional = Optional.of(parentOfOrgUnitModel);
		BDDMockito.given(ctx.getData()).willReturn(orgUnitModel);
		BDDMockito.given(orgUnitService.getParent(orgUnitModel)).willReturn(parentUnitOptional);
		enableOrgUnitAction.perform(ctx);

		Mockito.verify(orgUnitService, Mockito.times(0)).activateUnit(Mockito.any(OrgUnitModel.class));
	}

	@Test
	public void shouldPerformEnableOrgUnitIfAuthorized()
	{
		BDDMockito.given(ctx.getData()).willReturn(orgUnitModel);
		BDDMockito.given(orgUnitModel.getActive()).willReturn(Boolean.FALSE);
		BDDMockito.given(userService.getCurrentUser()).willReturn(currentUser);
		BDDMockito.given(Boolean.valueOf(orgUnitAuthorizationStrategy.canEditUnit(currentUser))).willReturn(Boolean.TRUE);

		Assert.assertTrue("Should be able to perform", enableOrgUnitAction.canPerform(ctx));
	}

	@Test
	public void shouldNotPerformEnableOrgUnitIfActive()
	{
		BDDMockito.given(ctx.getData()).willReturn(orgUnitModel);
		BDDMockito.given(orgUnitModel.getActive()).willReturn(Boolean.TRUE);
		BDDMockito.given(userService.getCurrentUser()).willReturn(currentUser);

		Assert.assertFalse("Should not be able to perform", enableOrgUnitAction.canPerform(ctx));
	}

	@Test
	public void shouldNotPerformEnableOrgUnitIfNotAuthorized()
	{
		BDDMockito.given(ctx.getData()).willReturn(orgUnitModel);
		BDDMockito.given(orgUnitModel.getActive()).willReturn(Boolean.FALSE);
		BDDMockito.given(userService.getCurrentUser()).willReturn(currentUser);
		BDDMockito.given(Boolean.valueOf(orgUnitAuthorizationStrategy.canEditUnit(currentUser))).willReturn(Boolean.FALSE);

		Assert.assertFalse("Should not be able to perform", enableOrgUnitAction.canPerform(ctx));
	}

	@Test
	public void shouldNotPerformEnableOrgUnitIfNullData()
	{
		BDDMockito.given(ctx.getData()).willReturn(null);

		Assert.assertFalse("Should not be able to perform", enableOrgUnitAction.canPerform(ctx));
	}
}
