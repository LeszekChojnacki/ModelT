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
package de.hybris.platform.permissionsfacades.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;


/**
 * Permissions checker for catalog read/write permissions.
 */
public interface CatalogPermissionsChecker
{
	/**
	 * Checks if given authentication has access to catalog/version with given request.
	 *
	 * @param authentication
	 *           principal trying to gain access
	 * @param request
	 *           request made to access catalogVersion
	 * @param catalog
	 *           name of catalog
	 * @param catalogVersion
	 *           name of catalog version
	 *
	 * @return true if request can access given catalogVersion.
	 */
	boolean hasAccessToCatalog(final Authentication authentication, final HttpServletRequest request, final String catalog,
			final String catalogVersion);
}
