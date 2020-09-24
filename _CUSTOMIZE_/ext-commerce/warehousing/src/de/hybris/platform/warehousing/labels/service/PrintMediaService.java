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
package de.hybris.platform.warehousing.labels.service;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;


/**
 * Interface defining how to generate the labels.
 * Also, provides api to convert the {@link MediaModel} into popup script
 */
public interface PrintMediaService
{

	/**
	 * Generates the {@link MediaModel} for the given {@link de.hybris.platform.ordersplitting.model.ConsignmentModel}
	 *
	 * @param frontendTemplateName
	 * 		the templateName to generate the {@link MediaModel}
	 * @param businessProcessModel
	 * 		the {@link BusinessProcessModel} for which media needs to be generated
	 * @return the generated {@link MediaModel}
	 */
	MediaModel getMediaForTemplate(String frontendTemplateName, BusinessProcessModel businessProcessModel);

	/**
	 * Generates the popup script for the given {@link MediaModel}.
	 *
	 * @param mediaModel
	 * 		the {@link MediaModel} to be rendered as html in popup
	 * @param width
	 * 		the width of the popup window
	 * @param height
	 * 		the height of the popup height
	 * @param blockedPopupMessage
	 * 		the localised error message if popups are not allowed to be open
	 * @return the popup script to open a popup window with the rendered html equivalent of the given media object
	 */
	String generatePopupScriptForMedia(MediaModel mediaModel, String width, String height, String blockedPopupMessage);

	/**
	 * Generates an HTML page for the given {@link MediaModel}
	 *
	 * @param mediaModel
	 * 		the {@link MediaModel} to be rendered as an HTML page
	 * @return the generated HTML page
	 */
	String generateHtmlMediaTemplate(MediaModel mediaModel);
}
