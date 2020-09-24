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
import de.hybris.platform.core.model.type.CollectionTypeModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Excel translator which allows to import media collection as reference. For more information
 * {@link AbstractExcelMediaImportTranslator}
 */
public class ExcelMediaCollectionImportTranslator extends AbstractExcelMediaImportTranslator<Collection<MediaModel>>
{
	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor.getAttributeType() instanceof CollectionTypeModel && getTypeService().isAssignableFrom(
				((CollectionTypeModel) attributeDescriptor.getAttributeType()).getElementType().getCode(), MediaModel._TYPECODE);
	}

	@Override
	public Optional<Object> exportData(final Collection<MediaModel> mediasToExport)
	{
		if (CollectionUtils.isEmpty(mediasToExport))
		{
			return Optional.empty();
		}

		final String export = String.join(",", mediasToExport.stream().map(super::exportMedia).filter(Optional::isPresent)
				.map(Optional::get).collect(Collectors.toList()));
		return StringUtils.isNotBlank(export) ? Optional.of(export) : Optional.empty();
	}

	@Override
	public Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final Impex impex = new Impex();
		final ImpexForType mediaImpex = impex.findUpdates(MediaModel._TYPECODE);
		final Collection<String> mediaReferences = new ArrayList<>();
		importParameters.getMultiValueParameters().stream().filter(this::hasImportData).forEach(params -> {
			final String mediaRefId = generateMediaRefId(attributeDescriptor, params);
			mediaImpex.addRow(createMediaRow(attributeDescriptor, mediaRefId, params));
			mediaReferences.add(mediaRefId);
		});

		if (CollectionUtils.isNotEmpty(mediaReferences))
		{
			final ImpexForType impexForType = impex.findUpdates(importParameters.getTypeCode());
			addReferencedMedia(impexForType, attributeDescriptor, mediaReferences);
		}

		return impex;
	}
}
