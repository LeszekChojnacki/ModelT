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
package de.hybris.platform.deeplink.services.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.deeplink.DeeplinkUtils;
import de.hybris.platform.deeplink.dao.DeeplinkUrlDao;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlModel;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlRuleModel;
import de.hybris.platform.deeplink.pojo.DeeplinkUrlInfo;
import de.hybris.platform.deeplink.resolvers.BarcodeUrlResolver;
import de.hybris.platform.deeplink.services.DeeplinkUrlService;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.io.StringWriter;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * The Class DeeplinkUrlServiceImpl. Default implementation of {@link DeeplinkUrlService}.
 *
 * @spring.bean deeplinkUrlService
 *
 */
public class DeeplinkUrlServiceImpl implements DeeplinkUrlService
{
	private static final Logger LOG = Logger.getLogger(DeeplinkUrlServiceImpl.class);
	private DeeplinkUrlDao deeplinkUrlDao;
	private BarcodeUrlResolver resolver;
	@Resource
	private TypeService typeService;

	@Override
	public String generateShortUrl(final DeeplinkUrlModel deeplinkUrlModel, final Object contextObject)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Short URL generation for DeeplinkUrl object - code: " + deeplinkUrlModel.getCode() + ", baseUrl: "
					+ deeplinkUrlModel.getBaseUrl() + " and Context object: " + contextObject);
		}
		final StringBuilder result = new StringBuilder(deeplinkUrlModel.getBaseUrl());
		result.append("?").append(DeeplinkUtils.getDeeplinkParameterName()).append("=");
		result.append(deeplinkUrlModel.getCode());
		if (contextObject instanceof ItemModel)
		{
			result.append("-").append(((ItemModel) contextObject).getPk().toString());
		}
		LOG.info("Generated short URL: " + result);
		return result.toString();
	}

	@Override
	public LongUrlInfo generateUrl(final String barcodeToken)
	{
		LongUrlInfo result = null;
		DeeplinkUrlRuleModel deeplinkUrlRule = null;

		final DeeplinkUrlInfo deeplinkUrlInfo = getResolver().resolve(barcodeToken);
		final DeeplinkUrlModel deeplinkUrl = deeplinkUrlInfo.getDeeplinkUrl();
		if (deeplinkUrl != null)
		{
			deeplinkUrlRule = getDeeplinkUrlRule(deeplinkUrl.getBaseUrl(), deeplinkUrlInfo.getContextObject());
		}

		if (deeplinkUrlRule != null)
		{
			final VelocityContext context = new VelocityContext();
			context.put("ctx", deeplinkUrlInfo);
			final String parsedUrl = parseTemplate(deeplinkUrlRule.getDestUrlTemplate(), context);
			result = new LongUrlInfo(parsedUrl, deeplinkUrlRule.getUseForward().booleanValue());
		}

		return result;
	}

	public DeeplinkUrlDao getDeeplinkUrlDao()
	{
		return deeplinkUrlDao;
	}

	public BarcodeUrlResolver getResolver()
	{
		return resolver;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Override
	public String parseTemplate(final String template, final VelocityContext context)
	{
		String result = null;
		try
		{
			Velocity.init();
		}
		catch (final Exception e)
		{
			LOG.error("There was error during Velocity engine initialization", e);
			throw new SystemException(e.getMessage(), e);
		}


		final StringWriter url = new StringWriter();

		try
		{
			Velocity.evaluate(context, url, "destTemplateURL", template);
			result = url.toString();
		}
		catch (final ParseErrorException e)
		{
			LOG.error("There was error during parsing template string", e);
		}
		catch (final MethodInvocationException e)
		{
			LOG.error("There was error during invoking method", e);
		}
		catch (final ResourceNotFoundException e)
		{
			LOG.error("Resource not found", e);
		}
		return result;
	}

	public void setDeeplinkUrlDao(final DeeplinkUrlDao deeplinkUrlDao)
	{
		this.deeplinkUrlDao = deeplinkUrlDao;
	}

	public void setResolver(final BarcodeUrlResolver resolver)
	{
		this.resolver = resolver;
	}

	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	/**
	 * Gets the deeplink url rule.
	 *
	 * @param baseUrl
	 *           the base url
	 * @param contextObject
	 *           the context object
	 * @return the deeplink url rule
	 */
	protected DeeplinkUrlRuleModel getDeeplinkUrlRule(final String baseUrl, final Object contextObject)
	{
		for (final DeeplinkUrlRuleModel deeplinkUrlRule : getDeeplinkUrlDao().findDeeplinkUrlRules())
		{
			final TypeModel contextObjectType = getTypeForContextObject(contextObject);

			if (contextObjectType == null)
			{
				if (Pattern.matches(deeplinkUrlRule.getBaseUrlPattern(), baseUrl))
				{
					return deeplinkUrlRule;
				}
			}
			else
			{
				if (Pattern.matches(deeplinkUrlRule.getBaseUrlPattern(), baseUrl)
						&& getTypeService().isAssignableFrom(deeplinkUrlRule.getApplicableType(), contextObjectType))
				{
					return deeplinkUrlRule;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the type for context object.
	 *
	 * @param contextObject
	 *           the context object
	 * @return the type for context object
	 */
	protected TypeModel getTypeForContextObject(final Object contextObject)
	{
		TypeModel type = null;
		if (contextObject instanceof ItemModel)
		{
			type = getTypeService().getComposedTypeForClass(contextObject.getClass());
		}
		return type;
	}
}
