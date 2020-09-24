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
package de.hybris.platform.datahubbackoffice.service.datahub;

import de.hybris.platform.core.model.user.UserModel;

/**
 * A service containing context of the which user is logged in
 */
public interface UserContext
{
	/**
	 * @return the currently logged in user
	 */
	UserModel getCurrentUser();

	/**
	 * @return whether the currently logged in user part of the Data Hub Admin Group
	 */
	boolean isUserDataHubAdmin();

	/**
	 * Verifies whether the context user belongs to the specified user group.
	 * @param userGroup name of the user group to check for.
	 * @return {@code true}, if the context user belongs to the specified group; {@code false} otherwise.
	 */
	boolean isMemberOf(String userGroup);
}
