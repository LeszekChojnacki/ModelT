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
package de.hybris.platform.datahubbackoffice.dataaccess.composition.error;

import de.hybris.platform.datahubbackoffice.dataaccess.DataHubPageable;
import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.dataaccess.composition.CompositionActionConstants;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.PoolActionClient;
import com.hybris.datahub.dto.event.CompositionActionData;
import com.hybris.datahub.dto.item.ErrorData;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Required;

public class CompositionErrorFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<ErrorData>
{
	private static final String DATAHUB_COMPOSITION_TYPECODE = CompositionActionConstants.COMPOSITION_ERROR_TYPE_CODE;
	private PoolActionClient poolActionClient;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return DATAHUB_COMPOSITION_TYPECODE.equals(typeCode);
	}

	@Override
	public Pageable<ErrorData> search(final SearchQueryData searchQueryData)
	{
		return new CompositionErrorPageable(searchQueryData);
	}

	@Required
	public void setPoolActionClient(final PoolActionClient poolActionClient)
	{
		this.poolActionClient = poolActionClient;
	}

	private class CompositionErrorPageable extends DataHubPageable<ErrorData>
	{
		private final CompositionActionData compositionActionData;

		public CompositionErrorPageable(final SearchQueryData searchQueryData)
		{
			super(searchQueryData);
			compositionActionData = getCompositionActionData(searchQueryData);
		}

		@Override
		protected PagedDataHubResponse<ErrorData> getPagedData(final int pageNumber, final int pageSize)
		{
			if (compositionActionData != null)
			{
				final PagedDataHubResponse<ErrorData> errorsPage = poolActionClient.getCompositionErrors(compositionActionData.getActionId(), pageNumber, pageSize);
				return new PagedDataHubResponse<>(errorsPage.getTotalCount(), errorsPage.getItems());
			}
			else
			{
				return new PagedDataHubResponse<>(0, new ArrayList<>());
			}
		}

		private CompositionActionData getCompositionActionData(final SearchQueryData searchQueryData)
		{
			final SearchAttributeDescriptor descriptor = new SearchAttributeDescriptor(CompositionActionConstants.COMPOSITION_ACTION_ATTRIBUTE_DESCRIPTOR, 0);
			if (searchQueryData.getAttributes().contains(descriptor))
			{
				final CompositionActionData actionData = (CompositionActionData) searchQueryData.getAttributeValue(descriptor);
				if (actionData != null)
				{
					return actionData;
				}
			}
			return null;
		}
	}
}
