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
package de.hybris.platform.subscriptioncockpits.services.label.impl;

import de.hybris.platform.cockpit.services.label.AbstractModelLabelProvider;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.i18n.L10NService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract label provider implementation for {@link ItemModel}
 */
public class AbstractSubscriptionModelLabelProvider<R extends ItemModel> extends AbstractModelLabelProvider<R>
{

	private L10NService l10NService;

	@Override
	protected String getIconPath(final R item)
	{
		return null;
	}

	@Override
	protected String getIconPath(final R item, final String languageIso)
	{
		return null;
	}

	@Override
	protected String getItemDescription(final R item)
	{
		return null;
	}

	@Override
	protected String getItemDescription(final R item, final String languageIso)
	{
		return null;
	}

	@Override
	protected String getItemLabel(final R item)
	{
		return null;
	}

	@Override
	protected String getItemLabel(final R item, final String languageIso)
	{
		return null;
	}

	@Required
	public void setL10NService(final L10NService l10nService)
	{
		l10NService = l10nService;
	}

	protected L10NService getL10NService()
	{
		return l10NService;
	}
}
