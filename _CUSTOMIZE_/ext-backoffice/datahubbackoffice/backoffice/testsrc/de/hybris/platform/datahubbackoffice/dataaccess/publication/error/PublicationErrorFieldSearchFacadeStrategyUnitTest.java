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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import de.hybris.platform.datahubbackoffice.dataaccess.publication.PublicationConstants;

import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.PublicationClient;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.publication.TargetSystemPublicationData;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublicationErrorFieldSearchFacadeStrategyUnitTest
{
	private static final long PUBLICATION_ID = 2L;
	private static final int PAGE_NUMBER = 0;
	private static final int PAGE_SIZE = 10;

	@Mock
	private PublicationClient publicationClient;
	@InjectMocks
	private PublicationErrorFieldSearchFacadeStrategy strategy;

	@Test
	public void testSearchWhenNoTargetSystemDataIsProvided()
	{
		final Pageable<ErrorData> pageable = strategy.search(toSearchData(null));

		assertThat(pageable.getCurrentPage()).isEmpty();
	}

	@Test
	public void testSearchWhenTargetSystemPublicationIsSuccessful()
	{
		final TargetSystemPublicationData publicationData = successfulPublicationData();

		final Pageable<ErrorData> pageable = strategy.search(toSearchData(publicationData));

		assertThat(pageable.getCurrentPage()).isEmpty();
	}

	private TargetSystemPublicationData successfulPublicationData()
	{
		return publicationData("COMPLETE");
	}

	@Test
	public void testSearchWhenTargetSystemPublicationIsCompletedWithErrors()
	{
		final TargetSystemPublicationData publicationData = errorPublicationData(new ErrorData(), new ErrorData());

		final Pageable<ErrorData> pageable = strategy.search(toSearchData(publicationData));

		assertThat(pageable.getTotalCount()).isEqualTo(2);
	}

	private TargetSystemPublicationData errorPublicationData(final ErrorData... errors)
	{
		return publicationData("COMPLETE_W_ERRORS", errors);
	}

	@Test
	public void testSearchWhenTargetSystemPublicationIsFailure()
	{
		final TargetSystemPublicationData publicationData = failurePublicationData();

		final Pageable<ErrorData> pageable = strategy.search(toSearchData(publicationData));

		assertThat(pageable.getCurrentPage()).isEmpty();
	}

	private TargetSystemPublicationData failurePublicationData()
	{
		return publicationData("FAILURE");
	}

	private TargetSystemPublicationData publicationData(final String status, final ErrorData... errors)
	{
		final TargetSystemPublicationData data = publicationDataWithStatus(status);
		final PagedDataHubResponse<Object> response = new PagedDataHubResponse<>(errors.length, Arrays.asList((Object[])errors));
		doReturn(response).when(publicationClient).getPublicationErrors(data.getPublicationId(), PAGE_NUMBER, PAGE_SIZE);
		return data;
	}

	private TargetSystemPublicationData publicationDataWithStatus(final String status)
	{
		final TargetSystemPublicationData data = new TargetSystemPublicationData();
		data.setPublicationId(PUBLICATION_ID);
		data.setStatus(status);
		return data;
	}

	private AdvancedSearchQueryData toSearchData(final TargetSystemPublicationData pubData)
	{
		final SearchQueryCondition condition = new SearchQueryCondition();
		condition.setDescriptor(new SearchAttributeDescriptor(PublicationConstants.TARGET_SYSTEM_PUBLICATION_ATTRIBUTE_DESCRIPTOR, 0));
		condition.setOperator(null);
		condition.setValue(pubData);

		return new AdvancedSearchQueryData.Builder(PublicationConstants.PUBLICATION_ERROR_TYPE_CODE)
				.conditions(condition)
				.build();
	}
}
