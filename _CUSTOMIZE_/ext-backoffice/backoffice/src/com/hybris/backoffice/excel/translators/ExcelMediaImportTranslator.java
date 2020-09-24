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

import java.util.Map;
import java.util.Optional;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Excel translator which allows to import single media reference. For more information
 * {@link AbstractExcelMediaImportTranslator}
 */
public class ExcelMediaImportTranslator extends AbstractExcelMediaImportTranslator<MediaModel>
{

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return getTypeService().isAssignableFrom(MediaModel._TYPECODE, attributeDescriptor.getAttributeType().getCode());
	}

	@Override
	public Optional<Object> exportData(final MediaModel objectToExport)
	{
		return exportMedia(objectToExport).map(Object.class::cast);
	}

	@Override
	public Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final Impex impex = new Impex();
		if (hasImportData(importParameters.getSingleValueParameters()))
		{
			final ImpexForType mediaImpex = impex.findUpdates(MediaModel._TYPECODE);

			final Map<String, String> params = importParameters.getSingleValueParameters();
			final String mediaRefId = generateMediaRefId(attributeDescriptor, params);
			mediaImpex.addRow(createMediaRow(attributeDescriptor, mediaRefId, params));

			final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
			addReferencedMedia(impexForType, attributeDescriptor, mediaRefId);
		}

		return impex;
	}
}
