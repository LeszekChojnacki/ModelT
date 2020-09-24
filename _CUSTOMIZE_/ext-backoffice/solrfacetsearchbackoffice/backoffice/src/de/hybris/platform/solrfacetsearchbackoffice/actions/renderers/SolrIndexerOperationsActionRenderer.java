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
package de.hybris.platform.solrfacetsearchbackoffice.actions.renderers;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;


public class SolrIndexerOperationsActionRenderer extends DefaultActionRenderer<String, Object>
{
	protected static final String ACTION_NAME = "solrindexeroperations.action.name";

	@Override
	protected String getLocalizedName(final ActionContext<?> context)
	{
		return context.getLabel(ACTION_NAME);
	}
}
