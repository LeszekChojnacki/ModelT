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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.zkoss.zul.Filedownload;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.exporting.ExcelExportService;
import com.hybris.backoffice.excel.exporting.ExcelExportWorkbookPostProcessor;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.search.data.pageable.Pageable;


public class ExcelTemplateExportAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<String, Object>
{
	private static final Logger LOG = Logger.getLogger(ExcelTemplateExportAction.class);

	protected static final String MODEL_PAGEABLE = "pageable";

	protected static final String SOCKET_OUT_ITEMS_TO_EXPORT = "itemsToExport";

	@Resource
	private ExcelExportService excelExportService;
	@Resource
	private TypeService typeService;
	@Resource
	private PermissionFacade permissionFacade;
	@Resource
	private ExcelExportWorkbookPostProcessor excelExportWorkbookPostProcessor;

	@Override
	public ActionResult<Object> perform(final ActionContext<String> ctx)
	{
		final String typeCode = ctx.getData();

		if (typeService.isAssignableFrom(ProductModel._TYPECODE, typeCode))
		{
			final Pageable<ItemModel> pageable = getPageable(ctx);
			sendOutput(SOCKET_OUT_ITEMS_TO_EXPORT, pageable);
			return new ActionResult<>(ActionResult.SUCCESS);
		}

		try (final Workbook template = excelExportService.exportTemplate(typeCode);
				final ByteArrayOutputStream excel = new ByteArrayOutputStream())
		{
			final ExcelExportResult result = new ExcelExportResult(template);
			excelExportWorkbookPostProcessor.process(result);

			template.write(excel);
			saveFile(excel, typeCode);
		}
		catch (final IOException e)
		{
			LOG.error("Cannot export excel template file", e);
			return new ActionResult<>(ActionResult.ERROR);
		}

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected void saveFile(final ByteArrayOutputStream stream, final String typeCode)
	{
		Filedownload.save(stream.toByteArray(), Config.getParameter("mediatype.by.fileextension.xlsx"),
				typeCode + "ImportTemplate.xlsx");
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

	protected String getPageableModelProperty(final ActionContext<String> ctx)
	{
		final String pageableModelProperty = (String) ctx.getParameter(MODEL_PAGEABLE);
		return StringUtils.isNotEmpty(pageableModelProperty) ? pageableModelProperty : MODEL_PAGEABLE;
	}

	@Override
	public boolean canPerform(final ActionContext<String> ctx)
	{
		final String typeCode = ctx.getData();
		return StringUtils.isNotEmpty(typeCode) && typeService.isAssignableFrom(ItemModel._TYPECODE, typeCode)
				&& permissionFacade.canReadType(typeCode);
	}

}
