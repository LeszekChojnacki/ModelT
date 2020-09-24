/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.labels.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commerceservices.impersonation.ImpersonationContext;
import de.hybris.platform.commerceservices.impersonation.ImpersonationService;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ordersplitting.model.*;
import de.hybris.platform.servicelayer.media.MediaService;

import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Context specific to ShippingLabel
 */
public class ShippingLabelContext extends CommonPrintLabelContext
{
	private MediaService mediaService;
	private ImpersonationService impersonationService;
	private String barcodeMediaImageName;

	@Override
	public void init(final ConsignmentProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel, documentPageModel);
	}

	/**
	 * Extracts the image url for the barcode of the label
	 *
	 * @param consignmentModel
	 * 		the consignment model for which we request the Pick Slip label
	 * @return the source url for the barcode image of the label (expecting the relative path as a string)
	 */
	public String getBarcodeMediaImageURL(final ConsignmentModel consignmentModel)
	{
		validateParameterNotNull(consignmentModel, "Consignment cannot be null");
		validateParameterNotNull(consignmentModel.getOrder(),
				String.format("Order cannot be null for the Consignment: [%s]", consignmentModel.getCode()));
		validateParameterNotNull(consignmentModel.getOrder().getSite(),
				String.format("Site cannot be null for the Order: [%s]", consignmentModel.getOrder().getCode()));

		final CMSSiteModel cmsSiteModel = (CMSSiteModel) consignmentModel.getOrder().getSite();

		final ImpersonationContext context = new ImpersonationContext();
		context.setSite(cmsSiteModel);
		context.setUser(consignmentModel.getOrder().getUser());
		context.setCatalogVersions(Collections.emptyList());
		final MediaModel barcodeImageMedia = getImpersonationService().executeInContext(context, () ->
		{
			final List<ContentCatalogModel> contentCatalogs = cmsSiteModel.getContentCatalogs();
			if (!contentCatalogs.isEmpty())
			{
				return getMediaService().getMedia(contentCatalogs.get(0).getActiveCatalogVersion(), getBarcodeMediaImageName());
			}
			return null;
		});

		String path = null;
		if (barcodeImageMedia != null)
		{
			path = barcodeImageMedia.getDownloadURL();
		}
		return path;
	}

	protected String getBarcodeMediaImageName()
	{
		return barcodeMediaImageName;
	}

	@Required
	public void setBarcodeMediaImageName(final String barcodeMediaImageName)
	{
		this.barcodeMediaImageName = barcodeMediaImageName;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	protected ImpersonationService getImpersonationService()
	{
		return impersonationService;
	}

	@Required
	public void setImpersonationService(final ImpersonationService impersonationService)
	{
		this.impersonationService = impersonationService;
	}
}
