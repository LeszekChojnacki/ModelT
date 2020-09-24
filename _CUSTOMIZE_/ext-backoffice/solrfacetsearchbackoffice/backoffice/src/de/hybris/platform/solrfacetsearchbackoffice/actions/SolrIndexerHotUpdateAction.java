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
package de.hybris.platform.solrfacetsearchbackoffice.actions;

import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;


public class SolrIndexerHotUpdateAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<SolrFacetSearchConfigModel, Object>
{
	protected static final String OUTPUT_SOCKET = "operationWizard";

	@Override
	public ActionResult<Object> perform(final ActionContext<SolrFacetSearchConfigModel> ctx)
	{
		final Map<Object, Object> parametersMap = Maps.newHashMap();
		parametersMap.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), "SolrIndexerHotUpdateWizard");
		parametersMap.put("facetSearchConfig", ctx.getData());
		sendOutput(OUTPUT_SOCKET, parametersMap);
		return new ActionResult(ActionResult.SUCCESS, null);
	}

}
