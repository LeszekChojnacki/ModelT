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

import de.hybris.platform.validation.enums.Severity;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Li;
import org.zkoss.zhtml.Ul;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;
import com.hybris.cockpitng.util.UITools;


/**
 * Allows to display a single error found in uploaded excel file
 */
public class ExcelImportValidationRowResultRenderer implements ListitemRenderer<ExcelValidationResult>
{

	public static final String YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX = "yw-excel-validation-result-row";
	public static final String SCLASS_Y_SHOW_DETAILS_BTN_EXPANDED = "y-show-details-btn-expanded";
	public static final String SCLASS_Y_SHOW_DETAILS_BTN = "y-show-details-btn";

	private ExcelValidationDetailsComponentFactory<Li> factory;

	@Override
	public void render(final Listitem listitem, final ExcelValidationResult excelValidationResult, final int index)
			throws Exception
	{
		final String headerTitleMessage = Labels.getLabel(excelValidationResult.getHeader().getMessageKey(),
				excelValidationResult.getHeader().getParams());

		final Listcell validationResultRow = new Listcell();
		final Div validationResultHeader = new Div();
		validationResultHeader.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-header");

		final A validationDetailsContainerToggleButton = new A();
		validationDetailsContainerToggleButton.setSclass(SCLASS_Y_SHOW_DETAILS_BTN);
		validationResultHeader.appendChild(validationDetailsContainerToggleButton);

		final Label headerTitle = new Label(headerTitleMessage);
		headerTitle.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-header-title");
		validationResultHeader.appendChild(headerTitle);

		final Label headerSubtitle = new Label(getSubtitleLabelValue(excelValidationResult));
		headerSubtitle.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-header-subtitle");
		validationResultHeader.appendChild(headerSubtitle);

		validationResultRow.appendChild(validationResultHeader);
		final Div validationDetails = new Div();
		validationDetails.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-details");
		validationResultRow.appendChild(validationDetails);
		final EventListener<Event> eventListener = event -> handleToggleDetailsContainer(excelValidationResult,
				validationDetailsContainerToggleButton, validationDetails);
		validationResultHeader.addEventListener(Events.ON_CLICK, eventListener);
		validationDetailsContainerToggleButton.addEventListener(Events.ON_CLICK, eventListener);
		listitem.appendChild(validationResultRow);
	}

	protected void handleToggleDetailsContainer(final ExcelValidationResult excelValidationResult,
			final A validationDetailsContainerToggleButton, final Div validationDetailsContainer)
	{
		final boolean expand = validationDetailsContainer.getChildren().isEmpty();
		modifyStyle(validationDetailsContainerToggleButton, expand);
		if (expand)
		{
			final Ul detailsRows = new Ul();

			for (final ValidationMessage validationMessage : excelValidationResult.getValidationErrors())
			{
				detailsRows.appendChild(factory.createValidationDetails(
						validationMessage
								.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.SELECTED_ATTRIBUTE_DISPLAYED_NAME_KEY),
						validationMessage));
			}
			validationDetailsContainer.appendChild(detailsRows);
		}
		else
		{
			validationDetailsContainer.getChildren().clear();
		}
	}

	protected void modifyStyle(final A validationDetailsContainerToggleButton, final boolean expand)
	{
		UITools.modifySClass(validationDetailsContainerToggleButton, SCLASS_Y_SHOW_DETAILS_BTN, !expand);
		UITools.modifySClass(validationDetailsContainerToggleButton, SCLASS_Y_SHOW_DETAILS_BTN_EXPANDED, expand);
	}

	protected String getSubtitleLabelValue(final ExcelValidationResult excelValidationResult)
	{
		final String labelPrefix = "excel.import.validation.header.subtitle.";

		final BiFunction<Severity, Long, String> getLabel = (severity, noOfIssues) -> Labels
				.getLabel(labelPrefix + StringUtils.lowerCase(severity.getCode()), new String[]
		{ String.valueOf(noOfIssues) });

		return excelValidationResult.getValidationErrors() //
				.stream() //
				.collect( //
						Collectors.groupingBy(ValidationMessage::getSeverity, Collectors.counting()) //
				) //
				.entrySet() //
				.stream() //
				.map(entry -> getLabel.apply(entry.getKey(), entry.getValue())) //
				.collect(Collectors.joining(", "));
	}

	/**
	 * @deprecated since 1808. Use {@link ExcelValidationDetailsComponentFactory} instead.
	 */
	@Deprecated
	protected Component createDetailsRow(final Object columnHeader, final String validationMessage)
	{
		final Li li = new Li();
		li.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-details-cell");
		if (columnHeader != null && !StringUtils.isBlank(columnHeader.toString()))
		{
			final Label columnHeaderLabel = new Label(String.format("[%s]: ", columnHeader));
			columnHeaderLabel.setSclass(YW_EXCEL_VALIDATION_RESULT_ROW_PREFIX + "-details-cell-column-header");
			li.appendChild(columnHeaderLabel);
		}
		final Label validationMessageLabel = new Label(String.format("%s", validationMessage));
		li.appendChild(validationMessageLabel);
		return li;
	}

	@Required
	public void setFactory(final ExcelValidationDetailsComponentFactory<Li> factory)
	{
		this.factory = factory;
	}
}
