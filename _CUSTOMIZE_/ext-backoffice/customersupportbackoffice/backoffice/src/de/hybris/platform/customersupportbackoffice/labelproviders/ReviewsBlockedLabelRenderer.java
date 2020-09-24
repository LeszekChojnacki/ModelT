/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.labelproviders;

import de.hybris.platform.customerreview.model.CustomerReviewModel;

import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 *
 * This Label provider will be used to display camel case boolean values.
 *
 */

public class ReviewsBlockedLabelRenderer implements WidgetComponentRenderer<Listcell, ListColumn, CustomerReviewModel>
{
	protected static final String TRUE_STRING = "True";
	protected static final String FALSE_STRING = "False";

	@Override
	public void render(final Listcell parent, final ListColumn configuration, final CustomerReviewModel customerReview,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		Label reviewBlockedLabel = null;
		if (customerReview.getBlocked().booleanValue())
		{
			reviewBlockedLabel = new Label(TRUE_STRING);
		}
		else
		{
			reviewBlockedLabel = new Label(FALSE_STRING);
		}
		reviewBlockedLabel.setVisible(true);
		reviewBlockedLabel.setParent(parent);
	}
}
