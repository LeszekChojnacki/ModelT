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
package de.hybris.platform.ruleengine.setup;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * this class replicates some functionality from AbstractSystemSetup in the commerceservices extension. (required as
 * this extension does not depend on commerceservices)
 */
public abstract class AbstractRuleEngineSystemSetup
{
	private static final Logger LOG = Logger.getLogger(AbstractRuleEngineSystemSetup.class);

	private String impexExt = ".impex";
	private String fileEncoding = "UTF-8";
	private ImportService importService;
	private CommonI18NService commonI18NService;

	@SuppressWarnings("squid:S1192")
	public void importImpexFile(final String file, final boolean errorIfMissing, final boolean legacyMode)
	{
		try (final InputStream resourceAsStream = getClass().getResourceAsStream(file))
		{
			if (resourceAsStream == null)
			{
				if (errorIfMissing)
				{
					LOG.error("Importing [" + file + "]... ERROR (MISSING FILE)", null);
				}
				else
				{
					LOG.info("Importing [" + file + "]... SKIPPED (Optional File Not Found)");
				}
			}
			else
			{
				importImpexFile(file, resourceAsStream, legacyMode);

				// Try to import language specific impex files
				importLanguageSpecificImpexFiles(file, legacyMode);
			}
		}
		catch (final IOException e)
		{
			LOG.error("FAILED", e);
		}
	}

	protected void importLanguageSpecificImpexFiles(final String file, final boolean legacyMode) throws IOException {
		if (!file.endsWith(getImpexExt()))
		{
			return;
		}
		final String filePath = file.substring(0, file.length() - getImpexExt().length());

		final List<LanguageModel> languages = getCommonI18NService().getAllLanguages();
		for (final LanguageModel language : languages)
		{
			final String languageFilePath = filePath + "_" + language.getIsocode() + getImpexExt();
			try (final InputStream languageResourceAsStream = getClass().getResourceAsStream(languageFilePath))
			{
				if (languageResourceAsStream != null)
				{
					importImpexFile(languageFilePath, languageResourceAsStream, legacyMode);
				}
			}
		}
	}

	protected void importImpexFile(final String file, final InputStream stream, final boolean legacyMode)
	{
		final String message = "Importing [" + file + "]...";

		try
		{
			LOG.info(message);

			final ImportConfig importConfig = new ImportConfig();
			importConfig.setScript(new StreamBasedImpExResource(stream, getFileEncoding()));
			importConfig.setLegacyMode(Boolean.valueOf(legacyMode));

			final ImportResult importResult = getImportService().importData(importConfig);
			if (importResult.isError())
			{
				LOG.error(message + " FAILED");
			}
		}
		catch (final Exception e)
		{
			LOG.error(message + " FAILED", e);
		}
	}



	protected String getImpexExt()
	{
		return impexExt;
	}

	public void setImpexExt(final String impexExt)
	{
		this.impexExt = impexExt;
	}

	protected String getFileEncoding()
	{
		return fileEncoding;
	}

	public void setFileEncoding(final String fileEncoding)
	{
		this.fileEncoding = fileEncoding;
	}

	protected ImportService getImportService()
	{
		return importService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

}
