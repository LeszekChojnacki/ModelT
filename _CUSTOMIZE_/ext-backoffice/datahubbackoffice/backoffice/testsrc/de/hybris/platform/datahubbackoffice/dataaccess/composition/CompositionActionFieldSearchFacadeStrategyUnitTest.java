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
package de.hybris.platform.datahubbackoffice.dataaccess.composition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.client.PoolActionClient;
import com.hybris.datahub.dto.event.CompositionActionData;
import com.hybris.datahub.dto.filter.CompositionFilterDto;
import com.hybris.datahub.runtime.domain.CompositionActionStatusType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class CompositionActionFieldSearchFacadeStrategyUnitTest
{
	@Mock
	private PoolActionClient poolActionClient;
	@InjectMocks
	private CompositionActionFieldSearchFacadeStrategy strategy;

	@Test
	public void testSearchWithOneStatusFilter()
	{
		final SearchQueryData searchQueryData = mock(SearchQueryData.class);
		final SearchAttributeDescriptor statusAttributeDescriptor = mock(SearchAttributeDescriptor.class);

		when(statusAttributeDescriptor.getAttributeName()).thenReturn("status");
		when(searchQueryData.getAttributes()).thenReturn(Sets.newHashSet(statusAttributeDescriptor));
		when(searchQueryData.getAttributeValue(statusAttributeDescriptor)).thenReturn(CompositionActionStatusType.COMPLETE_W_ERRORS.name());

		final ArgumentCaptor<CompositionFilterDto> filterCaptor = ArgumentCaptor.forClass(CompositionFilterDto.class);

		final Pageable<CompositionActionData> pageable = strategy.search(searchQueryData);
		pageable.getCurrentPage();

		verify(poolActionClient).getAllCompositions(anyInt(), anyInt(), filterCaptor.capture());

		assertThat(filterCaptor.getValue().getStatuses()).hasSize(1)
														 .containsExactly(CompositionActionStatusType.COMPLETE_W_ERRORS.name());
	}
}