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
package com.hybris.backoffice.widgets.actions.bulkedit;

import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.validator.routines.IntegerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectCreationException;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.util.type.BackofficeTypeUtils;


/**
 * Action which allows to edit many items at once. It sends bulk edit context(map) with {@link #CTX_TYPE_CODE} which is
 * common super type of items to edit and {@link #CTX_ITEMS_TO_EDIT} list of items to edit.
 */
public class BulkEditAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Collection, Map>
{
	private static final Logger LOG = LoggerFactory.getLogger(BulkEditAction.class);

	protected static final String PARAM_CONFIRMATION_THRESHOLD = "confirmation.threshold";
	protected static final String CTX_ITEMS_TO_EDIT = "objectsToEdit";
	public static final String CTX_TYPE_CODE = "typeCode";
	public static final String LABEL_BULKEDIT_CONFIRMATION_MSG = "bulkedit.confirmation";
	public static final String SOCKET_OUT_BULK_EDIT_CTX = "bulkEditCtx";
	public static final String CTX_TEMPLATE_OBJECT = "templateObject";
	@Resource
	private PermissionFacade permissionFacade;
	@Resource
	private BackofficeTypeUtils backofficeTypeUtils;
	@Resource
	private ObjectFacade objectFacade;

	/**
	 * @deprecated since 1808 - use {@link #objectFacade}
	 */
	@Deprecated
	@Resource
	private ModelService modelService;

	@Override
	public boolean canPerform(final ActionContext<Collection> ctx)
	{
		final Collection selectedItems = ctx.getData();
		final ObjectFacadeOperationResult reloadResult = objectFacade.reload(selectedItems);
		if (reloadResult != null && reloadResult.getSuccessfulObjects() != null)
		{
			return reloadResult.getSuccessfulObjects().stream().filter(Objects::nonNull).count() > 0
					&& getPermissionFacade().canChangeInstances(reloadResult.getSuccessfulObjects());
		}
		return false;
	}

	@Override
	public ActionResult<Map> perform(final ActionContext<Collection> context)
	{
		ActionResult<Map> result = new ActionResult<>(ActionResult.ERROR);
		if (context.getData() != null)
		{
			final Map<String, Object> bulkEditCtx = new HashMap<>();
			final ObjectFacadeOperationResult<Object> reloadResult = objectFacade.reload(getItemsToEdit(context));
			final List<Object> refreshedItemsToEdit = new ArrayList<>(reloadResult.getSuccessfulObjects());
			final String closestSuperType = getBackofficeTypeUtils().findClosestSuperType(refreshedItemsToEdit);
			try
			{
				bulkEditCtx.put(CTX_TEMPLATE_OBJECT, getObjectFacade().create(closestSuperType));
			}
			catch (final ObjectCreationException e)
			{
				LOG.debug("Cannot create new item", e);
			}
			bulkEditCtx.put(CTX_TYPE_CODE, closestSuperType);
			bulkEditCtx.put(CTX_ITEMS_TO_EDIT, refreshedItemsToEdit);
			sendOutput(SOCKET_OUT_BULK_EDIT_CTX, bulkEditCtx);
			result = new ActionResult<>(ActionResult.SUCCESS);
			result.setData(bulkEditCtx);
		}
		return result;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<Collection> ctx)
	{
		return getItemsToEdit(ctx).size() > getConfirmationThresholdValue(ctx);
	}

	protected Collection<Object> getItemsToEdit(final ActionContext<Collection> ctx)
	{
		return ctx.getData() != null ? ctx.getData() : Collections.emptyList();
	}

	protected int getConfirmationThresholdValue(final ActionContext<Collection> ctx)
	{
		final String confirmationThreshold = String.valueOf(ctx.getParameter(PARAM_CONFIRMATION_THRESHOLD));
		return IntegerValidator.getInstance().isValid(confirmationThreshold) ? Integer.parseInt(confirmationThreshold) : 500;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<Collection> ctx)
	{
		return ctx.getLabel(LABEL_BULKEDIT_CONFIRMATION_MSG, new Integer[]
		{ getItemsToEdit(ctx).size(), getConfirmationThresholdValue(ctx) });
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	public BackofficeTypeUtils getBackofficeTypeUtils()
	{
		return backofficeTypeUtils;
	}

	/**
	 * @deprecated since 1808 - use {@link #getObjectFacade()}
	 */
	@Deprecated
	public ModelService getModelService()
	{
		return modelService;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}
}
