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
package com.hybris.backoffice.widgets.actions.excel;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IntegerValidator;
import org.zkoss.zul.Messagebox;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;
import com.hybris.cockpitng.util.type.BackofficeTypeUtils;


public class ExcelExportAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<String, Pageable<? extends ItemModel>>
{
	protected static final String SOCKET_OUT_ITEMS_TO_EXPORT = "itemsToExport";

	protected static final String LABEL_EXCEL_EXPORT_ACTION_CONFIRMATION = "excelExportAction.confirmation";
	protected static final String PARAM_CONFIRMATION_THRESHOLD = "confirmation.threshold";

	protected static final String MODEL_SELECTED_OBJECTS = "selectedObjects";
	protected static final String MODEL_PAGEABLE = "pageable";

	@Resource
	private TypeService typeService;
	@Resource
	private PermissionFacade permissionFacade;
	@Resource
	private BackofficeTypeUtils backofficeTypeUtils;

	@Override
	public ActionResult<Pageable<? extends ItemModel>> perform(final ActionContext<String> ctx)
	{
		final ActionResult<Pageable<? extends ItemModel>> result = new ActionResult<>(ActionResult.ERROR);
		getData(ctx).ifPresent(excelExportData -> {
			final int totalCount = excelExportData.getTotalCount();
			if (isMaxRowsExceeded(ctx, totalCount))
			{
				showMaxRowsExceeded(ctx, totalCount);
			}
			else
			{
				sendOutput(SOCKET_OUT_ITEMS_TO_EXPORT, excelExportData);
				result.setResultCode(ActionResult.SUCCESS);
				result.setData(excelExportData);
			}
		});
		return result;
	}

	protected void showMaxRowsExceeded(final ActionContext<String> ctx, final int rows)
	{
		Messagebox.show(ctx.getLabel("export.max.rows.exceeded.msg", new Integer[]
		{ rows, getExportMaxRows(ctx) }), ctx.getLabel("export.max.rows.exceeded.title"), Messagebox.OK, Messagebox.EXCLAMATION);
	}

	protected boolean isMaxRowsExceeded(final ActionContext<String> ctx, final int rows)
	{
		return rows > getExportMaxRows(ctx);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<String> ctx)
	{
		final int size = getSize(ctx);
		return size > getConfirmationThresholdValue(ctx) && size <= getExportMaxRows(ctx);
	}

	protected int getExportMaxRows(final ActionContext<String> ctx)
	{
		return Config.getInt("backoffice.excel.export.max.rows", Integer.MAX_VALUE);
	}

	@Override
	public String getConfirmationMessage(final ActionContext<String> ctx)
	{
		return ctx.getLabel(LABEL_EXCEL_EXPORT_ACTION_CONFIRMATION, new Integer[]
		{ getSize(ctx), getConfirmationThresholdValue(ctx) });
	}

	@Override
	public boolean canPerform(final ActionContext<String> ctx)
	{
		final String typeCode = StringUtils.isNotBlank(ctx.getData()) ? ctx.getData()
				: getSelectedItemsType(ctx).orElse(StringUtils.EMPTY);
		return StringUtils.isNotEmpty(typeCode) && typeService.isAssignableFrom(ItemModel._TYPECODE, typeCode)
				&& permissionFacade.canReadType(typeCode);
	}

	/**
	 * Returns data related with collection browser's pageable. If no items are selected then the all items for specific
	 * typeCode are returned. Otherwise, only selected items are returned.
	 *
	 * @param ctx
	 *           context of the ExcelExportAction
	 * @return optional of pageable
	 */
	public Optional<Pageable<ItemModel>> getData(final ActionContext<String> ctx)
	{
		return Optional.ofNullable(getSelectedItems(ctx).orElseGet(() -> getPageable(ctx)));
	}

	protected Optional<Pageable<ItemModel>> getSelectedItems(final ActionContext<String> ctx)
	{
		final Collection<Object> selectedObjects = getSelectedItemsFromModel(ctx);
		if (CollectionUtils.isNotEmpty(selectedObjects))
		{
			final String typeCode = getBackofficeTypeUtils().findClosestSuperType(new ArrayList<>(selectedObjects));
			return Optional.of(new PageableList(new ArrayList(selectedObjects), selectedObjects.size(), typeCode));
		}
		return Optional.empty();
	}

	protected Collection<Object> getSelectedItemsFromModel(final ActionContext<String> ctx)
	{
		final WidgetModel widgetModel = getWidgetModel(ctx);
		return widgetModel != null ? widgetModel.getValue(getSelectedItemsModelProperty(ctx), Collection.class)
				: Collections.emptyList();
	}

	protected Optional<String> getSelectedItemsType(final ActionContext<String> ctx)
	{
		final Collection<Object> selection = getSelectedItemsFromModel(ctx);
		if (CollectionUtils.isNotEmpty(selection))
		{
			return Optional.ofNullable(backofficeTypeUtils.findClosestSuperType(new ArrayList<>(selection)));
		}
		return Optional.empty();
	}

	protected Pageable<ItemModel> getPageable(final ActionContext<String> ctx)
	{
		final WidgetModel widgetModel = getWidgetModel(ctx);
		return widgetModel != null ? widgetModel.getValue(getPageableModelProperty(ctx), Pageable.class) : null;
	}

	protected WidgetModel getWidgetModel(final ActionContext<String> ctx)
	{
		return (WidgetModel) ctx.getParameter(ActionContext.PARENT_WIDGET_MODEL);
	}

	public int getSize(final ActionContext<String> ctx)
	{
		return getData(ctx).map(Pageable::getTotalCount).orElse(0);
	}

	protected String getPageableModelProperty(final ActionContext<String> ctx)
	{
		final String pageableModelProperty = (String) ctx.getParameter(MODEL_PAGEABLE);
		return StringUtils.isNotEmpty(pageableModelProperty) ? pageableModelProperty : MODEL_PAGEABLE;
	}

	protected String getSelectedItemsModelProperty(final ActionContext<String> ctx)
	{
		final String selectedItemsModelProperty = (String) ctx.getParameter(MODEL_SELECTED_OBJECTS);
		return StringUtils.isNotEmpty(selectedItemsModelProperty) ? selectedItemsModelProperty : MODEL_SELECTED_OBJECTS;
	}

	protected int getConfirmationThresholdValue(final ActionContext<String> ctx)
	{
		final String confirmationThreshold = String.valueOf(ctx.getParameter(PARAM_CONFIRMATION_THRESHOLD));
		return IntegerValidator.getInstance().isValid(confirmationThreshold) ? Integer.parseInt(confirmationThreshold) : 500;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	public BackofficeTypeUtils getBackofficeTypeUtils()
	{
		return backofficeTypeUtils;
	}
}
