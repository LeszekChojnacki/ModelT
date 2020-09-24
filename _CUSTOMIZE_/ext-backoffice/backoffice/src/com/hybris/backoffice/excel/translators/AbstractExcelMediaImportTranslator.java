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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.impex.jalo.media.MediaDataTranslator;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.type.TypeService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Abstract media translator which allows to import media references with content. Format:
 *
 * <pre>
 *     filePath:code:catalog:version
 * </pre>
 *
 * Where:
 * <ul>
 * <li>{@value #PARAM_FILE_PATH} - defines file in the uploaded zip (sub dirs can be specified)</li>
 * <li>{@value #PARAM_CODE} - media's code {@link MediaModel#CODE}.
 * <ul>
 * <li>if empty and {@value #PARAM_FILE_PATH} is defined then code will be generated.</li>
 * <li>if code:catalog:version corresponds to an existing media it will be assigned as a reference</li>
 * <li>if {@value #PARAM_FILE_PATH} is defined and code:catalog:version corresponds to an existing media it's content
 * will be updated and it will be assigned as a reference</li>
 * </ul>
 * </li>
 * <li>catalog:version - defines media's catalog version {@link MediaModel#CATALOGVERSION}</li>
 * </ul>
 *
 * @param <T>
 *           type of imported reference. It should be related to Media e.g. single reference or collection of medias.
 */
public abstract class AbstractExcelMediaImportTranslator<T> extends AbstractCatalogVersionAwareTranslator<T>
{
	public static final String PARAM_FILE_PATH = "filePath";
	public static final String PARAM_FOLDER = "folder";
	public static final String PARAM_CODE = "code";
	protected static final String MEDIA_CONTENT_HEADER_NAME = "@media";
	private TypeService typeService;
	private KeyGenerator mediaCodeGenerator;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return PARAM_FILE_PATH + ":" + PARAM_CODE + ":" + super.referenceCatalogVersionFormat() + ":" + PARAM_FOLDER;
	}

	/**
	 * Exports single media
	 *
	 * @param media
	 *           media to export
	 * @return media exported according to {@link #referenceFormat(AttributeDescriptorModel)}
	 */
	public Optional<String> exportMedia(final MediaModel media)
	{
		return media != null ? Optional.of(String.format(":%s:%s:%s", media.getCode(),
				super.exportCatalogVersionData(media.getCatalogVersion()), media.getFolder().getQualifier())) : Optional.empty();
	}

	protected Map<ImpexHeaderValue, Object> createMediaRow(final AttributeDescriptorModel attributeDescriptor,
			final String mediaRefId, final Map<String, String> params)
	{
		final Map<ImpexHeaderValue, Object> row = new HashMap<>();
		row.put(createMediaReferenceIdHeader(attributeDescriptor, params), mediaRefId);
		row.put(createMediaCodeHeader(attributeDescriptor, params), getCode(attributeDescriptor, params));
		row.put(createMediaCatalogVersionHeader(attributeDescriptor, params), catalogVersionData(params));
		if (StringUtils.isNotEmpty(getFolder(attributeDescriptor, params)))
		{
			row.put(createMediaFolderHeader(attributeDescriptor, params), getFolder(attributeDescriptor, params));
		}

		final String filePath = getFilePath(attributeDescriptor, params);
		if (StringUtils.isNotBlank(filePath))
		{
			row.put(createMediaContentHeader(attributeDescriptor, params), filePath);
		}

		return row;
	}

	protected boolean hasImportData(final Map<String, String> singleParams)
	{
		final String code = singleParams.get(AbstractExcelMediaImportTranslator.PARAM_CODE);
		final String filePath = singleParams.get(AbstractExcelMediaImportTranslator.PARAM_FILE_PATH);
		final String catalog = singleParams.get(CatalogVersionModel.CATALOG);
		final String version = singleParams.get(CatalogVersionModel.VERSION);

		final boolean hasCodeAndPath = !StringUtils.isAllBlank(code, filePath);
		final boolean hasCatalogAndVersion = !StringUtils.isAnyBlank(catalog, version);

		return hasCodeAndPath && hasCatalogAndVersion;
	}

	protected String generateMediaRefId(final AttributeDescriptorModel attributedescriptor, final Map<String, String> params)
	{
		if (params != null)
		{
			final String code = params.get(PARAM_CODE);
			if (StringUtils.isNotBlank(code))
			{
				final String catalog = params.get(CatalogVersionModel.CATALOG);
				final String version = params.get(CatalogVersionModel.VERSION);
				final String folder = params.get(PARAM_FOLDER);
				final String combinedValue = String.format("%s_%s_%s_%s_%s", AbstractExcelMediaImportTranslator.class.getSimpleName(),
						code, catalog, version, folder);

				return Base64.getEncoder().encodeToString(combinedValue.getBytes(StandardCharsets.UTF_8));
			}
		}
		return UUID.randomUUID().toString();
	}

	protected void addReferencedMedia(final ImpexForType impexForType, final AttributeDescriptorModel attributeDescriptor,
			final Collection<String> mediaRefs)
	{
		addReferencedMedia(impexForType, attributeDescriptor, String.join(",", mediaRefs));
	}

	protected void addReferencedMedia(final ImpexForType impexForType, final AttributeDescriptorModel attributeDescriptor,
			final String mediaRef)
	{
		final ImpexHeaderValue mediaHeader = createReferenceHeader(attributeDescriptor);
		impexForType.putValue(0, mediaHeader, mediaRef);
	}

	protected ImpexHeaderValue createReferenceHeader(final AttributeDescriptorModel attributeDescriptor)
	{
		return new ImpexHeaderValue.Builder(
				String.format("%s(%s)", attributeDescriptor.getQualifier(), Impex.EXCEL_IMPORT_DOCUMENT_REF_HEADER_NAME))
						.withQualifier(attributeDescriptor.getQualifier()).build();
	}

	protected ImpexHeaderValue createMediaContentHeader(final AttributeDescriptorModel attributeDescriptor,
			final Map<String, String> params)
	{
		return new ImpexHeaderValue.Builder(MEDIA_CONTENT_HEADER_NAME).withTranslator(MediaDataTranslator.class.getName()).build();
	}

	protected ImpexHeaderValue createMediaCatalogVersionHeader(final AttributeDescriptorModel attributeDescriptor,
			final Map<String, String> params)
	{
		return new ImpexHeaderValue.Builder(catalogVersionHeader(MediaModel._TYPECODE)).withUnique(true)
				.withMandatory(mandatoryFilter.test(attributeDescriptor)).build();
	}

	protected ImpexHeaderValue createMediaCodeHeader(final AttributeDescriptorModel attributeDescriptor,
			final Map<String, String> params)
	{
		return new ImpexHeaderValue.Builder(MediaModel.CODE).withUnique(true)
				.withMandatory(mandatoryFilter.test(attributeDescriptor)).build();
	}

	protected ImpexHeaderValue createMediaReferenceIdHeader(final AttributeDescriptorModel attributeDescriptor,
			final Map<String, String> params)
	{
		return new ImpexHeaderValue.Builder(Impex.EXCEL_IMPORT_DOCUMENT_REF_HEADER_NAME).build();
	}

	protected ImpexHeaderValue createMediaFolderHeader(final AttributeDescriptorModel attributeDescriptor,
			final Map<String, String> params)
	{
		return new ImpexHeaderValue.Builder(String.format("%s(%s)", MediaModel.FOLDER, MediaFolderModel.QUALIFIER)).build();
	}

	protected String getCode(final AttributeDescriptorModel attributeDescriptor, final Map<String, String> params)
	{
		final String code = params.get(PARAM_CODE);
		return StringUtils.isNotBlank(code) ? code : mediaCodeGenerator.generate().toString();
	}

	protected String getFilePath(final AttributeDescriptorModel attributeDescriptor, final Map<String, String> params)
	{
		return params.get(PARAM_FILE_PATH);
	}

	protected String getFolder(final AttributeDescriptorModel attributeDescriptor, final Map<String, String> params)
	{
		return params.get(PARAM_FOLDER);
	}

	@Override
	public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		// not called in overridden importData
		throw new UnsupportedOperationException("Sub class should override importData method");
	}

	@Override
	public Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		throw new UnsupportedOperationException("Sub class should override importData method");
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public KeyGenerator getMediaCodeGenerator()
	{
		return mediaCodeGenerator;
	}

	@Required
	public void setMediaCodeGenerator(final KeyGenerator mediaCodeGenerator)
	{
		this.mediaCodeGenerator = mediaCodeGenerator;
	}

	public ExcelFilter<AttributeDescriptorModel> getMandatoryFilter()
	{
		return mandatoryFilter;
	}

	@Required
	public void setMandatoryFilter(final ExcelFilter<AttributeDescriptorModel> mandatoryFilter)
	{
		this.mandatoryFilter = mandatoryFilter;
	}
}
