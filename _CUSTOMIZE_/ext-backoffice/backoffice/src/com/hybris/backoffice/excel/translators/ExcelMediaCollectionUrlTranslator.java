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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;


/**
 * Excel translator which allows to import media collection as reference. For more information
 * {@link AbstractExcelMediaImportTranslator}
 */
public class ExcelMediaCollectionUrlTranslator extends ExcelMediaCollectionImportTranslator
{
	private final ExcelMediaUrlExportDecorator urlExporter = new ExcelMediaUrlExportDecorator();

	@Override
	public Optional<Object> exportData(final Collection<MediaModel> mediasToExport)
	{
		final String export = Optional.ofNullable(mediasToExport).map(Collection::stream).orElseGet(Stream::empty)//
				.map(this::exportMedia)//
				.filter(Optional::isPresent).map(Optional::get)//
				.collect(Collectors.joining(","));
		return StringUtils.isNotBlank(export) ? Optional.of(export) : Optional.empty();
	}

	@Override
	public Optional<String> exportMedia(final MediaModel media)
	{
		return urlExporter.addUrlToMediaExport(super.exportMedia(media), media);
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return urlExporter.decorateReferenceFormat(super.referenceFormat(attributeDescriptor));
	}
}
