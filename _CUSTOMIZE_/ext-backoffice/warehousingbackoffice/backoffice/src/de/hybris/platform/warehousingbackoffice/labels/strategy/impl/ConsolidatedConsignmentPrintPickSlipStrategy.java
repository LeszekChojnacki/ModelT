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
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.warehousing.labels.service.PrintMediaService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsolidatedConsignmentPrintDocumentStrategy;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import com.hybris.cockpitng.labels.LabelUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static de.hybris.platform.warehousing.constants.WarehousingConstants.CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME;


/**
 * Prints a Picking Slip for a list of {@link ConsignmentModel}.
 */
public class ConsolidatedConsignmentPrintPickSlipStrategy implements ConsolidatedConsignmentPrintDocumentStrategy
{
	protected static final String FRONTEND_TEMPLATENAME = "ConsolidatedPickLabelDocumentTemplate";
	protected static final String POPUP_WIDTH = "900";
	protected static final String POPUP_HEIGHT = "800";
	protected static final String BLOCKED_POPUP_MESSAGE = "blockedpopupmessage";

	@Resource
	protected PrintMediaService printMediaService;

	@Override
	public void printDocument(final List<ConsignmentModel> consignmentModels)
	{
		validateParameterNotNullStandardMessage("consignmentModels", consignmentModels);

		final MediaModel pickListMedia = printMediaService
				.getMediaForTemplate(FRONTEND_TEMPLATENAME, generateBusinessProcess(consignmentModels));

		final String popup = printMediaService
				.generatePopupScriptForMedia(pickListMedia, POPUP_WIDTH, POPUP_HEIGHT, resolveLabel(BLOCKED_POPUP_MESSAGE));
		Clients.evalJavaScript(popup);
	}

	/***
	 * Generates a {@link BusinessProcessModel} and use the {@link BusinessProcessParameterModel} as a container
	 * for a list of {@link ConsignmentModel}
	 * @param consignmentModels list of {@link ConsignmentModel}
	 * @return a business process containing consignments
	 */
	protected BusinessProcessModel generateBusinessProcess(final List<ConsignmentModel> consignmentModels)
	{
		final BusinessProcessModel businessProcessModel = new BusinessProcessModel();
		final BusinessProcessParameterModel businessProcessParameterModel = new BusinessProcessParameterModel();

		businessProcessParameterModel.setName(CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME);
		businessProcessParameterModel.setValue(consignmentModels);
		businessProcessModel.setContextParameters(Arrays.asList(businessProcessParameterModel));

		return businessProcessModel;
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
}
