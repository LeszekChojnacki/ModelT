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

import org.zkoss.zhtml.Li;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.validation.model.ValidationInfo;


public class DefaultBulkEditValidationDetailsComponentFactory implements BulkEditValidationDetailsComponentFactory<Li>
{

	public static final String YW_BULKEDIT_VALIDATION_RESULT_DETAILS_CELL = "yw-bulkedit-validation-result-row-details-cell";

	@Override
	public Li createValidationDetails(final ValidationInfo validationMessage)
	{
		final Li li = new Li();
		li.setSclass(YW_BULKEDIT_VALIDATION_RESULT_DETAILS_CELL);
		final Label validationMessageLabel = new Label(getMessageValue(validationMessage));
		li.appendChild(validationMessageLabel);
		return li;
	}

	protected String getMessageValue(final ValidationInfo validationMessage)
	{
		return validationMessage.getValidationMessage();
	}

}
