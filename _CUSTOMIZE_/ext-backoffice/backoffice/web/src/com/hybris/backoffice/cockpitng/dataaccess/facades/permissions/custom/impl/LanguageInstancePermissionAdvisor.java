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
package com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.impl;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.InstancePermissionAdvisor;


public class LanguageInstancePermissionAdvisor implements InstancePermissionAdvisor<LanguageModel>
{

	private ModelService modelService;

	@Override
	public boolean canModify(final LanguageModel instance)
	{
		return true;
	}

	@Override
	public boolean canDelete(final LanguageModel instance)
	{
		if (!modelService.isNew(instance))
		{
			final ItemModelContext context = instance.getItemModelContext();
			if (context.isDirty(LanguageModel.ACTIVE))
			{
				return BooleanUtils.isNotTrue(context.<Boolean> getOriginalValue(LanguageModel.ACTIVE));
			}
		}
		return BooleanUtils.isNotTrue(instance.getActive());
	}

	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof LanguageModel;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
