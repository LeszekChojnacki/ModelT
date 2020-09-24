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

package de.hybris.platform.configurablebundlecockpits.services.label.impl;

import de.hybris.platform.cockpit.services.label.AbstractModelLabelProvider;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.configurablebundleservices.model.BundleSelectionCriteriaModel;
import de.hybris.platform.configurablebundleservices.model.PickExactlyNBundleSelectionCriteriaModel;
import de.hybris.platform.configurablebundleservices.model.PickNToMBundleSelectionCriteriaModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Label provider implementation for {@link BundleSelectionCriteriaModel} and sub-types
 */
public class BundleSelectionCriteriaModelLabelProvider extends AbstractModelLabelProvider<BundleSelectionCriteriaModel>
{
	private L10NService l10NService;

	@Override
	protected String getItemLabel(final BundleSelectionCriteriaModel selectionCriteria)
	{
		String label = "";
		if (selectionCriteria instanceof PickNToMBundleSelectionCriteriaModel)
		{
			final PickNToMBundleSelectionCriteriaModel model = (PickNToMBundleSelectionCriteriaModel) selectionCriteria;
			label = getL10NService().getLocalizedString("cockpit.bundleselection.pickntom", new Object[]
			{ model.getN(), model.getM() });
		}
		else if (selectionCriteria instanceof PickExactlyNBundleSelectionCriteriaModel)
		{
			final PickExactlyNBundleSelectionCriteriaModel model = (PickExactlyNBundleSelectionCriteriaModel) selectionCriteria;
			label = getL10NService().getLocalizedString("cockpit.bundleselection.pickexactly", new Object[]
			{ model.getN() });
		}
		return label;
	}

	@Override
	protected String getItemLabel(final BundleSelectionCriteriaModel selectionCriteria, final String languageIso)
	{
		return getItemLabel(selectionCriteria);
	}

	@Override
	protected String getIconPath(final BundleSelectionCriteriaModel item)
	{
		return null;
	}

	@Override
	protected String getIconPath(final BundleSelectionCriteriaModel item, final String languageIso)
	{
		return null;
	}

	@Override
	protected String getItemDescription(final BundleSelectionCriteriaModel item)
	{
		return "";
	}

	@Override
	protected String getItemDescription(final BundleSelectionCriteriaModel item, final String languageIso)
	{
		return "";
	}

	@Required
	public void setL10NService(final L10NService l10NService)
	{
		this.l10NService = l10NService;
	}

	protected L10NService getL10NService()
	{
		return l10NService;
	}
}
