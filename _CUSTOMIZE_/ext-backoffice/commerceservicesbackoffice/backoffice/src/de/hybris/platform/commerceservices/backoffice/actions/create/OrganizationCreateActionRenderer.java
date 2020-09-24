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
package de.hybris.platform.commerceservices.backoffice.actions.create;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;


/**
 * Organization implementation of {@link DefaultActionRenderer}
 *
 */
public class OrganizationCreateActionRenderer extends DefaultActionRenderer<String, Object>
{
	public static final String LBL_SALES_UNIT = "organization.action.create.sales";

	@Override
	protected String getLocalizedName(final ActionContext<?> context)
	{
		return context.getLabel(LBL_SALES_UNIT);
	}
}
