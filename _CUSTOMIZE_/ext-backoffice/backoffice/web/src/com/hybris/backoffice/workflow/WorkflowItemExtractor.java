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
package com.hybris.backoffice.workflow;

import java.util.Collection;

import com.hybris.backoffice.widgets.networkchart.context.NetworkChartContext;


public interface WorkflowItemExtractor
{
	/**
	 * Extracts {@link WorkflowItem} from the {@link NetworkChartContext}
	 *
	 * @param context
	 *           which contains WorkflowModel objects
	 * @return extracted workflow items
	 */
	Collection<WorkflowItem> extract(NetworkChartContext context);
}
