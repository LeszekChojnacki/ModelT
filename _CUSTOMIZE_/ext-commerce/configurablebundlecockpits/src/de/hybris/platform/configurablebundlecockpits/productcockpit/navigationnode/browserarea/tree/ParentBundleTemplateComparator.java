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

package de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree;

import de.hybris.platform.configurablebundleservices.enums.BundleTemplateStatusEnum;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateStatusModel;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sort {@link BundleTemplateModel}s by their {@link BundleTemplateStatusModel}. Move approved bundle templates to the
 * top and archived bundles to the bottom.
 */
public class ParentBundleTemplateComparator implements Comparator<BundleTemplateModel>, Serializable
{
	@Override
	// NOSONAR
	public int compare(final BundleTemplateModel template1, final BundleTemplateModel template2)
	{
		final BundleTemplateStatusEnum status1 = template1.getStatus().getStatus();
		final BundleTemplateStatusEnum status2 = template2.getStatus().getStatus();


		if (BundleTemplateStatusEnum.ARCHIVED.equals(status2) && !status1.equals(status2))
		{
			return -1;
		}
		else if (BundleTemplateStatusEnum.ARCHIVED.equals(status1) && !status1.equals(status2))
		{
			return 1;
		}
		else
		{
			if (template1.getName() == null)
			{
				return template2.getName() == null ? 0 : -1;
			}
			else
			{
				return template2.getName() == null ? 1 : template1.getName().compareTo(template2.getName());
			}
		}
	}
}
