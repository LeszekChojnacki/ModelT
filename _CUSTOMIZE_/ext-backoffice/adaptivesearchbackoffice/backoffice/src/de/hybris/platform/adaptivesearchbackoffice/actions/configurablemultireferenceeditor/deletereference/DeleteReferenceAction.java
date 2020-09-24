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
package de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.deletereference;

import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class DeleteReferenceAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<AbstractEditorData, Object>
{
	@Resource
	private ModelService modelService;

	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		final ItemModel model = data.getModel();
		modelService.remove(model);

		ReferenceActionUtils.refreshCurrentObject(ctx);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		if (data == null)
		{
			return false;
		}

		return data.isFromSearchConfiguration() && !data.isOverride();
	}
}
