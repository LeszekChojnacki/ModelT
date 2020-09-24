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
package com.hybris.backoffice.bulkedit.renderer;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.backoffice.bulkedit.BulkEditDownloadValidationReportService;
import com.hybris.backoffice.bulkedit.BulkEditForm;
import com.hybris.backoffice.bulkedit.ValidationResult;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;



public class BulkEditValidationRenderer extends DefaultCustomViewRenderer
{

	private ListitemRenderer<ValidationResult> listitemRenderer;
	private BulkEditDownloadValidationReportService bulkEditDownloadValidationReportService;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final BulkEditForm bulkEditForm = getBulkEditForm(wim, parameters);

		final Div container = new Div();
		container.setSclass("yw-bulkedit-validation-result-container");
		container.setParent(parent);

		final Listbox listBox = createListbox(bulkEditForm.getValidations());
		listBox.setParent(container);

		final Button button = createButton(bulkEditForm.getValidations());
		button.setParent(parent.getParent());
	}

	protected BulkEditForm getBulkEditForm(final WidgetInstanceManager wim, final Map<String, String> params)
	{
		final String attributesFormModelKey = params.get("bulkEditFormModelKey");
		return wim.getModel().getValue(attributesFormModelKey, BulkEditForm.class);
	}

	protected Listbox createListbox(final List<ValidationResult> validationResult)
	{
		final Listbox listBox = new Listbox();
		listBox.setDisabled(true);
		listBox.setNonselectableTags("*");
		listBox.setOddRowSclass("yw-bulkedit-validation-result-container-odd-row");

		listBox.setModel(new ListModelArray<>(validationResult));
		listBox.setItemRenderer(getListitemRenderer());
		return listBox;
	}

	protected Button createButton(final List<ValidationResult> validationResult)
	{
		final Button button = new Button(Labels.getLabel("bulkedit.wizard.button.downloadReport"));
		button.addEventListener(Events.ON_CLICK, event -> bulkEditDownloadValidationReportService.downloadReport(validationResult));
		button.setSclass("yw-bulkedit-validation-result-report");
		return button;
	}

	public ListitemRenderer<ValidationResult> getListitemRenderer()
	{
		return listitemRenderer;
	}

	public void setListitemRenderer(final ListitemRenderer<ValidationResult> listitemRenderer)
	{
		this.listitemRenderer = listitemRenderer;
	}

	public BulkEditDownloadValidationReportService getBulkEditDownloadValidationReportService()
	{
		return bulkEditDownloadValidationReportService;
	}

	@Required
	public void setBulkEditDownloadValidationReportService(
			final BulkEditDownloadValidationReportService bulkEditDownloadValidationReportService)
	{
		this.bulkEditDownloadValidationReportService = bulkEditDownloadValidationReportService;
	}
}
