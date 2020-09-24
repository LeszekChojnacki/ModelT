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
package de.hybris.platform.permissionsfacades;

import de.hybris.platform.permissionsfacades.data.CatalogPermissionsData;
import de.hybris.platform.permissionsfacades.data.PermissionsData;

import java.util.List;


/**
 * Facade for calculating permissions.
 */
@SuppressWarnings("squid:S1214")
public interface PermissionsFacade
{
	String READ_ACCESS_TYPE = "read";
	String WRITE_ACCESS_TYPE = "write";

	/**
	 * Calculate permissions for a principal and multiple types.
	 *
	 * @param principalUid
	 *           principal uid to retrieve the permissions for.
	 * @param types
	 *           type ids
	 * @param permissionNames
	 *           permission names
	 * @return calculated types permissions (based on principal, its groups, the types and super types)
	 */
	List<PermissionsData> calculateTypesPermissions(String principalUid, List<String> types, List<String> permissionNames);


	/**
	 * Calculate global permissions for a principal. This will look in the user group hierarchy as well.
	 *
	 * @param principalUid
	 *           principal uid to retrieve the permissions for.
	 * @param permissionNames
	 *           permissions names to look for
	 * @return calculated global permissions (based on principal and its groups)
	 */
	PermissionsData calculateGlobalPermissions(String principalUid, List<String> permissionNames);

	/**
	 * Calculate permissions for attributes
	 *
	 * @param principalUid
	 *           principal uid to retrieve the permissions for.
	 * @param attributes
	 *           fully qualified attribute of the for <ItemTypeName>.<Attributename>
	 * @param permissionNames
	 *           permissions names to look for
	 * @return calculated permissions for attributes
	 */
	List<PermissionsData> calculateAttributesPermissions(String principalUid, List<String> attributes,
			List<String> permissionNames);


	/**
	 * Calculate permissions for catalog version
	 *
	 * @param principalUid
	 *           principal uid to retrieve the permissions for.
	 * @param catalogIds
	 *           filter of catalog ids to consider
	 * @param catalogVersions
	 *           filter of catalog version to consider
	 * @return list of permissions for catalog versions that match any of the ids in catalogIds and any of the versions
	 *         in catalogVersions
	 */
	List<CatalogPermissionsData> calculateCatalogPermissions(String principalUid, List<String> catalogIds,
			List<String> catalogVersions);

}
