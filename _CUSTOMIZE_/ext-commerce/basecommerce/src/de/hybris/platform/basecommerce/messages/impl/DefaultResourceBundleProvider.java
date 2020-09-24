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
package de.hybris.platform.basecommerce.messages.impl;

import de.hybris.platform.basecommerce.messages.ResourceBundleProvider;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.servicelayer.i18n.impl.CompositeResourceBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.apache.log4j.Logger;


/**
 * Default Implementation of interface {@link ResourceBundleProvider}
 */
public class DefaultResourceBundleProvider implements ResourceBundleProvider
{
	private static final Logger LOG = Logger.getLogger(DefaultResourceBundleProvider.class);

	/** Building and look-up for bundles is expensive so we cache it here. */
	private final Map<Locale, ResourceBundle> bundlesMap = new HashMap<Locale, ResourceBundle>();

	/** hybris specific resource bundles: [extension]/resources/localization/[extension]/[resourceBundle]. */
	private String resourceBundle;

	@Override
	public ResourceBundle getResourceBundle(final Locale locale)
	{
		ResourceBundle result = bundlesMap.get(locale);

		if (result == null)
		{
			final Collection<String> extensions = ExtensionManager.getInstance().getExtensionNames();
			final List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

			for (final String extension : extensions)
			{
				try
				{

					bundles.add(getBundleWithFallback(locale, extension + "." + resourceBundle));
				}
				catch (final MissingResourceException e)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug(extension + "/" + resourceBundle + " is NOT available" + e);
					}
				}
			}

			Collections.reverse(bundles);
			result = new CompositeResourceBundle(bundles);
			this.bundlesMap.put(locale, result);
		}

		return result;
	}


	/**
	 * Method retrieves resource bundle starting with disabled fallback ,and if it fails (on websphere it occurs always)
	 * , method tries to resolve bundle with fallback enabled
	 *
	 */
	protected ResourceBundle getBundleWithFallback(final Locale locale, final String resourceKey)
	{
		final Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
		ResourceBundle bundle = null;
		try
		{
			if (locale.equals(Locale.getDefault()))
			{
				bundle = ResourceBundle.getBundle(resourceKey, control);
			}
			else
			{
				bundle = ResourceBundle.getBundle(resourceKey, locale, control);
			}
		}
		catch (final MissingResourceException e)
		{
			if (locale.equals(Locale.getDefault()))
			{
				bundle = ResourceBundle.getBundle(resourceKey);
			}
			else
			{
				bundle = ResourceBundle.getBundle(resourceKey, locale);
			}
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Can't load fallback default resource bundle " + resourceKey + ", reason: " + e);
			}
		}
		return bundle;
	}


	/**
	 * @param resourceBundle
	 *           the resourceBundle to set
	 */
	public void setResourceBundle(final String resourceBundle)
	{
		this.resourceBundle = resourceBundle;
	}
}
