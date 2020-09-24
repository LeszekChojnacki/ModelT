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

import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewAttributeDescriptorModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.internal.jalo.ServicelayerManager;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hybris.backoffice.cockpitng.dataaccess.facades.common.PlatformFacadeStrategyHandleCache;
import com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.InstancePermissionAdvisor;
import com.hybris.cockpitng.core.util.Resettable;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;


/**
 * Default implementation which delegates the permission checks to the {@link PermissionCRUDService}.
 */
public class DefaultPlatformPermissionFacadeStrategy implements PermissionFacadeStrategy, Resettable
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultPlatformPermissionFacadeStrategy.class);
	private final Map<String, Boolean> permissionAwareTypeCache = Maps.newHashMap();
	private PermissionCRUDService permissionCRUDService;
	private ReadPermissionCache readPermissionCache;
	private TypeFacade typeFacade;
	private CatalogVersionService catalogVersionService;
	private CatalogTypeService catalogTypeService;
	private UserService userService;
	private PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache;
	private CommonI18NService commonI18NService;
	private ModelService modelService;
	private TypeService typeService;
	private List<InstancePermissionAdvisor> permissionAdvisors;

	private static final String COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_MESSAGE = "Could not find attribute descriptor for given: ";
	private static final String COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_FORMAT = "%s.%s";

	@Override
	public boolean canReadType(final String typeCode)
	{
		return !isPermissionAwareType(typeCode) || getReadPermissionCache().canReadType(typeCode);
	}

	private boolean isPermissionAwareType(final String typeCode)
	{
		Boolean result = permissionAwareTypeCache.get(typeCode);
		if (result == null)
		{
			try
			{
				result = Boolean.valueOf(typeService.getTypeForCode(typeCode) instanceof ComposedTypeModel);
			}
			catch (final UnknownIdentifierException uie)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Could not load type: " + typeCode, uie);
				}
				result = Boolean.FALSE;
			}
			permissionAwareTypeCache.put(typeCode, result);
		}
		return result.booleanValue();
	}

	@Override
	public boolean canChangeType(final String typeCode)
	{
		return !isPermissionAwareType(typeCode) || permissionCRUDService.canChangeType(typeCode);
	}

	@Override
	public boolean canReadInstanceProperty(final Object instance, final String property)
	{
		final String type = typeFacade.getType(instance);
		return canReadProperty(type, property) && canReadCatalogVersionAwareInstance(instance);
	}

	@Override
	public boolean canReadProperty(final String typeCode, final String property)
	{
		if (!isPermissionAwareType(typeCode))
		{
			return true;
		}
		boolean result = canReadType(typeCode);
		if (result)
		{
			result = getReadPermissionCache().canReadAttribute(typeCode, property) || isViewParamAttribute(typeCode, property);
		}
		return result;
	}

	private boolean isViewParamAttribute(final String typeCode, final String property)
	{
		if (StringUtils.isBlank(property))
		{
			return false;
		}

		final TypeModel typeForCode = typeService.getTypeForCode(typeCode);
		if (typeForCode instanceof ViewTypeModel)
		{
			for (final ViewAttributeDescriptorModel descriptorModel : ((ViewTypeModel) typeForCode).getParams())
			{

				if (property.equals(descriptorModel.getQualifier()))
				{
					return true;
				}

			}
		}

		return false;
	}

	@Override
	public boolean canChangeInstanceProperty(final Object instance, final String property)
	{
		if (canChangeInstance(instance))
		{
			final String type = typeFacade.getType(instance);
			return canChangeProperty(type, property) && canWriteCatalogVersionAwareInstance(instance);
		}
		return false;
	}

	@Override
	public boolean canChangeInstancesProperty(final Collection<Object> instances, final String property)
	{
		final Set<Object> set = new LinkedHashSet<>(CollectionUtils.emptyIfNull(instances));

		return !set.stream().anyMatch(instance -> !canChangeInstanceProperty(instance, property));
	}

	@Override
	public boolean canChangeProperty(final String typeCode, final String property)
	{
		if (!isPermissionAwareType(typeCode))
		{
			return true;
		}
		boolean result = canChangeType(typeCode);
		if (result)
		{
			try
			{
				result = permissionCRUDService.canChangeAttribute(typeCode, property);
			}
			catch (final UnknownIdentifierException uie)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_MESSAGE
							+ String.format(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_FORMAT, typeCode, property), uie);
				}
				result = true;
			}
		}
		return result;

	}

	@Override
	public boolean canChangeInstance(final Object instance)
	{
		if (instance == null)
		{
			return false;
		}

		final String type = typeFacade.getType(instance);
		if (canChangeType(type) && canWriteCatalogVersionAwareInstance(instance))
		{
			for (final InstancePermissionAdvisor advisor : getPermissionAdvisors())
			{
				if (advisor.isApplicableTo(instance) && !advisor.canModify(instance))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canChangeInstances(final Collection<Object> instances)
	{
		final Set<Object> uniqueInstances = new LinkedHashSet<>(CollectionUtils.emptyIfNull(instances));

		return uniqueInstances.stream().allMatch(this::canChangeInstance);
	}

	@Override
	public boolean canCreateTypeInstance(final String typeCode)
	{
		if (!isPermissionAwareType(typeCode))
		{
			return true;
		}
		return permissionCRUDService.canCreateTypeInstance(typeCode);
	}

	@Override
	public boolean canReadInstance(final Object instance)
	{
		final String type = typeFacade.getType(instance);
		return canReadType(type) && canReadCatalogVersionAwareInstance(instance);
	}

	@Override
	public boolean canRemoveInstance(final Object instance)
	{
		if (instance == null)
		{
			return false;
		}

		final String type = typeFacade.getType(instance);
		if (canRemoveTypeInstance(type) && canWriteCatalogVersionAwareInstance(instance))
		{
			for (final InstancePermissionAdvisor advisor : getPermissionAdvisors())
			{
				if (advisor.isApplicableTo(instance) && !advisor.canDelete(instance))
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean canRemoveTypeInstance(final String typeCode)
	{
		if (!isPermissionAwareType(typeCode))
		{
			return true;
		}
		return permissionCRUDService.canRemoveTypeInstance(typeCode);
	}

	@Override
	public boolean canChangeTypePermission(final String typeCode)
	{
		return isPermissionAwareType(typeCode) && permissionCRUDService.canChangeTypePermission(typeCode);
	}

	@Override
	public boolean canChangePropertyPermission(final String typeCode, final String property)
	{
		boolean result = isPermissionAwareType(typeCode);
		if (result)
		{
			try
			{
				result = permissionCRUDService.canChangeAttributePermission(typeCode, property);
			}
			catch (final UnknownIdentifierException uie)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_MESSAGE
							+ String.format(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_FORMAT, typeCode, property), uie);
				}
				result = false;
			}
		}
		return result;
	}

	@Override
	public Set<Locale> getAllWritableLocalesForCurrentUser()
	{
		final Set<Locale> ret = Sets.newHashSet();
		final Set<Language> allJaloWritableLanguages = ServicelayerManager.getInstance().getAllWritableLanguages();

		if (allJaloWritableLanguages != null)
		{
			ret.addAll(extractLocales(allJaloWritableLanguages));
		}
		return ret;
	}

	@Override
	public Set<Locale> getAllReadableLocalesForCurrentUser()
	{
		final Set<Locale> ret = Sets.newHashSet();
		final Set<Language> allJaloReadableLanguages = ServicelayerManager.getInstance().getAllReadableLanguages();

		if (allJaloReadableLanguages != null)
		{
			ret.addAll(extractLocales(allJaloReadableLanguages));
		}
		return ret;
	}

	private Set<Locale> extractLocales(final Set<Language> languages)
	{
		final Set<Locale> ret = Sets.newHashSet();
		for (final Language language : languages)
		{
			final LanguageModel languageModel = modelService.get(language);
			if (languageModel != null)
			{
				ret.add(commonI18NService.getLocaleForLanguage(languageModel));
			}
		}
		return ret;
	}

	@Override
	public Set<Locale> getReadableLocalesForInstance(final Object instance)
	{
		final Set<Locale> readableLocales = Sets.newHashSet();
		final CatalogVersionModel catalogVersion = getCatalogVersionIfPresent(instance);

		final Set<Locale> catalogVersionLocales = catalogVersion == null ? Collections.<Locale> emptySet()
				: getLocalesForLanguage(catalogVersion.getLanguages());
		final Set<Locale> allReadableLocales = getAllReadableLocalesForCurrentUser();

		updateLocales(readableLocales, catalogVersionLocales, allReadableLocales);

		return readableLocales;
	}

	private boolean isCurrentUserAdmin()
	{
		final UserModel currentUser = userService.getCurrentUser();
		return userService.isAdmin(currentUser);
	}

	@Override
	public Set<Locale> getWritableLocalesForInstance(final Object instance)
	{
		final Set<Locale> writableLocales = new HashSet<>();
		final CatalogVersionModel catalogVersion = getCatalogVersionIfPresent(instance);

		final Set<Locale> catalogVersionLocales = catalogVersion == null ? Collections.<Locale> emptySet()
				: getLocalesForLanguage(catalogVersion.getLanguages());
		final Set<Locale> allWritableLocales = getAllWritableLocalesForCurrentUser();

		updateLocales(writableLocales, catalogVersionLocales, allWritableLocales);

		return writableLocales;
	}


	private void updateLocales(final Set<Locale> localesToUpdate, final Set<Locale> catalogVersionLocales,
			final Set<Locale> localesToAdd)
	{
		if (CollectionUtils.isNotEmpty(localesToAdd) && CollectionUtils.isNotEmpty(catalogVersionLocales))
		{
			localesToUpdate.addAll(localesToAdd);
			if (!isCurrentUserAdmin())
			{
				localesToUpdate.retainAll(catalogVersionLocales);
			}
		}
		else
		{
			if (CollectionUtils.isNotEmpty(localesToAdd))
			{
				localesToUpdate.addAll(localesToAdd);
			}
			if (CollectionUtils.isNotEmpty(catalogVersionLocales))
			{
				localesToUpdate.addAll(catalogVersionLocales);
			}
		}
	}

	protected Set<Locale> getLocalesForLanguage(final Collection<LanguageModel> languages)
	{
		final Set<Locale> locales = new HashSet<>();

		for (final LanguageModel language : languages)
		{
			locales.add(commonI18NService.getLocaleForLanguage(language));
		}

		return locales;
	}

	protected CatalogVersionModel getCatalogVersionIfPresent(final Object object)
	{
		if (object instanceof ItemModel && catalogTypeService.isCatalogVersionAwareModel((ItemModel) object))
		{
			try
			{
				return catalogTypeService.getCatalogVersionForCatalogVersionAwareModel((ItemModel) object);
			}
			catch (final IllegalStateException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(e.getMessage(), e);
				}
				return null;
			}
		}
		return null;
	}

	protected boolean canWriteCatalogVersionAwareInstance(final Object instance)
	{
		boolean result = true;
		final CatalogVersionModel catalogVersion = getCatalogVersionIfPresent(instance);
		if (catalogVersion != null)
		{
			result = catalogVersionService.canWrite(catalogVersion, userService.getCurrentUser());
		}
		return result;
	}

	protected boolean canReadCatalogVersionAwareInstance(final Object instance)
	{
		boolean result = true;
		final CatalogVersionModel catalogVersion = getCatalogVersionIfPresent(instance);
		if (catalogVersion != null)
		{
			result = catalogVersionService.canRead(catalogVersion, userService.getCurrentUser());
		}
		return result;
	}

	protected PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}

	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setPlatformFacadeStrategyHandleCache(final PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache)
	{
		this.platformFacadeStrategyHandleCache = platformFacadeStrategyHandleCache;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Override
	public boolean canHandle(final String typeCode)
	{
		return platformFacadeStrategyHandleCache.canHandle(typeCode);
	}

	@Override
	public void reset()
	{
		permissionAwareTypeCache.clear();
	}

	public List<InstancePermissionAdvisor> getPermissionAdvisors()
	{
		return permissionAdvisors;
	}

	@Required
	public void setPermissionAdvisors(final List<InstancePermissionAdvisor> permissionAdvisors)
	{
		this.permissionAdvisors = permissionAdvisors;
	}

	public ReadPermissionCache getReadPermissionCache()
	{
		return readPermissionCache;
	}

	@Required
	public void setReadPermissionCache(final ReadPermissionCache defaultReadPermissionCache)
	{
		this.readPermissionCache = defaultReadPermissionCache;
	}
}
