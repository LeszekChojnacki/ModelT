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
package de.hybris.platform.permissionsfacades.security.impl;

import de.hybris.platform.permissionsfacades.PermissionsFacade;
import de.hybris.platform.permissionsfacades.data.CatalogPermissionsData;
import de.hybris.platform.permissionsfacades.security.CatalogPermissionsChecker;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;


public class DefaultCatalogPermissionsChecker implements CatalogPermissionsChecker
{
	private PermissionsFacade permissionsFacade;
	private static final List<String> WRITE_HTTP_METHODS = Arrays.asList("POST", "PUT", "PATCH", "DELETE");
	private static final List<String> READ_HTTP_METHODS = Arrays.asList("GET");

	@Override
	public boolean hasAccessToCatalog(final Authentication authentication, final HttpServletRequest request, final String catalog,
			final String catalogVersion)
	{

		if (WRITE_HTTP_METHODS.contains(request.getMethod()))
		{
			return hasCatalogPermission(authentication.getName(), catalog, catalogVersion, PermissionsFacade.WRITE_ACCESS_TYPE);
		}
		if (READ_HTTP_METHODS.contains(request.getMethod()))
		{
			return hasCatalogPermission(authentication.getName(), catalog, catalogVersion, PermissionsFacade.READ_ACCESS_TYPE);
		}

		return false;
	}


	protected boolean hasCatalogPermission(final String uid, final String catalog, final String catalogVersion,
			final String permissionName)
	{
		final List<CatalogPermissionsData> permissions = permissionsFacade.calculateCatalogPermissions(uid, Arrays.asList(catalog),
				Arrays.asList(catalogVersion));
		final Optional<Entry<String, String>> permission = permissions.stream()//
				.filter(p -> catalog.equals(p.getCatalogId()) && catalogVersion.equals(p.getCatalogVersion()))
				.flatMap(p -> p.getPermissions().entrySet().stream())//
				.filter(entry -> permissionName.equals(entry.getKey()) && Boolean.TRUE.toString().equals(entry.getValue()))//
				.findAny();
		return permission.isPresent();
	}

	protected PermissionsFacade getPermissionsFacade()
	{
		return permissionsFacade;
	}


	public void setPermissionsFacade(final PermissionsFacade permissionsFacade)
	{
		this.permissionsFacade = permissionsFacade;
	}

}
