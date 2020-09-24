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
package com.hybris.backoffice.cockpitng.dataaccess.facades.permissions;

import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.security.UserRightModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.security.Principal;
import de.hybris.platform.jalo.security.UserRight;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.security.permissions.PermissionAssignment;
import de.hybris.platform.servicelayer.security.permissions.PermissionCheckResult;
import de.hybris.platform.servicelayer.security.permissions.PermissionCheckingService;
import de.hybris.platform.servicelayer.security.permissions.PermissionManagementService;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dataaccess.facades.common.PlatformFacadeStrategyHandleCache;
import com.hybris.backoffice.daos.BackofficeUserRightsDao;
import com.hybris.cockpitng.dataaccess.facades.permissions.Permission;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionInfo;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionManagementFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.permissions.impl.DefaultPermissionInfo;
import com.hybris.cockpitng.labels.LabelService;


public class DefaultPlatformPermissionManagementFacadeStrategy implements PermissionManagementFacadeStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultPlatformPermissionManagementFacadeStrategy.class);
	private final List<String> userRightsStringsInHMCOrder = Arrays.asList(PermissionsConstants.READ, PermissionsConstants.CREATE,
			PermissionsConstants.REMOVE, PermissionsConstants.CHANGE); // IMPORTANT: order of elements is extremely important!!!
	private final List<String> fieldRightsStringsInHMCOrder = Arrays.asList(PermissionsConstants.READ,
			PermissionsConstants.CHANGE); // IMPORTANT: order of elements is extremely important!!!
	private TypeService typeService;
	private PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache;
	private PermissionCheckingService permissionCheckingService;
	private PermissionManagementService permissionManagementService;
	private UserService userService;
	private ModelService modelService;
	private BackofficeUserRightsDao backofficeUserRightsDao;
	private FlexibleSearchService flexibleSearchService;
	private LabelService labelService;


	@Override
	public Permission getTypePermission(final String principalId, final String typeCode, final String permissionName)
	{
		final PrincipalModel principalModel = getPrincipalById(principalId);
		final PermissionAssignment permissionAssignement = getTypePermissionAssignmentForPrincipal(typeCode, principalModel,
				permissionName);

		boolean inherited = true;
		final boolean granted;

		if (permissionAssignement != null)
		{
			inherited = false;
			granted = permissionAssignement.isGranted();
		}
		else
		{
			granted = permissionCheckingService.checkTypePermission(typeCode, principalModel, permissionName).isGranted();
		}

		return new Permission(inherited, !granted, permissionName, principalId, typeCode, null);
	}

	@Override
	public PermissionInfo getTypePermissionInfo(final String principal, final String type)
	{
		final DefaultPermissionInfo defaultPermissionInfo = new DefaultPermissionInfo(PermissionInfo.PermissionInfoType.TYPE,
				principal, type, getPermissionMap(principal, type));
		defaultPermissionInfo.setLabel(labelService.getObjectLabel(type));
		return defaultPermissionInfo;

	}

	@Override
	public PermissionInfo getPrincipalPermissionInfo(final String principal, final String type)
	{
		final PermissionInfo info = new DefaultPermissionInfo(PermissionInfo.PermissionInfoType.PRINCIPAL, principal, type,
				getPermissionMap(principal, type));
		final PrincipalModel principalById = getPrincipalById(principal);
		if (principalById != null)
		{
			info.setLabel(labelService.getObjectLabel(principalById));
		}
		return info;
	}

	@Override
	public Permission getFieldPermission(final String principalId, final String typeCode, final String field,
			final String permissionName)
	{
		boolean inherited = true;
		final boolean granted;

		final PrincipalModel principalModel = getPrincipalById(principalId);

		final PermissionAssignment permissionAssignment = getFieldPermissionAssignmentForPrincipal(principalModel, typeCode, field,
				permissionName);
		if (permissionAssignment != null)
		{
			inherited = false;
			granted = permissionAssignment.isGranted();
		}
		else
		{
			final ComposedTypeModel composedType = typeService.getComposedTypeForCode(typeCode);
			final Set<AttributeDescriptorModel> attributeDescriptorsForType = typeService
					.getAttributeDescriptorsForType(composedType);
			final AttributeDescriptorModel attributeDescriptor = getAttributeDescriptor(attributeDescriptorsForType, field);
			if (attributeDescriptor != null)
			{
				final PermissionCheckResult attributePermission = permissionCheckingService
						.checkAttributeDescriptorPermission(attributeDescriptor, principalModel, permissionName);

				if (attributePermission == null)
				{
					granted = false;
				}
				else
				{
					granted = attributePermission.isGranted();
				}
			}
			else
			{
				granted = true;
			}
		}

		return new Permission(inherited, !granted, permissionName, principalId, typeCode, field);
	}


	@Override
	public PermissionInfo getFieldPermissionInfo(final String principal, final String typeCode, final String field)
	{
		final Map<String, Permission> permissionMap = new HashMap<>();
		permissionMap.put(PermissionsConstants.READ, getFieldPermission(principal, typeCode, field, PermissionsConstants.READ));
		permissionMap.put(PermissionsConstants.CHANGE, getFieldPermission(principal, typeCode, field, PermissionsConstants.CHANGE));


		final PermissionInfo result = new DefaultPermissionInfo(PermissionInfo.PermissionInfoType.FIELD, principal, typeCode, field,
				permissionMap);
		result.setLabel(getLocalizedAttributeName(typeCode, field));

		return result;
	}



	protected String getLocalizedAttributeName(final String typeCode, final String field)
	{
		String ret;
		try
		{
			final ComposedTypeModel type = typeService.getComposedTypeForCode(typeCode);
			final AttributeDescriptorModel attribute = typeService.getAttributeDescriptor(type, field);
			ret = attribute.getName();
		}
		catch (UnknownIdentifierException e)
		{
			String qualifiedAttributeName = String.format("%s.%s", typeCode, field);
			ret = labelService.getObjectLabel(qualifiedAttributeName);
			if (StringUtils.equals(ret, qualifiedAttributeName))
			{
				ret = String.format("[ %s ]", StringUtils.capitalize(field));
			}
		}
		return ret;
	}

	@Override
	public Collection<PermissionInfo> getPrincipalsWithPermissionAssignment(final String typeCode)
	{
		final List<PermissionInfo> allUserpermissionsforType = new ArrayList<>();
		final ComposedTypeModel type = typeService.getComposedTypeForCode(typeCode);
		final Collection<PermissionAssignment> typePermissions = permissionManagementService.getTypePermissions(type);
		final List<PermissionAssignment> attrPermForType = getAttributePermissionsForType(type);

		if (CollectionUtils.isNotEmpty(typePermissions) || CollectionUtils.isNotEmpty(attrPermForType))
		{
			final Map<PrincipalModel, List<PermissionAssignment>> tmpMap = new HashMap<>();

			for (final PermissionAssignment permissionAssignment : typePermissions)
			{
				final PrincipalModel principal = permissionAssignment.getPrincipal();
				List<PermissionAssignment> list = tmpMap.get(principal);
				if (list == null)
				{
					list = new ArrayList<>();
					tmpMap.put(principal, list);
				}
				list.add(permissionAssignment);
			}

			for (final Entry<PrincipalModel, List<PermissionAssignment>> permissionEntry : tmpMap.entrySet())
			{
				final PrincipalModel principal = permissionEntry.getKey();
				final PermissionInfo typePermission = getPrincipalPermissionInfo(principal.getUid(), type.getCode());
				allUserpermissionsforType.add(typePermission);
			}
		}

		return allUserpermissionsforType;
	}

	@Override
	public Collection<PermissionInfo> getTypePermissionInfosForPrincipal(final String principalId)
	{
		final List<UserRightModel> rightModels = getAllCrudUserRights();

		final Set<ComposedTypeModel> typesWithPermissions = new HashSet<>();

		final List<PrincipalModel> principals = new ArrayList<>();

		final PrincipalModel principalModel;

		if (userService.isUserExisting(principalId))
		{
			principalModel = userService.getUserForUID(principalId);
			if (principalModel instanceof UserModel)
			{
				final Set<UserGroupModel> allUserGroupsForUser = userService.getAllUserGroupsForUser((UserModel) principalModel);
				if (allUserGroupsForUser != null)
				{
					principals.addAll(allUserGroupsForUser);
				}
			}
		}
		else
		{
			principalModel = userService.getUserGroupForUID(principalId);
		}

		principals.add(principalModel);

		for (final PrincipalModel principal : principals)
		{
			final Principal jaloPrincipal = modelService.getSource(principal);

			final List allSources = modelService.getAllSources(rightModels, new ArrayList<>());
			final Set set = jaloPrincipal.getItemPermissionsMap(allSources).keySet();

			typesWithPermissions.addAll(modelService.getAll(set, new HashSet<ComposedTypeModel>()));
		}

		final List<PermissionInfo> result = new ArrayList<>();

		for (final Object typeWithPermission : typesWithPermissions)
		{
			if (typeWithPermission instanceof ComposedTypeModel)
			{
				result.add(getTypePermissionInfo(principalId, ((ComposedTypeModel) typeWithPermission).getCode()));
			}
		}

		return result;
	}

	@Override
	public void setPermission(final Permission permission)
	{
		final String field = permission.getField();
		final String type = permission.getType();
		final String principal = permission.getPrincipal();

		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(type);

		if (StringUtils.isBlank(field))
		{
			final PermissionInfo typePermission = getTypePermissionInfo(principal, type);
			final List<Permission> permissions = typePermission.getPermissions();

			final List<PermissionAssignment> currentPermissionState = getCurrentPermissionState(permission, permissions);
			permissionManagementService.addTypePermission(composedType,
					currentPermissionState.toArray(new PermissionAssignment[currentPermissionState.size()]));
		}
		else
		{
			final PermissionInfo fieldPermission = getFieldPermissionInfo(principal, type, field);
			final List<Permission> permissions = fieldPermission.getPermissions();

			final Set<AttributeDescriptorModel> attributeDescriptorsForType = typeService
					.getAttributeDescriptorsForType(composedType);
			final AttributeDescriptorModel attributeDescriptor = getAttributeDescriptor(attributeDescriptorsForType, field);

			final List<PermissionAssignment> currentPermissionState = getCurrentPermissionState(permission, permissions);
			if (attributeDescriptor == null)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format("%s '%s'. %s", "Cannot change permission for attribute ", field,
							"Attribute is not declared on the service layer!"));
				}
			}
			else
			{
				permissionManagementService.addAttributePermissions(attributeDescriptor, currentPermissionState);
			}
		}
	}

	/**
	 * * Returns Field Permission Assignment for principal. If no assignment is defined directly for the user, returns
	 * null.
	 *
	 * @param principalModel
	 * @param typeCode
	 * @param field
	 * @param permissionName
	 * @return the permission assignment or null
	 */
	protected PermissionAssignment getFieldPermissionAssignmentForPrincipal(final PrincipalModel principalModel,
			final String typeCode, final String field, final String permissionName)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(typeCode);
		final Set<AttributeDescriptorModel> attributeDescriptorsForType = typeService.getAttributeDescriptorsForType(composedType);
		final AttributeDescriptorModel attributeDescriptor = getAttributeDescriptor(attributeDescriptorsForType, field);
		if (attributeDescriptor == null)
		{
			return null;
		}
		final Collection<PermissionAssignment> permissionAssignements = permissionManagementService
				.getAttributePermissionsForPrincipal(attributeDescriptor, principalModel);
		if (CollectionUtils.isEmpty(permissionAssignements))
		{
			return null;
		}
		for (final PermissionAssignment existingPermissionAssignement : permissionAssignements)
		{
			if (existingPermissionAssignement.getPermissionName().equals(permissionName))
			{
				return existingPermissionAssignement;
			}
		}
		return null;
	}



	@Override
	public void deletePermission(final Permission permission)
	{
		final String field = permission.getField();
		final String type = permission.getType();
		final String name = permission.getName();
		final String principal = permission.getPrincipal();

		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(type);
		final PrincipalModel principalModel = getPrincipalById(principal);

		final Principal principalItem = modelService.getSource(principalModel);

		if (StringUtils.isBlank(field))
		{
			final List<UserRight> allCrudUserRightsJalo = getAllCrudUserRightsJalo();
			final Map itemPermissionsMap = new HashMap(principalItem.getItemPermissionsMap(allCrudUserRightsJalo));
			final Object jaloType = modelService.getSource(composedType);

			final List perm = (List) itemPermissionsMap.get(jaloType);
			resetPermission(userRightsStringsInHMCOrder.indexOf(name), perm);
			principalItem.setItemPermissionsByMap(allCrudUserRightsJalo, itemPermissionsMap);
		}
		else
		{
			final Set<AttributeDescriptorModel> attributeDescriptorsForType = typeService
					.getAttributeDescriptorsForType(composedType);
			final AttributeDescriptorModel attributeDescriptor = getAttributeDescriptor(attributeDescriptorsForType, field);

			removeAttributePermission(attributeDescriptor, principalItem, name);
		}
	}

	private void removeAttributePermission(final AttributeDescriptorModel attributeDescriptor, final Principal principalItem,
			final String name)
	{
		final AttributeDescriptor attributeDescriptorJalo = modelService.getSource(attributeDescriptor);

		final List<UserRight> allFieldUserRightsJalo = getAllFieldUserRightsJalo();
		final Map permissionMap = new HashMap(attributeDescriptorJalo.getPermissionMap(allFieldUserRightsJalo));

		final List perm = (List) permissionMap.get(principalItem);
		resetPermission(fieldRightsStringsInHMCOrder.indexOf(name), perm);

		try
		{
			attributeDescriptorJalo.setPermissionsByMap(allFieldUserRightsJalo, permissionMap);
		}
		catch (final JaloSecurityException e)
		{
			LOG.error("Could not set permission map reason: ", e);
		}
	}

	private static void resetPermission(final int index, final List permissionList)
	{
		if (permissionList != null && permissionList.size() > index)
		{
			permissionList.set(index, null);
		}
	}

	@Override
	public PermissionInfo updatePermissionInfo(final Permission permission)
	{
		if (permission.getField() != null && !permission.getField().isEmpty())
		{
			return this.getFieldPermissionInfo(permission.getPrincipal(), permission.getType(), permission.getField());
		}
		else
		{
			return this.getTypePermissionInfo(permission.getPrincipal(), permission.getType());
		}
	}

	@Override
	public boolean canHandle(final String context)
	{
		return platformFacadeStrategyHandleCache.canHandle(context) || isValidPrincipal(context);
	}

	// -----------------------------------------------------------------------------
	// Helper methods.
	// -----------------------------------------------------------------------------

	protected List<PermissionAssignment> getCurrentPermissionState(final Permission permission, final List<Permission> permissions)
	{
		final List<PermissionAssignment> currentState = new ArrayList<>();

		for (final Permission p : permissions)
		{
			if (p.getName().equals(permission.getName()))
			{
				p.setDenied(permission.isDenied());
				p.setInherited(permission.isInherited());
			}

			if (!p.isInherited())
			{
				currentState.add(new PermissionAssignment(p.getName(), getPrincipalById(p.getPrincipal()), p.isDenied()));
			}
		}

		return currentState;
	}

	protected UserRightModel getUserRightForCode(final String code)
	{
		final Collection<UserRightModel> rightModel = backofficeUserRightsDao.findUserRightsByCode(code);

		if (rightModel.isEmpty() || rightModel.size() > 1)
		{
			return null;
		}

		return rightModel.iterator().next();
	}

	protected AttributeDescriptorModel getAttributeDescriptor(final Set<AttributeDescriptorModel> attributeDescriptorsForType,
			final String field)
	{

		for (final AttributeDescriptorModel attributeDescriptorModel : attributeDescriptorsForType)
		{
			if (attributeDescriptorModel.getQualifier().equals(field))
			{
				return attributeDescriptorModel;
			}
		}

		return null;
	}


	private List<UserRight> getAllCrudUserRightsJalo()
	{
		return modelService.getAllSources(getAllCrudUserRights(), new ArrayList<UserRight>());
	}

	protected List<UserRightModel> getAllCrudUserRights()
	{
		final List<UserRightModel> rightsList = new ArrayList<>();

		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.READ));
		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.CREATE));
		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.REMOVE));
		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.CHANGE));

		return rightsList;
	}

	private List<UserRight> getAllFieldUserRightsJalo()
	{
		return modelService.getAllSources(getAllFieldUserRights(), new ArrayList<UserRight>());
	}

	protected List<UserRightModel> getAllFieldUserRights()
	{
		final List<UserRightModel> rightsList = new ArrayList<>();

		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.READ));
		CollectionUtils.addIgnoreNull(rightsList, getUserRightForCode(PermissionsConstants.CHANGE));

		return rightsList;
	}

	protected List<PermissionAssignment> getAttributePermissionsForType(final ComposedTypeModel type)
	{
		final List<PermissionAssignment> assignments = new ArrayList<>();
		final Set<AttributeDescriptorModel> attributes = typeService.getAttributeDescriptorsForType(type);
		for (final AttributeDescriptorModel attributeDescriptorModel : attributes)
		{
			assignments.addAll(permissionManagementService.getAttributePermissions(attributeDescriptorModel));
		}
		return assignments;
	}

	protected boolean isValidPrincipal(final String principalId)
	{
		try
		{
			final PrincipalModel principalById = getPrincipalById(principalId);
			return principalById != null;
		}
		catch (final ModelNotFoundException mnfe)
		{
			return false;
		}
		catch (final AmbiguousIdentifierException aie)
		{
			return false;
		}
	}

	/**
	 * Returns Type Permission Assignment for principal. If no assignment is defined directly for the user, returns null.
	 *
	 * @param typeCode
	 * @param principalModel
	 * @param permissionName
	 * @return the permission assignment or null
	 */
	protected PermissionAssignment getTypePermissionAssignmentForPrincipal(final String typeCode,
			final PrincipalModel principalModel, final String permissionName)
	{
		final ComposedTypeModel type = typeService.getComposedTypeForCode(typeCode);
		final Collection<PermissionAssignment> assignments = permissionManagementService.getTypePermissionsForPrincipal(type,
				principalModel);

		for (final PermissionAssignment assignment : assignments)
		{
			if (assignment.getPermissionName().equals(permissionName))
			{
				return assignment;
			}
		}
		return null;
	}

	protected PrincipalModel getPrincipalById(final String principalUid)
	{
		final PrincipalModel examplePrincipal = new PrincipalModel();
		examplePrincipal.setUid(principalUid);

		return flexibleSearchService.getModelByExample(examplePrincipal);
	}

	protected Map<String, Permission> getPermissionMap(final String principal, final String type)
	{
		final Map<String, Permission> permissionMap = new HashMap<>();

		permissionMap.put(PermissionsConstants.READ, getTypePermission(principal, type, PermissionsConstants.READ));
		permissionMap.put(PermissionsConstants.CREATE, getTypePermission(principal, type, PermissionsConstants.CREATE));
		permissionMap.put(PermissionsConstants.REMOVE, getTypePermission(principal, type, PermissionsConstants.REMOVE));
		permissionMap.put(PermissionsConstants.CHANGE, getTypePermission(principal, type, PermissionsConstants.CHANGE));

		return permissionMap;
	}

	@Required
	public void setPlatformFacadeStrategyHandleCache(final PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache)
	{
		this.platformFacadeStrategyHandleCache = platformFacadeStrategyHandleCache;
	}

	@Required
	public void setPermissionCheckingService(final PermissionCheckingService permissionCheckingService)
	{
		this.permissionCheckingService = permissionCheckingService;
	}

	@Required
	public void setBackofficeUserRightsDao(final BackofficeUserRightsDao backofficeUserRightsDao)
	{
		this.backofficeUserRightsDao = backofficeUserRightsDao;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setPermissionManagementService(final PermissionManagementService permissionManagementService)
	{
		this.permissionManagementService = permissionManagementService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
