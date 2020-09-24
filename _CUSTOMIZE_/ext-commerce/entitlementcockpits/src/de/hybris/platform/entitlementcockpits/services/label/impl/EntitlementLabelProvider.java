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
package de.hybris.platform.entitlementcockpits.services.label.impl;

import de.hybris.platform.entitlementservices.model.EntitlementModel;




/**
 * Label provider implementation for {@link EntitlementModel}
 */
public class EntitlementLabelProvider extends AbstractSimplifiedModelLabelProvider<EntitlementModel>
{

	@Override
	protected String getItemLabel(final EntitlementModel item)
	{
		if (item == null)
		{
			return getL10NService().getLocalizedString("cockpit.entitlement.name", new Object[]
			{ "<null>" });
		}

		return getL10NService().getLocalizedString("cockpit.entitlement.name", new Object[]
			{ item.getName() });
	}
}
