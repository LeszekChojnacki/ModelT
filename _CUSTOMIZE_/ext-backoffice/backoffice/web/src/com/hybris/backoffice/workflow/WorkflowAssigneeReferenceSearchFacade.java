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
package com.hybris.backoffice.workflow;

import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionAssignment;
import de.hybris.platform.servicelayer.security.permissions.PermissionManagementService;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;


/**
 * Search provider for {@link PrincipalModel}. Returns all groups and employees that has permission for the
 * WorkflowAction type.
 */
public class WorkflowAssigneeReferenceSearchFacade implements ReferenceEditorSearchFacade<PrincipalModel>
{
	private PermissionManagementService permissionManagementService;
	private TypeService typeService;

	@Override
	public Pageable<PrincipalModel> search(final SearchQueryData searchQueryData)
	{
		final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
		final Comparator<PrincipalModel> principalComparator = Comparator
				.comparing((final PrincipalModel principal) -> principal.getDisplayName(), nullSafeStringComparator)
				.thenComparing(PrincipalModel::getUid, nullSafeStringComparator);

		final List<PrincipalModel> principalList = getAllPrincipals().stream()
				.filter(principal -> matchPrincipalBySearchText(principal, searchQueryData.getSearchQueryText()))
				.sorted(principalComparator).collect(Collectors.toList());
		return new PageableList(principalList, searchQueryData.getPageSize());
	}

	protected List<PrincipalModel> getAllPrincipals()
	{
		final ComposedTypeModel type = typeService.getComposedTypeForCode(WorkflowActionModel._TYPECODE);
		final Collection<PermissionAssignment> permissions = permissionManagementService.getItemPermissionsForName(type,
				PermissionsConstants.READ);
		final Set<PrincipalModel> exclusions = permissions.stream().filter(permission -> permission.isDenied())
				.map(permission -> permission.getPrincipal()).collect(Collectors.toCollection(HashSet::new));
		final List<PrincipalModel> principals = permissions.stream().filter(permission -> permission.isGranted())
				.map(permission -> permission.getPrincipal()).collect(Collectors.toList());

		final Set<PrincipalModel> res = new HashSet<>();
		principals.forEach(principal -> res.addAll(getAllPrincipalsWithPermissions(principal, exclusions)));
		return new ArrayList(res);
	}

	protected List<PrincipalModel> getAllPrincipalsWithPermissions(final PrincipalModel principal,
			final Set<PrincipalModel> exclusions)
	{
		if (!exclusions.contains(principal))
		{
			final List<PrincipalModel> res = Lists.newArrayList(principal);
			if (principal instanceof UserGroupModel && ((UserGroupModel) principal).getMembers() != null)
			{
				for (final PrincipalModel member : ((UserGroupModel) principal).getMembers())
				{
					res.addAll(getAllPrincipalsWithPermissions(member, exclusions));
				}
			}
			return res;
		}
		return Collections.emptyList();
	}

	protected boolean matchPrincipalBySearchText(final PrincipalModel principal, final String searchText)
	{
		return StringUtils.isEmpty(searchText) || StringUtils.containsIgnoreCase(principal.getDisplayName(), searchText)
				|| StringUtils.containsIgnoreCase(principal.getUid(), searchText);
	}

	protected PermissionManagementService getPermissionManagementService()
	{
		return permissionManagementService;
	}

	@Required
	public void setPermissionManagementService(final PermissionManagementService permissionManagementService)
	{
		this.permissionManagementService = permissionManagementService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

}
