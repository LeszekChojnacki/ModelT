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
package de.hybris.platform.warehousingbackoffice.labels.strategy.impl;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.warehousing.labels.service.PrintMediaService;
import de.hybris.platform.warehousing.process.impl.DefaultConsignmentProcessService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import com.hybris.cockpitng.labels.LabelUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;

/**
 * Prints a packing slip for a {@link ConsignmentModel}.
 */
public class ConsignmentPrintPackSlipStrategy implements ConsignmentPrintDocumentStrategy
{
	protected static final String FRONTEND_TEMPLATENAME = "PackLabelDocumentTemplate";
	protected static final String POPUP_WIDTH = "950";
	protected static final String POPUP_HEIGHT = "800";
	protected static final String BLOCKED_POPUP_MESSAGE = "blockedpopupmessage";

	private PrintMediaService printMediaService;
	private DefaultConsignmentProcessService consignmentBusinessProcessService;

	@Override
	public void printDocument(final ConsignmentModel consignmentModel)
	{
		ServicesUtil.validateParameterNotNull(consignmentModel, "Consignment cannot be null");

		final MediaModel pickListMedia = getPrintMediaService().getMediaForTemplate(FRONTEND_TEMPLATENAME,
				getConsignmentBusinessProcessService().getConsignmentProcess(consignmentModel));

		final String popup = getPrintMediaService()
				.generatePopupScriptForMedia(pickListMedia, POPUP_WIDTH, POPUP_HEIGHT, resolveLabel(BLOCKED_POPUP_MESSAGE));
		Clients.evalJavaScript(popup);
	}


	/**
	 * Gets the localized label for the given key.
	 *
	 * @param labelKey
	 * 		the key for which the label is required
	 * @return the localized label
	 */
	protected String resolveLabel(final String labelKey)
	{
		final String defaultValue = LabelUtils.getFallbackLabel(labelKey);
		return Labels.getLabel(labelKey, defaultValue);
	}

	protected PrintMediaService getPrintMediaService()
	{
		return printMediaService;
	}

	@Required
	public void setPrintMediaService(final PrintMediaService printMediaService)
	{
		this.printMediaService = printMediaService;
	}

	protected DefaultConsignmentProcessService getConsignmentBusinessProcessService()
	{
		return consignmentBusinessProcessService;
	}

	@Required
	public void setConsignmentBusinessProcessService(final DefaultConsignmentProcessService consignmentBusinessProcessService)
	{
		this.consignmentBusinessProcessService = consignmentBusinessProcessService;
	}
}
