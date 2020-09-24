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

import de.hybris.platform.subscriptionservices.model.OverageUsageChargeEntryModel;
import de.hybris.platform.subscriptionservices.model.PerUnitUsageChargeModel;
import de.hybris.platform.subscriptionservices.model.UsageChargeEntryModel;

import org.apache.commons.lang.StringUtils;


/**
 * Label provider implementation for {@link PerUnitUsageChargeModel}
 */
public class PerUnitUsageChargeLabelProvider extends AbstractSubscriptionModelLabelProvider<PerUnitUsageChargeModel>
{

	@Override
	protected String getItemLabel(final PerUnitUsageChargeModel model)
	{
		String usageUnit = "<null>";
		String tiersAndOverage = StringUtils.EMPTY;

		if (model == null)
		{
			return getL10NService().getLocalizedString("cockpit.usagecharge.perunit.name", new Object[]
			{ "<null>", "<null>", "<null>" });
		}

		if (model.getUsageUnit() != null)
		{
			usageUnit = model.getUsageUnit().getNamePlural();
		}

		if (model.getUsageChargeEntries() != null)
		{
			tiersAndOverage = getTiersAndOverage(model);
		}

		if (model.getName() == null)
		{
			return getL10NService().getLocalizedString("cockpit.usagecharge.perunit.noname", new Object[]
			{ usageUnit, tiersAndOverage });
		}

		return getL10NService().getLocalizedString("cockpit.usagecharge.perunit.name", new Object[]
		{ model.getName(), usageUnit, tiersAndOverage });
	}

	protected String getTiersAndOverage(final PerUnitUsageChargeModel model)
	{
		final int size = model.getUsageChargeEntries().size();
		if (isOverage(model))
		{
			if (size == 1)
			{
				return getL10NService().getLocalizedString("cockpit.usagecharge.overage.name");
			}
			else
			{
				return getL10NService().getLocalizedString("cockpit.usagecharge.tiersandoverage.name", new Object[]
				{ Integer.toString(size - 1) });
			}
		}
		else
		{
			return getL10NService().getLocalizedString("cockpit.usagecharge.tiers.name", new Object[]
			{ Integer.toString(size) });
		}
	}

	protected boolean isOverage(final PerUnitUsageChargeModel model)
	{
		boolean isOverage = false;

		for (final UsageChargeEntryModel usageChargeEntryModel : model.getUsageChargeEntries())
		{
			if (usageChargeEntryModel instanceof OverageUsageChargeEntryModel)
			{
				isOverage = true;
				break;
			}
		}
		return isOverage;
	}
}
