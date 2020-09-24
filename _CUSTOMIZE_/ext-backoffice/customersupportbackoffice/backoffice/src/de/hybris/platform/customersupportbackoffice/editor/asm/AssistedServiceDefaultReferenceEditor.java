/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.editor.asm;

import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;

import de.hybris.platform.site.BaseSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.CockpitConfigurationNotFoundException;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Base;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorLayout;
import com.hybris.cockpitng.editor.defaultreferenceeditor.DefaultReferenceEditor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.engine.WidgetInstanceManager;

import javax.annotation.Resource;

/**
 * Renderer for com.hybris.cockpitng.editor.asmdeeplinkreferenceeditor.
 */
public class AssistedServiceDefaultReferenceEditor<T> extends DefaultReferenceEditor<T>
{

	private static final Logger LOG = LoggerFactory.getLogger(AssistedServiceDefaultReferenceEditor.class);

	@Resource
	private UrlResolver<ProductModel> productModelUrlResolver;
	@Resource
	protected BaseSiteService baseSiteService;

	@Override
	public ReferenceEditorLayout<T> createReferenceLayout(final EditorContext context)
	{
		final AsmRendererLayout layout = new AsmRendererLayout<>(this,
			loadBaseConfiguration(getTypeCode(), (WidgetInstanceManager) context.getParameter(Editor.WIDGET_INSTANCE_MANAGER)),
			productModelUrlResolver, baseSiteService);
		layout.setSimpleSelectEnabled(
				Boolean.parseBoolean((String) context.getParameter(CustomersupportbackofficeConstants.SIMPLE_SELECT_PARAM)));
		layout.setShowAsmPrefix(
				Boolean.parseBoolean((String) context.getParameter(CustomersupportbackofficeConstants.ASM_PREFIX_PARAM)));
		layout.setAsmForwardURL((String) context.getParameter(CustomersupportbackofficeConstants.ASM_FORWARD_URL_PARAM));
		layout.setPlaceholderKey(getPlaceholderKey());
		return layout;
	}

	@Override
	protected Base loadBaseConfiguration(final String typeCode, final WidgetInstanceManager wim)
	{
		Base config = null;

		final DefaultConfigContext configContext = new DefaultConfigContext("base");
		configContext.setType(typeCode);

		try
		{
			config = wim.loadConfiguration(configContext, Base.class);
			if (config == null)
			{
				LOG.warn("Loaded UI configuration is null. Ignoring.");
			}
		}
		catch (final CockpitConfigurationNotFoundException ccnfe)
		{
			LOG.debug("Could not find UI configuration for given context (" + configContext + ").", ccnfe);
		}
		catch (final CockpitConfigurationException cce)
		{
			LOG.error("Could not load cockpit config for the given context '" + configContext + "'.", cce);
		}

		return config;
	}
}
