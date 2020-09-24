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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.core.model.media.MediaModel;

import java.util.Optional;


public class ExcelMediaUrlExportDecorator
{
	public static final String URL = "url";

	public Optional<String> addUrlToMediaExport(final Optional<String> mediaExport, final MediaModel media)
	{
		return mediaExport.map(m -> m + getExternalUrlInQuotes(media));
	}

	public String decorateReferenceFormat(final String referenceFormat)
	{
		return referenceFormat + ":" + URL;
	}

	protected String getExternalUrlInQuotes(final MediaModel media)
	{
		// Checks for the ":" character - i.e. protocol:resource
		final String urlPattern = ".+:.+";

		return ((media.getDownloadURL() != null) && media.getDownloadURL().matches(urlPattern))
				? (":\"" + media.getDownloadURL() + "\"")
				: ":";
	}

}
