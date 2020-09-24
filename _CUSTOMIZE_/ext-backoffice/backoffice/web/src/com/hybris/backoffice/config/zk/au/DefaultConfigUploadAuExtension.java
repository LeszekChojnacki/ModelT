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
package com.hybris.backoffice.config.zk.au;

import com.hybris.cockpitng.admin.CockpitAdminService;
import com.hybris.cockpitng.adminmode.CockpitAdminComposer;
import com.hybris.cockpitng.adminmode.FilteredConfigRewriter;
import com.hybris.cockpitng.adminmode.exception.ContextModificationException;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultCockpitConfigurationService;
import com.hybris.cockpitng.core.config.impl.jaxb.Config;
import com.hybris.cockpitng.core.util.CockpitProperties;
import com.hybris.cockpitng.core.util.jaxb.JAXBContextFactory;
import com.hybris.cockpitng.core.util.jaxb.SchemaValidationStatus;
import com.hybris.cockpitng.core.util.jaxb.SchemaValidationStatus.SchemaValidationCode;
import com.hybris.cockpitng.util.CockpitSessionService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.http.AuExtension;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;


public class DefaultConfigUploadAuExtension implements AuExtension
{
	protected static final String CONFIG_VALIDATE = "/configValidate";
	protected static final String CONFIG_UPLOAD = "/configUpload";
	private static final String HEADER_RESPONSE_VALIDATION_STATUS = "validationStatus";
	private static final String HEADER_RESPONSE_VALIDATION_LABEL = "validationLabel";
	private static final String HEADER_RESPONSE_PERSISTENCE_SUCCESSFUL = "persistenceSuccessful";
	private static final String HEADER_RESPONSE_PERSISTENCE_FAILURE_LABEL = "persistenceFailureLabel";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigUploadAuExtension.class);
	private static final String VALIDATE_COCKPIT_CONFIG_ORCHESTRATOR_SETTING = "cockpitng.validate.cockpitConfig.orchestrator";
	public static final String I18N_CONFIG_PERSISTENCE_FAILED_GENERIC_MESSAGE = "config.persistence.failed.generic.message";


	@Override
	public void init(final DHtmlUpdateServlet servlet)
	{
		LOG.debug("Initialising multipart configuration handler: " + this);
	}

	@Override
	public void destroy()
	{
		LOG.debug("Destroying multipart configuration handler: " + this);
	}

	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response, final String pi)
	{
		switch (pi)
		{
			case CONFIG_UPLOAD:
				handleConfigUpload(request, response);
				break;
			case CONFIG_VALIDATE:
				handleConfigValidate(request, response);
				break;
			default:
				throw new IllegalArgumentException("Unrecognised path: " + pi);
		}
	}

	protected void handleConfigValidate(final HttpServletRequest request, final HttpServletResponse response)
	{
		try
		{
			if (shouldValidateCockpitConfig(request))
			{
				final InputStream inputStream = getInputStream(request);
				try
				{
					final SchemaValidationStatus status = getCockpitConfigurationService(request).validate(inputStream);
					formatConfigValidationResponse(status, response);
				}
				finally
				{
					IOUtils.closeQuietly(inputStream);
				}
			}
		}
		catch (final IOException ioe)
		{
			LOG.warn("Could not process content of the configuration", ioe);
		}
	}

	protected void formatConfigValidationResponse(final SchemaValidationStatus status, final HttpServletResponse response)
	{
		response.addHeader(HEADER_RESPONSE_VALIDATION_STATUS, status.getCode().toString());
		response.addHeader(HEADER_RESPONSE_VALIDATION_LABEL, getValidationLabelForStatus(status));
	}

	protected InputStream getInputStream(final HttpServletRequest request) throws IOException
	{
		return new ReaderInputStream(request.getReader());
	}

	protected String getValidationLabelForStatus(final SchemaValidationStatus status)
	{
		return Labels.getLabel("config.validation." + status.getCode());
	}

	protected void handleConfigUpload(final HttpServletRequest request, final HttpServletResponse response)
	{
		try
		{
			final String xml = fetchConfigFromRequest(request);

			final InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
			final SchemaValidationStatus validationStatus = getCockpitConfigurationService(request).validate(inputStream);

			if (validationStatus.getCode() != SchemaValidationCode.SUCCESS)
			{
				formatConfigValidationResponse(validationStatus, response);
			}
			if (validationStatus.getCode() != SchemaValidationCode.ERROR)
			{
				storeConfig(xml, request, response);
				response.addHeader(HEADER_RESPONSE_PERSISTENCE_SUCCESSFUL, Boolean.TRUE.toString());
			}
		}
		catch (final IOException ioe)
		{
			LOG.warn("Could not process content of the configuration", ioe);
			response.addHeader(HEADER_RESPONSE_PERSISTENCE_SUCCESSFUL, Boolean.FALSE.toString());
			response.addHeader(HEADER_RESPONSE_PERSISTENCE_FAILURE_LABEL,
					Labels.getLabel(I18N_CONFIG_PERSISTENCE_FAILED_GENERIC_MESSAGE));
		}
	}

	protected String fetchConfigFromRequest(final HttpServletRequest request) throws IOException
	{
		try (final BufferedReader reader = request.getReader();)
		{
			final StringWriter writer = new StringWriter();
			IOUtils.copy(reader, writer);
			return writer.toString();
		}
	}

	protected void storeConfig(final String xml, final HttpServletRequest request, final HttpServletResponse response)
	{
		try
		{
			if (isConfigurationFiltered(request))
			{
				final Config changesAsConfig = getCockpitConfigurationService(request).getChangesAsConfig(xml,
						getConfigUnmarshaller(request));
				final Config merged = getConfigWithAppliedChanges(changesAsConfig, request);
				getCockpitConfigurationService(request).storeRootConfig(merged);
			}
			else
			{
				getCockpitConfigurationService(request).setConfigAsString(xml);
			}

			getCockpitAdminService(request).refreshCockpit();
		}
		catch (final ContextModificationException | JAXBException | CockpitConfigurationException error)
		{
			handleError(response, error);
		}
	}

	protected boolean isConfigurationFiltered(final HttpServletRequest request)
	{
		final Object filtered = getCockpitSessionService(request).getAttribute(CockpitAdminComposer.COCKPIT_CONFIGURATION_FILTERED);
		return filtered instanceof Boolean && BooleanUtils.isTrue((Boolean) filtered);
	}

	protected void handleError(final HttpServletResponse response, final Exception error)
	{
		LOG.warn("Failed to process configuration", error);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	protected boolean shouldValidateCockpitConfig(final HttpServletRequest request)
	{
		return BooleanUtils.isTrue(getCockpitProperties(request).getBoolean(VALIDATE_COCKPIT_CONFIG_ORCHESTRATOR_SETTING));
	}

	protected CockpitProperties getCockpitProperties(final HttpServletRequest request)
	{
		return getApplicationContext(request).getBean("cockpitProperties", CockpitProperties.class);
	}

	protected CockpitAdminService getCockpitAdminService(final HttpServletRequest request)
	{
		return getApplicationContext(request).getBean("cockpitAdminService", CockpitAdminService.class);
	}

	protected Unmarshaller getConfigUnmarshaller(final HttpServletRequest request) throws JAXBException
	{
		final JAXBContextFactory cockpitJAXBContextFactory = getApplicationContext(request).getBean("cockpitJAXBContextFactory",
				JAXBContextFactory.class);

		return cockpitJAXBContextFactory.createContext(Config.class).createUnmarshaller();
	}

	protected Config getConfigWithAppliedChanges(final Config changes, final HttpServletRequest request)
			throws CockpitConfigurationException
	{
		final Config original = getCockpitConfigurationService(request).loadRootConfiguration();
		FilteredConfigRewriter.applyChangesInFilteredConfig(original, changes);
		return original;
	}

	protected DefaultCockpitConfigurationService getCockpitConfigurationService(final HttpServletRequest request)
	{
		return getApplicationContext(request).getBean("cockpitConfigurationService", DefaultCockpitConfigurationService.class);
	}

	protected CockpitSessionService getCockpitSessionService(final HttpServletRequest request)
	{
		return getApplicationContext(request).getBean("cockpitSessionService", CockpitSessionService.class);
	}

	public ApplicationContext getApplicationContext(final HttpServletRequest request)
	{
		return WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
	}
}
