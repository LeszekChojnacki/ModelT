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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.actions.deletesearchconfiguration;

import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.delete.DeleteAction;


public class DeleteSearchConfigurationAction extends DeleteAction
{
	@Resource
	private ModelService modelService;

	@Override
	public boolean canPerform(final ActionContext<Object> ctx)
	{
		final Object data = ctx.getData();

		if (data == null || modelService.isNew(data))
		{
			return false;
		}

		return super.canPerform(ctx);
	}
}
