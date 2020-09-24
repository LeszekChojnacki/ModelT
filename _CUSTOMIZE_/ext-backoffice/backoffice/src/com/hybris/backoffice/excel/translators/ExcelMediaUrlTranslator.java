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
import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Optional;


public class ExcelMediaUrlTranslator extends ExcelMediaImportTranslator
{
	private final ExcelMediaUrlExportDecorator urlExportDecorator = new ExcelMediaUrlExportDecorator();

	@Override
	public Optional<Object> exportData(final MediaModel media)
	{
		return urlExportDecorator.addUrlToMediaExport(exportMedia(media), media).map(Object.class::cast);
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return urlExportDecorator.decorateReferenceFormat(super.referenceFormat(attributeDescriptor));
	}

}
