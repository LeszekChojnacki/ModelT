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
package com.hybris.backoffice.excel.imp.wizard.renderer;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.imp.wizard.ExcelDownloadReportService;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


/**
 * Allows to display list of errors found in uploaded excel file
 */
public class ExcelImportValidationResultRenderer extends DefaultCustomViewRenderer
{

	private static final String LABEL_EXCEL_IMPORT_WIZARD_DOWNLOAD_REPORT = "excel.import.wizard.downloadvalidationerrros.button.label";
	private static final String LABEL_EXCEL_IMPORT_WIZARD_VALIDATION_RESULT_SUBTITLE = "excel.import.wizard.validation.result.subtitle";
	private ListitemRenderer<ExcelValidationResult> listitemRenderer;
	private ExcelDownloadReportService excelDownloadReportService;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final List<ExcelValidationResult> result = wim.getModel().getValue(ExcelConstants.EXCEL_IMPORT_VALIDATION_RESULT,
				List.class);

		final Div container = new Div();
		container.setSclass("yw-excel-validation-result-container");
		container.setParent(parent);

		final Div subtitle = createSubtitle();
		subtitle.setParent(container);

		final Listbox listBox = createListbox(result);
		listBox.setParent(container);

		final Button button = createButton(result);
		button.setParent(parent.getParent());
	}

	protected Div createSubtitle()
	{
		final Div subtitleContainer = new Div();
		subtitleContainer.setSclass("yw-excel-validation-result-subtitle-container");

		final Label subtitle = new Label(Labels.getLabel(LABEL_EXCEL_IMPORT_WIZARD_VALIDATION_RESULT_SUBTITLE));
		subtitle.setSclass("yw-excel-validation-result-subtitle");
		subtitle.setParent(subtitleContainer);
		return subtitleContainer;
	}

	protected Listbox createListbox(final List<ExcelValidationResult> result)
	{
		final Listbox listBox = new Listbox();
		listBox.setDisabled(true);
		listBox.setNonselectableTags("*");
		listBox.setOddRowSclass("yw-excel-validation-result-container-odd-row");

		listBox.setModel(new ListModelArray<>(result));
		listBox.setItemRenderer(getListitemRenderer());
		return listBox;
	}

	protected Button createButton(final List<ExcelValidationResult> result)
	{
		final Button button = new Button(Labels.getLabel(LABEL_EXCEL_IMPORT_WIZARD_DOWNLOAD_REPORT));
		button.addEventListener(Events.ON_CLICK, event -> excelDownloadReportService.downloadReport(result));
		button.setSclass("yw-excel-validation-result-report");
		return button;
	}

	public ListitemRenderer<ExcelValidationResult> getListitemRenderer()
	{
		return listitemRenderer;
	}

	@Required
	public void setListitemRenderer(final ListitemRenderer<ExcelValidationResult> listitemRenderer)
	{
		this.listitemRenderer = listitemRenderer;
	}

	public ExcelDownloadReportService getExcelDownloadReportService()
	{
		return excelDownloadReportService;
	}

	@Required
	public void setExcelDownloadReportService(final ExcelDownloadReportService excelDownloadReportService)
	{
		this.excelDownloadReportService = excelDownloadReportService;
	}
}
