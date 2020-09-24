/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.compositionerrorsprovider;

import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.event.CompositionActionData;
import com.hybris.datahub.dto.item.ErrorData;

import de.hybris.platform.datahubbackoffice.dataaccess.composition.CompositionActionConstants;
import de.hybris.platform.datahubbackoffice.dataaccess.composition.error.CompositionErrorFieldSearchFacadeStrategy;

import org.zkoss.zk.ui.select.annotation.WireVariable;

public class CompositionErrorsProviderController extends DefaultWidgetController
{
	@WireVariable("compositionErrorFieldSearchFacadeStrategy")
	protected transient CompositionErrorFieldSearchFacadeStrategy fieldSearchFacade;

	@SocketEvent(socketId = "composition")
	public void getErrorsForGivenComposition(final CompositionActionData actionData)
	{
		final AdvancedSearchQueryData searchData = new AdvancedSearchQueryData.Builder(
				CompositionActionConstants.COMPOSITION_ERROR_TYPE_CODE)
				.conditions(toSearchCondition(actionData))
				.build();

		final Pageable<ErrorData> pageable = fieldSearchFacade.search(searchData);
		sendOutput("errors", pageable);
	}

	private static SearchQueryCondition toSearchCondition(final CompositionActionData actionData)
	{
		final SearchQueryCondition condition = new SearchQueryCondition();
		condition.setDescriptor(new SearchAttributeDescriptor(CompositionActionConstants.COMPOSITION_ACTION_ATTRIBUTE_DESCRIPTOR, 0));
		condition.setOperator(null);
		condition.setValue(actionData);
		return condition;
	}
}
