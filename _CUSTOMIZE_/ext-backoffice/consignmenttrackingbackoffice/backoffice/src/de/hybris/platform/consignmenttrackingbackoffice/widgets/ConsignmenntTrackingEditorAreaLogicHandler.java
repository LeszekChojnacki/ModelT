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
package de.hybris.platform.consignmenttrackingbackoffice.widgets;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.util.localization.Localization;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import static java.util.Objects.isNull;

import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.impl.DefaultValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;

/**
 * A handler to check a product coverage info before saving.
 */
public class ConsignmenntTrackingEditorAreaLogicHandler extends DefaultEditorAreaLogicHandler
{
	public static final String MSGKEY = "type.validation.consignment.tracking.text";
	@Override
	public List<ValidationInfo> performValidation(final WidgetInstanceManager widgetInstanceManager, final Object currentObject,
			final ValidationContext validationContext)
	{
		final List<ValidationInfo> validationInfos = new ArrayList<>(
				super.performValidation(widgetInstanceManager, currentObject, validationContext)); 
		if (currentObject instanceof ConsignmentModel)
		{
			final ConsignmentModel consignment = (ConsignmentModel) currentObject;
			if (!isValidConsignment(consignment))
			{
				validationInfos.add(createValidationInfo());
			}
		}
		return validationInfos;
	}

	/**
	 * validate consignment info
	 * 
	 * @param consignment
	 *           the being edited consignment
	 * @return true if Carrier and TrackingID both empty or not empty,false otherwise
	 */
	protected boolean isValidConsignment(final ConsignmentModel consignment)
	{
		return (isNull(consignment.getCarrierDetails()) && StringUtils.isEmpty(consignment.getTrackingID()))
				|| (!isNull(consignment.getCarrierDetails()) && StringUtils.isNotEmpty(consignment.getTrackingID()));

	}


	/**
	 * Create validation info
	 * 
	 * @return ValidationInfo Validation info
	 */
	protected ValidationInfo createValidationInfo()
	{
		final DefaultValidationInfo validationInfo = new DefaultValidationInfo();

		final String msg = Localization.getLocalizedString(MSGKEY);
		validationInfo.setValidationMessage(msg);
		validationInfo.setConfirmed(false);
		validationInfo.setValidationSeverity(ValidationSeverity.ERROR);
		return validationInfo;
	}
}
