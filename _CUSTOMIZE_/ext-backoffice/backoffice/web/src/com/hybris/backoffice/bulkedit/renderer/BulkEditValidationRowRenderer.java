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

import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Li;
import org.zkoss.zhtml.Ul;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.backoffice.bulkedit.ValidationResult;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


public class BulkEditValidationRowRenderer implements ListitemRenderer<ValidationResult>
{

	public static final String YW_BULKEDIT_VALIDATION_RESULT_ROW_PREFIX = "yw-bulkedit-validation-result-row";
	public static final String SCLASS_Y_SHOW_DETAILS_BTN_EXPANDED = "y-show-details-btn-expanded";
	public static final String SCLASS_Y_SHOW_DETAILS_BTN = "y-show-details-btn";

	private BulkEditValidationDetailsComponentFactory<Li> factory;
	private LabelService labelService;

	@Override
	public void render(final Listitem listitem, final ValidationResult validationResult, final int i)
	{
		final String headerTitleMessage = labelService.getShortObjectLabel(validationResult.getItem());

		final Listcell validationResultRow = new Listcell();
		final Div validationResultHeader = new Div();
		validationResultHeader.setSclass(YW_BULKEDIT_VALIDATION_RESULT_ROW_PREFIX + "-header");

		final A validationDetailsContainerToggleButton = new A();
		validationDetailsContainerToggleButton.setSclass(SCLASS_Y_SHOW_DETAILS_BTN);
		validationResultHeader.appendChild(validationDetailsContainerToggleButton);

		final Label headerTitle = new Label(headerTitleMessage);
		headerTitle.setSclass(YW_BULKEDIT_VALIDATION_RESULT_ROW_PREFIX + "-header-title");
		validationResultHeader.appendChild(headerTitle);

		final Label headerSubtitle = new Label(getSubtitleLabelValue(validationResult));
		headerSubtitle.setSclass(YW_BULKEDIT_VALIDATION_RESULT_ROW_PREFIX + "-header-subtitle");
		validationResultHeader.appendChild(headerSubtitle);

		validationResultRow.appendChild(validationResultHeader);
		final Div validationDetails = new Div();
		validationDetails.setSclass(YW_BULKEDIT_VALIDATION_RESULT_ROW_PREFIX + "-details");
		validationResultRow.appendChild(validationDetails);
		final EventListener<Event> eventListener = event -> handleToggleDetailsContainer(validationResult,
				validationDetailsContainerToggleButton, validationDetails);
		validationResultHeader.addEventListener(Events.ON_CLICK, eventListener);
		validationDetailsContainerToggleButton.addEventListener(Events.ON_CLICK, eventListener);
		listitem.appendChild(validationResultRow);
	}

	protected void handleToggleDetailsContainer(final ValidationResult validationResult,
			final A validationDetailsContainerToggleButton, final Div validationDetailsContainer)
	{
		final boolean expand = validationDetailsContainer.getChildren().isEmpty();
		modifyStyle(validationDetailsContainerToggleButton, expand);
		if (expand)
		{
			final Ul detailsRows = new Ul();

			for (final ValidationInfo validationMessage : validationResult.getValidationInfos())
			{
				detailsRows.appendChild(factory.createValidationDetails(validationMessage));
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

	protected String getSubtitleLabelValue(final ValidationResult validationResult)
	{
		final String labelPrefix = "bulkedit.validation.header.subtitle.";

		final BiFunction<ValidationSeverity, Long, String> getLabel = (severity, noOfIssues) -> Labels.getLabel(labelPrefix
				+ StringUtils.lowerCase(severity.getCode()), new String[]
		{ String.valueOf(noOfIssues) });

		return validationResult.getValidationInfos() //
				.stream() //
				.collect( //
						Collectors.groupingBy(ValidationInfo::getValidationSeverity, Collectors.counting()) //
				) //
				.entrySet() //
				.stream() //
				.map(entry -> getLabel.apply(entry.getKey(), entry.getValue())) //
				.collect(Collectors.joining(", "));
	}

	@Required
	public void setFactory(final BulkEditValidationDetailsComponentFactory<Li> factory)
	{
		this.factory = factory;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
