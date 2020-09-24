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
package com.hybris.backoffice.renderer.attributeschooser;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.attributechooser.Attribute;
import com.hybris.backoffice.attributechooser.AttributeChooserForm;
import com.hybris.backoffice.attributechooser.AttributesChooserConfig;
import com.hybris.backoffice.excel.export.wizard.renderer.ExcelExportRenderer;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


public abstract class AbstractAttributesExportRenderer<T> extends DefaultCustomViewRenderer
{
	public static final String PARAM_INCLUDE_ALL_SUPPORTED = "includeAllSupported";


	protected CockpitLocaleService cockpitLocaleService;
	protected NotificationService notificationService;
	protected PermissionFacade permissionFacade;
	protected WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> attributesChooserRenderer;

	protected Attribute createAttributeWithLocalizedChildren(final T attrDesc, final Set<String> supportedLanguages,
			final boolean mandatory)
	{
		final Attribute attribute = new Attribute(createAttributeQualifier(attrDesc), createAttributeName(attrDesc), mandatory);
		if (isLocalized(attrDesc) && !supportedLanguages.isEmpty())
		{
			attribute.setSubAttributes(
					supportedLanguages.stream().map(isoCode -> new Attribute(attribute, isoCode)).collect(Collectors.toSet()));
		}
		return attribute;
	}

	protected Set<String> getSupportedLanguages()
	{
		return getPermissionFacade().getAllReadableLocalesForCurrentUser().stream().map(Locale::toLanguageTag)
				.collect(Collectors.toSet());
	}

	protected AttributesChooserConfig createAttributesChooserConfig(final WidgetInstanceManager wim,
			final Map<String, String> params)
	{
		final AttributesChooserConfig config = new AttributesChooserConfig();
		config.setIncludeAllSupported(
				Boolean.parseBoolean(params.getOrDefault(ExcelExportRenderer.PARAM_INCLUDE_ALL_SUPPORTED, Boolean.TRUE.toString())));
		config.setUniqueModelPrefix(this.getClass().getSimpleName() + "$");
		return config;
	}

	protected abstract String createAttributeQualifier(T attrDesc);

	protected abstract String createAttributeName(T attrDesc);

	protected abstract boolean isLocalized(T attr);

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public CockpitLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	@Required
	public void setCockpitLocaleService(final CockpitLocaleService cockpitLocaleService)
	{
		this.cockpitLocaleService = cockpitLocaleService;
	}

	public WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> getAttributesChooserRenderer()
	{
		return attributesChooserRenderer;
	}

	@Required
	public void setAttributesChooserRenderer(
			final WidgetComponentRenderer<Component, AttributesChooserConfig, AttributeChooserForm> attributesChooserRenderer)
	{
		this.attributesChooserRenderer = attributesChooserRenderer;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
