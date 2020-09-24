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
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.labels.service.PrintMediaService;
import de.hybris.platform.warehousing.process.impl.DefaultConsignmentProcessService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import com.hybris.cockpitng.labels.LabelUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Prints a {@link ConsignmentModel#RETURNFORM} for a {@link ConsignmentModel}.
 */
public class ConsignmentPrintReturnFormStrategy implements ConsignmentPrintDocumentStrategy
{
	protected static final String FRONTEND_TEMPLATENAME = "ReturnFormDocumentTemplate";
	protected static final String POPUP_WIDTH = "700";
	protected static final String POPUP_HEIGHT = "700";
	protected static final String BLOCKED_POPUP_MESSAGE = "blockedpopupmessage";

	private PrintMediaService printMediaService;
	private DefaultConsignmentProcessService consignmentBusinessProcessService;
	private ModelService modelService;

	@Override
	public void printDocument(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);

		MediaModel returnFormMedia = consignment.getReturnForm();
		if (returnFormMedia == null)
		{
			returnFormMedia = getPrintMediaService().getMediaForTemplate(FRONTEND_TEMPLATENAME,
					getConsignmentBusinessProcessService().getConsignmentProcess(consignment));
			consignment.setReturnForm(returnFormMedia);
			getModelService().save(consignment);
		}

		final String popup = getPrintMediaService()
				.generatePopupScriptForMedia(returnFormMedia, POPUP_WIDTH, POPUP_HEIGHT, resolveLabel(BLOCKED_POPUP_MESSAGE));
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
