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
package com.hybris.backoffice.workflow.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.util.type.BackofficeTypeUtils;


/**
 * Default implementation which returns supported attachment types based on {@link #attachmentTypeCodes}
 */
public class DefaultWorkflowsTypeFacade implements WorkflowsTypeFacade
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkflowsTypeFacade.class);
	private Set<String> attachmentTypeCodes;
	private TypeFacade typeFacade;
	private TypeService typeService;
	private PermissionFacade permissionFacade;
	private BackofficeTypeUtils backofficeTypeUtils;

	@Override
	public Optional<ComposedTypeModel> findCommonAttachmentType(final List<ItemModel> attachments)
	{
		if (CollectionUtils.isNotEmpty(attachments))
		{
			final String closestSuperType = backofficeTypeUtils.findClosestSuperType(new ArrayList<>(attachments));
			if (StringUtils.isNotEmpty(closestSuperType))
			{
				return getClosestSupportedType(closestSuperType);
			}
		}
		return Optional.empty();
	}

	protected Optional<ComposedTypeModel> getClosestSupportedType(final String attachmentTypeCode)
	{
		final Optional<String> assignableType = getAttachmentTypeCodesForUser().stream()
				.filter(typeCode -> typeService.isAssignableFrom(typeCode, attachmentTypeCode)).findAny();
		try
		{
			return assignableType.map(s -> typeService.getComposedTypeForCode(s));
		}
		catch (final UnknownIdentifierException ex)
		{
			LOG.warn("Type not found {}", attachmentTypeCode);
		}
		return Optional.empty();
	}

	@Override
	public List<String> getSupportedAttachmentClassNames()
	{
		return toClassNames(getAttachmentTypeCodesForUser());
	}


	@Override
	public List<String> getAllAttachmentClassNames()
	{
		return toClassNames(getAttachmentTypeCodes());
	}

	protected List<String> toClassNames(final Set<String> typeCodes)
	{
		final List<String> classNames = new ArrayList<>();
		typeCodes.forEach(typeCode -> {
			try
			{
				final DataType dataType = typeFacade.load(typeCode);
				classNames.add(dataType.getClazz().getName());
			}
			catch (final TypeNotFoundException e)
			{
				LOG.warn(String.format("Cannot load type %s, will not be used as attachment type", typeCode), e);
			}
		});
		return classNames;
	}

	@Override
	public List<String> getSupportedAttachmentTypeCodes()
	{
		return new ArrayList<>(getAttachmentTypeCodesForUser());
	}

	@Override
	public List<ComposedTypeModel> getSupportedAttachmentTypes()
	{
		return getAttachmentTypeCodesForUser().stream().map(typeService::getComposedTypeForCode).collect(Collectors.toList());
	}

	@Required
	public void setAttachmentTypeCodes(final Set<String> attachmentTypeCodes)
	{
		this.attachmentTypeCodes = attachmentTypeCodes;
	}

	protected Set<String> getAttachmentTypeCodesForUser()
	{
		return attachmentTypeCodes.stream().filter(permissionFacade::canReadType).collect(Collectors.toSet());
	}

	public Set<String> getAttachmentTypeCodes()
	{
		return attachmentTypeCodes;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setBackofficeTypeUtils(final BackofficeTypeUtils backofficeTypeUtils)
	{
		this.backofficeTypeUtils = backofficeTypeUtils;
	}

	public BackofficeTypeUtils getBackofficeTypeUtils()
	{
		return backofficeTypeUtils;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}
}
