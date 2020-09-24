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

package de.hybris.platform.configurablebundlecockpits.admincockpit.session.impl;

import de.hybris.platform.cockpit.events.CockpitEvent;
import de.hybris.platform.cockpit.events.impl.ItemChangedEvent;
import de.hybris.platform.cockpit.session.impl.BaseUICockpitPerspective;
import de.hybris.platform.configurablebundleservices.model.BundleSelectionCriteriaModel;


/**
 * Overrides the BasePerspective to implement the following changes:
 * <ul>
 * <li>the owning BundleTemplate gets a refresh if its bundleSelectionCriteria is modified
 * </ul>
 */
public class AdminBundlePerspective extends BaseUICockpitPerspective
{
	@Override
	public void onCockpitEvent(final CockpitEvent event)
	{
		super.onCockpitEvent(event);
		if (event instanceof ItemChangedEvent)
		{
			final ItemChangedEvent itemChangedEvent = (ItemChangedEvent) event;
			if (itemChangedEvent.getItem().getObject() instanceof BundleSelectionCriteriaModel)
			{
				this.update();
			}
		}
	}
}
