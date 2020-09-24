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
package com.hybris.backoffice.widgets.actions.sync;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class SyncAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, List>
{

	protected static final String SOCKET_OUT_SELECTED_OBJECTS = "currentObjects";
	@Resource
	private ObjectFacade objectFacade;

	@Override
	public boolean canPerform(final ActionContext<Object> ctx)
	{
		if (ctx.getData() != null)
		{
			final List<Object> data = getData(ctx);
			return CollectionUtils.isNotEmpty(data) && data.stream().noneMatch(objectFacade::isModified);
		}
		return false;
	}

	@Override
	public ActionResult<List> perform(final ActionContext<Object> context)
	{
		ActionResult<List> result = new ActionResult<>(ActionResult.ERROR);
		if (context.getData() != null)
		{
			sendOutput(SOCKET_OUT_SELECTED_OBJECTS, getData(context));
			result = new ActionResult<>(ActionResult.SUCCESS);
		}
		return result;
	}

	protected List<Object> getData(final ActionContext<Object> context)
	{
		if (context.getData() instanceof Collection)
		{
			final Collection<Object> data = (Collection) context.getData();
			return data.stream().filter(o -> !Objects.isNull(o)).collect(Collectors.toList());
		}
		else if (context.getData() != null)
		{
			return Lists.newArrayList(context.getData());
		}
		return Collections.emptyList();
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}
}
