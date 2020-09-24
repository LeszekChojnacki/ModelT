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
package de.hybris.platform.permissionsfacades.impl;

import static com.google.common.base.Preconditions.checkArgument;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.synchronization.CatalogSynchronizationService;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.permissionsfacades.PermissionsFacade;
import de.hybris.platform.permissionsfacades.data.CatalogPermissionsData;
import de.hybris.platform.permissionsfacades.data.PermissionsData;
import de.hybris.platform.permissionsfacades.data.SyncPermissionsData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCheckResult;
import de.hybris.platform.servicelayer.security.permissions.PermissionCheckingService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link PermissionsFacade}.
 */
public class DefaultPermissionsFacade implements PermissionsFacade
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultPermissionsFacade.class);
	private static final String PRINCIPAL_PARAM_ERROR_MESSAGE = "principalUid parameter cannot be null";
	private static final String PERMISSIONS_NAMES_PARAM_ERROR_MESSAGE = "permissionNames parameter cannot be null";

	private PermissionCheckingService permissionCheckingService;
	private FlexibleSearchService flexibleSearchService;
	private TypeService typeService;
	private CatalogVersionService catalogVersionService;
	private SessionService sessionService;
	private SearchRestrictionService searchRestrictionService;
	private CatalogSynchronizationService catalogSynchronizationService;

	private final Pattern attributePattern = Pattern
			.compile("([^\\.]+)\\.([^\\.]+)"); //Attribute should have format <typeName>.<attributeName>

	@Override
	public List<PermissionsData> calculateTypesPermissions(final String principalUid, final List<String> types,
			final List<String> permissionNames)
	{
		checkArgument(principalUid != null, PRINCIPAL_PARAM_ERROR_MESSAGE);
		checkArgument(types != null, "types parameter cannot be null");
		checkArgument(permissionNames != null, PERMISSIONS_NAMES_PARAM_ERROR_MESSAGE);

		final PrincipalModel principal = findPrincipal(principalUid);
		final List principalPermissionsList = new ArrayList<PermissionsData>();

		for (final String type : types)
		{
			final PermissionsData permissionsData = new PermissionsData();
			principalPermissionsList.add(permissionsData);
			permissionsData.setId(type);
			permissionsData.setPermissions(new HashMap<String, String>());

			for (final String permissionName : permissionNames)
			{
				final PermissionCheckResult permissionCheckResult = permissionCheckingService.checkTypePermission(type, principal,
						permissionName);
				permissionsData.getPermissions().put(permissionName,
						permissionCheckResult.isGranted() ? Boolean.toString(true) : Boolean.toString(false));
			}
		}

		return principalPermissionsList;
	}

	@Override
	public List<PermissionsData> calculateAttributesPermissions(final String principalUid, final List<String> typeAttributes,
			final List<String> permissionNames)
	{
		checkArgument(principalUid != null, PRINCIPAL_PARAM_ERROR_MESSAGE);
		checkArgument(typeAttributes != null, "typeAttributes parameter cannot be null");
		checkArgument(permissionNames != null, PERMISSIONS_NAMES_PARAM_ERROR_MESSAGE);

		final List<PermissionsData> principalPermissionsList = new ArrayList<PermissionsData>();
		final PrincipalModel principal = findPrincipal(principalUid);
		Matcher matcher;

		for (final String typeAttribute : typeAttributes)
		{
			matcher = getAttributeNameMatcher(typeAttribute);
			final String type = matcher.group(1);
			final String attribute = matcher.group(2);

			for (final String attributeQualifier : findAllAttributesForType(type, attribute))
			{
				final PermissionsData permissionsData = retrieveSingleAttributePermissions(permissionNames, type, principal,
						attributeQualifier);
				principalPermissionsList.add(permissionsData);
			}
		}

		return principalPermissionsList;
	}


	@Override
	public PermissionsData calculateGlobalPermissions(final String principalUid, final List<String> permissionNames)
	{
		checkArgument(principalUid != null, PRINCIPAL_PARAM_ERROR_MESSAGE);
		checkArgument(permissionNames != null, PERMISSIONS_NAMES_PARAM_ERROR_MESSAGE);

		final PrincipalModel principal = findPrincipal(principalUid);
		final PermissionsData permissionsData = new PermissionsData();
		permissionsData.setId("global");
		permissionsData.setPermissions(new HashMap<String, String>());

		for (final String permissionName : permissionNames)
		{
			final PermissionCheckResult permissionCheckResult = permissionCheckingService.checkGlobalPermission(principal,
					permissionName);
			permissionsData.getPermissions().put(permissionName,
					permissionCheckResult.isGranted() ? Boolean.toString(true) : Boolean.toString(false));
		}

		return permissionsData;
	}


	protected PermissionsData retrieveSingleAttributePermissions(final List<String> permissionNames, final String type,
			final PrincipalModel principal, final String attributeQualifier)
	{
		final PermissionsData permissionsData = new PermissionsData();
		permissionsData.setId(type + "." + attributeQualifier);
		permissionsData.setPermissions(new HashMap<String, String>());

		for (final String permissionName : permissionNames)
		{
			final PermissionCheckResult permissionCheckResult = permissionCheckingService.checkAttributeDescriptorPermission(type,
					attributeQualifier, principal, permissionName);
			permissionsData.getPermissions().put(permissionName,
					permissionCheckResult.isGranted() ? Boolean.toString(true) : Boolean.toString(false));
		}
		return permissionsData;
	}

	protected Matcher getAttributeNameMatcher(final String typeAttribute)
	{
		if (StringUtils.isNotBlank(typeAttribute))
		{
			final Matcher matcher = attributePattern.matcher(typeAttribute);
			if (matcher.matches())
			{
				return matcher;
			}
		}
		throw new UnknownIdentifierException("Attribute doesn't exist :" + typeAttribute);
	}

	protected List<String> findAllAttributesForType(final String type, final String attribute)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(type);
		final List<AttributeDescriptorModel> attributes = new ArrayList<AttributeDescriptorModel>();
		attributes.addAll(composedType.getInheritedattributedescriptors());
		attributes.addAll(composedType.getDeclaredattributedescriptors());

		final List<String> allAttributeQualifiers = new ArrayList<String>();
		for (final AttributeDescriptorModel attributeDescriptor : attributes)
		{
			allAttributeQualifiers.add(attributeDescriptor.getQualifier());
		}

		if (StringUtils.equals("*", attribute))
		{
			return allAttributeQualifiers;
		}
		else if (allAttributeQualifiers.contains(attribute))
		{
			return Arrays.asList(attribute);
		}

		throw new UnknownIdentifierException("Attribute doesn't exist : " + type + "." + attribute);
	}

	@Override
	public List<CatalogPermissionsData> calculateCatalogPermissions(final String principalUid, final List<String> catalogIds,
			final List<String> catalogVersions)
	{
		checkArgument(principalUid != null, PRINCIPAL_PARAM_ERROR_MESSAGE);
		checkArgument(catalogIds != null, "catalogIds parameter cannot be null");
		checkArgument(catalogVersions != null, "catalogVersions parameter cannot be null");

		final PrincipalModel principal = findPrincipal(principalUid);
		final List<CatalogVersionModel> allCvs = executeWithAllCatalogs(
				() -> getFilteredCatalogVersions(catalogIds, catalogVersions));


		//retrieving catalog version permissions for user
		final Collection<CatalogVersionModel> writableCvs = getCatalogVersionService().getAllWritableCatalogVersions(principal);
		final Collection<CatalogVersionModel> readableCvs = getCatalogVersionService().getAllReadableCatalogVersions(principal);

		//generate DTO
		return allCvs.stream()
				.map(cv -> generateCatalogPermissionsDTO(cv, readableCvs.contains(cv), writableCvs.contains(cv), principal,
						getSyncPermissions(cv, principal)))
				.collect(Collectors.toList());

	}

	protected CatalogPermissionsData generateCatalogPermissionsDTO(final CatalogVersionModel cv, final boolean readPermission,
			final boolean writePermission, final PrincipalModel principal, final List<SyncPermissionsData> syncPermissions)
	{
		final CatalogPermissionsData permissions = new CatalogPermissionsData();
		permissions.setCatalogId(cv.getCatalog().getId());
		permissions.setCatalogVersion(cv.getVersion());
		permissions.setPermissions(new HashMap<String, String>());
		permissions.getPermissions().put(READ_ACCESS_TYPE, Boolean.toString(readPermission));
		permissions.getPermissions().put(WRITE_ACCESS_TYPE, Boolean.toString(writePermission));
		permissions.setSyncPermissions(syncPermissions);
		return permissions;
	}

	/**
	 * Gets sync permissions for a given catalog version and a given principal
	 *
	 * @param catalogVersion
	 * 		the catalog version
	 * @param principal
	 * 		the principal
	 * @return the list of sync permissions data
	 */
	protected List<SyncPermissionsData> getSyncPermissions(final CatalogVersionModel catalogVersion,
			final PrincipalModel principal)
	{
		return catalogVersion.getSynchronizations().stream().map(syncJob -> buildSyncPermissionData(syncJob, principal))
				.collect(Collectors.toList());
	}

	/**
	 * Creates the sync permission data for a given sync job and a principal
	 *
	 * @param syncJob
	 * 		the sync job
	 * @param principal
	 * 		the principal
	 * @return the sync permission data
	 */
	protected SyncPermissionsData buildSyncPermissionData(final SyncItemJobModel syncJob, final PrincipalModel principal)
	{
		final SyncPermissionsData syncPermissionsData = new SyncPermissionsData();
		syncPermissionsData.setCanSynchronize(getCatalogSynchronizationService().canSynchronize(syncJob, principal));
		syncPermissionsData.setTargetCatalogVersion(syncJob.getTargetVersion().getVersion());
		return syncPermissionsData;
	}

	protected List<CatalogVersionModel> getFilteredCatalogVersions(final List<String> catalogIds,
			final List<String> catalogVersions)
	{
		final List<CatalogVersionModel> result = new ArrayList<>();
		catalogIds.forEach(cId -> catalogVersions.forEach(cv -> {
			try
			{
				final CatalogVersionModel catalogVersionModel = catalogVersionService.getCatalogVersion(cId, cv);
				result.add(catalogVersionModel);
			}
			catch (final UnknownIdentifierException e)
			{
				LOG.debug("Catalog version '{}' for catalog '{}' doesn't exist", cv, cId);
			}
		}));

		return result;
	}

	protected PrincipalModel findPrincipal(final String principalUid)
	{
		final PrincipalModel example = new PrincipalModel();
		example.setUid(principalUid);
		return flexibleSearchService.getModelByExample(example);
	}

	protected <T> T executeWithAllCatalogs(final Supplier<T> action)
	{
		return executeInLocalView(() -> {
			setAllCatalogs();
			return action.get();
		});
	}

	protected <T> T executeInLocalView(final Supplier<T> action)
	{
		return sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				return action.get();
			}
		});
	}

	protected void setAllCatalogs()
	{
		try
		{
			searchRestrictionService.disableSearchRestrictions();
			catalogVersionService.setSessionCatalogVersions(catalogVersionService.getAllCatalogVersions());
		}
		finally
		{
			searchRestrictionService.enableSearchRestrictions();
		}
	}

	protected PermissionCheckingService getPermissionCheckingService()
	{
		return permissionCheckingService;
	}

	@Required
	public void setPermissionCheckingService(final PermissionCheckingService permissionCheckingService)
	{
		this.permissionCheckingService = permissionCheckingService;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected SearchRestrictionService getSearchRestrictionService()
	{
		return searchRestrictionService;
	}

	@Required
	public void setSearchRestrictionService(final SearchRestrictionService searchRestrictionService)
	{
		this.searchRestrictionService = searchRestrictionService;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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

	protected CatalogSynchronizationService getCatalogSynchronizationService()
	{
		return catalogSynchronizationService;
	}

	@Required
	public void setCatalogSynchronizationService(
			final CatalogSynchronizationService catalogSynchronizationService)
	{
		this.catalogSynchronizationService = catalogSynchronizationService;
	}
}
