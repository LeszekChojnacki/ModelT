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
package com.hybris.backoffice.daos;

import java.util.Set;

import com.hybris.backoffice.model.user.BackofficeRoleModel;


/**
 * DAO to perform {@link BackofficeRoleModel}. service operations.
 *
 * @spring.bean backofficeRoleDao
 */
public interface BackofficeRoleDao
{
	/**
	 * Finds all existing {@link BackofficeRoleModel}s .
	 *
	 * @return a set of all found {@link BackofficeRoleModel}s
	 */
	Set<BackofficeRoleModel> findAllBackofficeRoles();
}
