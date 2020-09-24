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
 */
package de.hybris.platform.datahubbackoffice.service.datahub.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.datahubbackoffice.service.datahub.UserContext;
import de.hybris.platform.servicelayer.user.UserService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class UserContextImplUnitTest
{
	@InjectMocks
	private final UserContext userContext = new UserContextImpl();
	@Mock
	private UserService userService;
	private final UserModel currentUser = new UserModel();
	private final UserGroupModel userGroup = new UserGroupModel();

	@Before
	public void setUp()
	{
		doReturn(currentUser).when(userService).getCurrentUser();
		doReturn(userGroup).when(userService).getUserGroupForUID("datahubadmingroup");
	}

	@Test
	public void testGetCurrentUser()
	{
		assertThat(userContext.getCurrentUser())
				.isNotNull()
				.isEqualTo(currentUser);
	}

	@Test
	public void testUserIsPartOfDataHubAdminGroup()
	{
		givenCurrentUserIsInDataHubAdminGroup(true);

		assertThat(userContext.isUserDataHubAdmin()).isTrue();
	}

	@Test
	public void testUserIsNotInDataHubAdminGroup()
	{
		givenCurrentUserIsInDataHubAdminGroup(false);

		assertThat(userContext.isUserDataHubAdmin()).isFalse();
	}

	@Test
	public void testIsMemberOf()
	{
		givenCurrentUserIsInDataHubAdminGroup(true);

		assertThat(userContext.isMemberOf("datahubadmingroup")).isTrue();
		assertThat(userContext.isMemberOf("someothergroup")).isFalse();
	}

	private void givenCurrentUserIsInDataHubAdminGroup(final boolean isDataHubAdmin)
	{
		doReturn(isDataHubAdmin).when(userService).isMemberOfGroup(currentUser, userGroup);
	}
}
