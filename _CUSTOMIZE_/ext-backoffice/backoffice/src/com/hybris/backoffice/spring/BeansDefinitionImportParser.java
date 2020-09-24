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
package com.hybris.backoffice.spring;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import de.hybris.platform.spring.ctx.WebScopeTenantIgnoreDocReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.hybris.backoffice.constants.BackofficeModules;


/**
 * Bean definitions template parser responsible for parsing <code>import-modules</code> tag.
 * <P>
 * Parser iterates over all defined backoffice modules and imports their spring beans configuration files.
 * </P>
 */
public class BeansDefinitionImportParser extends AbstractMultipleBeanParser
{

	private static final Logger LOGGER = LoggerFactory.getLogger(BeansDefinitionImportParser.class);

	protected static final String ELEMENT_NAME = "import-modules";

	private static final String ATTRIBUTE_RESOURCES = "resources";


	@Override
	protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext)
	{
		try
		{
			final Resource[] resources = getResources(element, parserContext);
			importResources(resources, parserContext);
		}
		catch (final IOException ex)
		{
			throw new BeanDefinitionValidationException("Unable to import specified resources: " + element, ex);
		}

		return null;
	}

	protected Resource[] getResources(final Element element, final ParserContext parserContext) throws IOException
	{
		final Optional<String> resourcesPattern = getResourcesPattern(element);
		final String pattern = resourcesPattern.orElseGet(element::getNodeValue);
		if (StringUtils.isNotBlank(pattern))
		{
			final PathMatchingResourcePatternResolver pmrl = new PathMatchingResourcePatternResolver();
			return pmrl.getResources(pattern);
		}
		else
		{
			return BackofficeModules.getBackofficeModules().stream().map(BackofficeModules::getSpringDefinitionsFile)
					.filter(File::exists).map(FileSystemResource::new).toArray(Resource[]::new);
		}
	}

	protected Optional<String> getResourcesPattern(final Element element)
	{
		if (element.hasAttributes())
		{
			final Node attribute = element.getAttributes().getNamedItem(ATTRIBUTE_RESOURCES);
			if (attribute != null)
			{
				return Optional.of(attribute.getNodeValue());
			}
		}

		return Optional.empty();
	}

	protected void importResources(final Resource[] resources, final ParserContext parserContext) throws IOException
	{
		for (final Resource resource : resources)
		{
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("Importing additional bean definitions: {}", resource.getURI());
			}

			final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(parserContext.getRegistry());
			reader.setDocumentReaderClass(WebScopeTenantIgnoreDocReader.class);
			reader.setEnvironment(parserContext.getReaderContext().getEnvironment());
			final int definitionsCount = reader.loadBeanDefinitions(resource);

			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Imported {} beans from {}", definitionsCount, resource.getURI());
			}
		}
	}

}
