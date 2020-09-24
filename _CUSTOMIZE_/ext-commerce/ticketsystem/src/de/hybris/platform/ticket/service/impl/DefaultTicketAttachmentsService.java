/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.service.impl;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.media.MediaPermissionService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.service.TicketAttachmentsService;
import de.hybris.platform.ticket.service.UnsupportedAttachmentException;

import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation for {@link TicketAttachmentsService}
 */
public class DefaultTicketAttachmentsService implements TicketAttachmentsService
{
	private MediaService mediaService;
	private MediaPermissionService mediaPermissionService;
	private ModelService modelService;
	private CatalogVersionService catalogVersionService;
	private String catalogVersionName;
	private String catalogId;
	private String folderName;
	private String commonCsAgentUserGroup;
	private UserService userService;
	private String allowedUploadedFormats;

	@Override
	public MediaModel createAttachment(final String name, final String contentType, final byte[] data, final UserModel customer)
	{
		checkFileExtension(name);
		final MediaModel mediaModel = new MediaModel();
		mediaModel.setCode(UUID.randomUUID().toString());
		// no idea why catalog version is required
		mediaModel.setCatalogVersion(getCatalogVersionService().getCatalogVersion(getCatalogId(), getCatalogVersionName()));
		mediaModel.setMime(contentType);
		mediaModel.setRealFileName(name);
		mediaModel.setFolder(getMediaService().getFolder(getFolderName()));
		getModelService().save(mediaModel);
		getMediaService().setDataForMedia(mediaModel, data);
		getMediaPermissionService().grantReadPermission(mediaModel,
				getUserService().getUserGroupForUID(getCommonCsAgentUserGroup()));
		getMediaPermissionService().grantReadPermission(mediaModel, customer);
		return mediaModel;
	}

	protected void checkFileExtension(final String name)
	{
		if (!FilenameUtils.isExtension(name.toLowerCase(),
				getAllowedUploadedFormats().replaceAll("\\s", "").toLowerCase().split(",")))
		{
			throw new UnsupportedAttachmentException(
					String.format("File %s has unsupported extension. Only [%s] allowed.", name, getAllowedUploadedFormats()));
		}
	}

	/**
	 * @return the mediaService
	 */
	protected MediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	/**
	 * @return the mediaPermissionService
	 */
	protected MediaPermissionService getMediaPermissionService()
	{
		return mediaPermissionService;
	}

	/**
	 * @param mediaPermissionService
	 *           the mediaPermissionService to set
	 */
	@Required
	public void setMediaPermissionService(final MediaPermissionService mediaPermissionService)
	{
		this.mediaPermissionService = mediaPermissionService;
	}

	/**
	 * @return the catalogVersionService
	 */
	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	/**
	 * @param catalogVersionService
	 *           the catalogVersionService to set
	 */
	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	/**
	 * @return the modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the catalogId
	 */
	protected String getCatalogId()
	{
		return catalogId;
	}

	/**
	 * @param catalogId
	 *           the catalogId to set
	 */
	@Required
	public void setCatalogId(final String catalogId)
	{
		this.catalogId = catalogId;
	}

	/**
	 * @return the catalogVersionName
	 */
	protected String getCatalogVersionName()
	{
		return catalogVersionName;
	}

	/**
	 * @param catalogVersionName
	 *           the catalogVersionName to set
	 */
	@Required
	public void setCatalogVersionName(final String catalogVersionName)
	{
		this.catalogVersionName = catalogVersionName;
	}

	/**
	 * @return the commonCsAgentUserGroup
	 */
	protected String getCommonCsAgentUserGroup()
	{
		return commonCsAgentUserGroup;
	}

	/**
	 * @param commonCsAgentUserGroup
	 *           the commonCsAgentUserGroup to set
	 */
	@Required
	public void setCommonCsAgentUserGroup(final String commonCsAgentUserGroup)
	{
		this.commonCsAgentUserGroup = commonCsAgentUserGroup;
	}

	/**
	 * @return the folderName
	 */
	protected String getFolderName()
	{
		return folderName;
	}

	/**
	 * @param folderName
	 *           the folderName to set
	 */
	@Required
	public void setFolderName(final String folderName)
	{
		this.folderName = folderName;
	}

	/**
	 * @return the userService
	 */
	protected UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected String getAllowedUploadedFormats()
	{
		return allowedUploadedFormats;
	}

	@Required
	public void setAllowedUploadedFormats(final String allowedUploadedFormats)
	{
		this.allowedUploadedFormats = allowedUploadedFormats;
	}
}