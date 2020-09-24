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
package com.hybris.backoffice.config.impl;

import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultActionsConfigurationFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Action;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.ActionGroup;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Actions;


public class DefaultBackofficeActionsConfigurationFallbackStrategy extends DefaultActionsConfigurationFallbackStrategy
{

	private TypeService typeService;

	@Override
	public Actions loadFallbackConfiguration(final ConfigContext context, final Class<Actions> configurationType)
	{

		final String componentCode = resolveRequiredComponentCode(context);
		if (componentCode != null && CollectionUtils.isNotEmpty(getComponentCodes()) && !getComponentCodes().contains(componentCode))
		{
			return null;
		}
		final String type = context.getAttribute(DefaultConfigContext.CONTEXT_TYPE);
		if (StringUtils.isNotBlank(type) && getTypeForCode(type) instanceof ViewTypeModel)
		{
			final Actions fallbackActions = new Actions();
			final ActionGroup group = new ActionGroup();

			fallbackActions.getGroup().add(group);

			group.setQualifier(DefaultActionsConfigurationFallbackStrategy.DEFAULT_ACTION_GROUP_QUALIFIER);

			final Action exportCsvAction = new Action();
			exportCsvAction.setActionId(DefaultActionsConfigurationFallbackStrategy.EXPORT_CSV_ACTION_ID);
			exportCsvAction.setProperty(DefaultActionsConfigurationFallbackStrategy.EXPORT_COLUMNS_AND_DATA_PROPERTY);
			group.getActions().add(exportCsvAction);
			return fallbackActions;
		}

		return super.loadFallbackConfiguration(context, configurationType);
	}

	private TypeModel getTypeForCode(final String typeCode)
	{
		try
		{
			return typeService.getTypeForCode(typeCode);
		}
		catch (final UnknownIdentifierException e)
		{
			return null;
		}
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
}
