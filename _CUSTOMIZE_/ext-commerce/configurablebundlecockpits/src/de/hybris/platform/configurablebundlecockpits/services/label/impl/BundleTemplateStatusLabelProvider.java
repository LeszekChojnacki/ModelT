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
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.configurablebundleservices.enums.BundleTemplateStatusEnum;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateStatusModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Label provider implementation for {@link BundleTemplateStatusModel}
 */
public class BundleTemplateStatusLabelProvider extends AbstractModelLabelProvider<BundleTemplateStatusModel>
{
	private TypeService typeService;

	@Override
	protected String getItemLabel(final BundleTemplateStatusModel bundleTemplateStatus)
	{

		if (bundleTemplateStatus.getStatus() != null)
		{
			String label = getTypeService().getEnumerationValue(BundleTemplateStatusEnum._TYPECODE,
					bundleTemplateStatus.getStatus().getCode()).getName();
			if (label == null)
			{
				// no localization for current language -> take code
				label = bundleTemplateStatus.getStatus().getCode();
			}
			return label;
		}

		return bundleTemplateStatus.getId();
	}


	@Override
	protected String getItemLabel(final BundleTemplateStatusModel bundleTemplateStatus, final String languageIso)
	{
		return getItemLabel(bundleTemplateStatus);
	}

	@Override
	protected String getIconPath(final BundleTemplateStatusModel arg0)
	{
		return null;
	}

	@Override
	protected String getIconPath(final BundleTemplateStatusModel arg0, final String arg1)
	{
		return null;
	}

	@Override
	protected String getItemDescription(final BundleTemplateStatusModel arg0)
	{
		return "";
	}

	@Override
	protected String getItemDescription(final BundleTemplateStatusModel arg0, final String arg1)
	{
		return "";
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}
}
