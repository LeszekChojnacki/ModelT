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
package de.hybris.platform.datahubbackoffice.dataaccess.publication.error;

import de.hybris.platform.datahubbackoffice.dataaccess.DataHubPageable;
import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.dataaccess.publication.PublicationConstants;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.PublicationClient;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.publication.TargetSystemPublicationData;

import java.util.ArrayList;

public class PublicationErrorFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<ErrorData>
{
	private PublicationClient publicationClient;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return PublicationConstants.PUBLICATION_ERROR_TYPE_CODE.equals(typeCode);
	}

	@Override
	public Pageable<ErrorData> search(final SearchQueryData searchQueryData)
	{
		return new PublicationErrorPageable(searchQueryData);
	}

	public void setPublicationClient(final PublicationClient publicationClient)
	{
		this.publicationClient = publicationClient;
	}

	private class PublicationErrorPageable extends DataHubPageable<ErrorData>
	{
		private final TargetSystemPublicationData targetSystemPublicationData;

		public PublicationErrorPageable(final SearchQueryData searchQueryData)
		{
			super(searchQueryData);
			targetSystemPublicationData = getTargetSystemPublication(searchQueryData);
		}

		@Override
		protected PagedDataHubResponse<ErrorData> getPagedData(final int pageNumber, final int pageSize)
		{
			return targetSystemPublicationData != null
					? requestErrorsFromDataHub(pageNumber, pageSize) : emptyResponse();
		}

		private PagedDataHubResponse<ErrorData> requestErrorsFromDataHub(final int pageNumber, final int pageSize)
		{
			final PagedDataHubResponse<ErrorData> errorsPage = publicationClient.getPublicationErrors(
					targetSystemPublicationData.getPublicationId(), pageNumber, pageSize);
			return new PagedDataHubResponse<>(errorsPage.getTotalCount(), errorsPage.getItems());
		}

		private PagedDataHubResponse<ErrorData> emptyResponse()
		{
			return new PagedDataHubResponse<>(0, new ArrayList<>());
		}

		private TargetSystemPublicationData getTargetSystemPublication(final SearchQueryData searchQueryData)
		{
			final SearchAttributeDescriptor descriptor = new SearchAttributeDescriptor(
					PublicationConstants.TARGET_SYSTEM_PUBLICATION_ATTRIBUTE_DESCRIPTOR, 0);
			return searchQueryData.getAttributes().contains(descriptor)
					? (TargetSystemPublicationData) searchQueryData.getAttributeValue(descriptor) : null;
		}
	}
}
