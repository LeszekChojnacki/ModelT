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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.translators.AbstractExcelMediaImportTranslator;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Validator which checks whether the uploaded file has a proper file extension. List of available file extensions is
 * configured via {@value #CONFIG_EXCEL_AVAILABLE_MEDIA_EXTENSIONS}.
 */
public class ExcelMediaFilenameExtensionValidator extends ExcelMediaImportValidator
{

	protected static final String EXCEL_IMPORT_VALIDATION_MEDIA_EXTENSIONS = "excel.import.validation.media.extensions";
	protected static final String CONFIG_EXCEL_AVAILABLE_MEDIA_EXTENSIONS = "excel.available.media.extensions";

	private ConfigurationService configurationService;

	@Override
	public List<ValidationMessage> validateSingleValue(final Map<String, Object> context, final Map<String, String> parameters)
	{
		return Optional.ofNullable(parameters.get(AbstractExcelMediaImportTranslator.PARAM_FILE_PATH)) //
				.filter(String.class::isInstance) //
				.map(String.class::cast) //
				.map(this::validateZipEntries) //
				.orElse(Collections.emptyList());
	}

	protected List<ValidationMessage> validateZipEntries(final String filePath)
	{
		final Map<String, String> cache = new HashMap<>();
		final String extensionsKey = "extensions";
		final Supplier<String> availableExtensions = () -> {
			if (!cache.containsKey(extensionsKey))
			{
				cache.put(extensionsKey, getAvailableExtensions());
			}
			return cache.get(extensionsKey);
		};

		return Optional.of(filePath) //
				.map(FilenameUtils::getExtension) //
				.filter(StringUtils::isNotBlank) //
				.filter(this::isNotAvailable) //
				.map(extension -> new ValidationMessage(EXCEL_IMPORT_VALIDATION_MEDIA_EXTENSIONS, extension, filePath,
						availableExtensions.get())) //
				.map(Lists::newArrayList) //
				.orElse(new ArrayList<>());
	}

	protected boolean isNotAvailable(final String extension)
	{
		return getConfigExtensions() //
				.stream() //
				.distinct() //
				.map(String::trim) //
				.noneMatch(configExtension -> StringUtils.equalsIgnoreCase(configExtension, extension));
	}

	protected Collection<String> getConfigExtensions()
	{
		return Lists.newArrayList(StringUtils.split(
				configurationService.getConfiguration().getString(CONFIG_EXCEL_AVAILABLE_MEDIA_EXTENSIONS, StringUtils.EMPTY), ","));
	}

	protected String getAvailableExtensions()
	{
		return getConfigExtensions().stream().collect(Collectors.joining(", ", "[", "]"));
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
