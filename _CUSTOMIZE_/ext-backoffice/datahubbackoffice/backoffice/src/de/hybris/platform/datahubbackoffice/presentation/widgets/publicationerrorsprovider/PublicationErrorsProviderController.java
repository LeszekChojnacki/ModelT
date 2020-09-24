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
package de.hybris.platform.datahubbackoffice.presentation.widgets.publicationerrorsprovider;

import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.publication.TargetSystemPublicationData;

import de.hybris.platform.datahubbackoffice.dataaccess.publication.error.PublicationErrorFieldSearchFacadeStrategy;

import org.zkoss.zk.ui.select.annotation.WireVariable;

public class PublicationErrorsProviderController extends DefaultWidgetController
{
	@WireVariable("publicationErrorsFieldSearchFacadeStrategy")
	protected transient PublicationErrorFieldSearchFacadeStrategy fieldSearchFacade;

	@SocketEvent(socketId = "publication")
	public void getErrorsForGivenPublication(final TargetSystemPublicationData targetSystemPublicationData)
	{
		final AdvancedSearchQueryData data = new AdvancedSearchQueryData.Builder("Datahub_PublicationError")
				.conditions(toSearchCondition(targetSystemPublicationData))
				.build();
		final Pageable<ErrorData> pageable = fieldSearchFacade.search(data);
		sendOutput("errors", pageable);
	}

	private static SearchQueryCondition toSearchCondition(final TargetSystemPublicationData targetSystemPublicationData)
	{
		final SearchQueryCondition condition = new SearchQueryCondition();
		condition.setDescriptor(new SearchAttributeDescriptor("targetSystemPublication", 0));
		condition.setOperator(null);
		condition.setValue(targetSystemPublicationData);
		return condition;
	}
}
