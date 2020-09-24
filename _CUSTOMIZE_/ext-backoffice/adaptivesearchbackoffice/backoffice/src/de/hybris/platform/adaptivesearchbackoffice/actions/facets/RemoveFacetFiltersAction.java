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
package de.hybris.platform.adaptivesearchbackoffice.actions.facets;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;

import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.ReferenceActionUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetFiltersRequestData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.MultiReferenceEditorLogic;

import java.util.Collections;

import org.apache.commons.collections4.CollectionUtils;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class RemoveFacetFiltersAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<AbstractFacetConfigurationEditorData, Object>
{
	@Override
	public ActionResult<Object> perform(final ActionContext<AbstractFacetConfigurationEditorData> ctx)
	{
		final MultiReferenceEditorLogic<AbstractFacetConfigurationEditorData, ?> editorLogic = ReferenceActionUtils
				.resolveEditorLogic(ctx);
		final AbstractFacetConfigurationEditorData data = ctx.getData();

		final FacetFiltersRequestData request = new FacetFiltersRequestData();
		request.setIndexProperty(data.getIndexProperty());
		request.setValues(Collections.emptyList());

		editorLogic.getWidgetInstanceManager().sendOutput(SEARCH_REQUEST_SOCKET, request);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<AbstractFacetConfigurationEditorData> ctx)
	{
		final AbstractFacetConfigurationEditorData data = ctx.getData();
		if (data == null || data.getFacet() == null)
		{
			return false;
		}

		return CollectionUtils.isNotEmpty(data.getFacet().getSelectedValues());
	}
}
