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

import static com.hybris.datahub.runtime.domain.PublicationActionStatusType.COMPLETE_W_ERRORS;
import static com.hybris.datahub.runtime.domain.PublicationActionStatusType.FAILURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import de.hybris.platform.datahubbackoffice.dataaccess.publication.PublicationConstants;
import de.hybris.platform.datahubbackoffice.dataaccess.publication.PublicationsInErrorFieldSearchFacadeStrategy;

import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.datahub.client.PublicationClient;
import com.hybris.datahub.dto.filter.TargetSystemPublicationFilterDto;
import com.hybris.datahub.dto.metadata.TargetSystemData;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublicationsInErrorFieldSearchStrategyUnitTest
{
	private static final String POOL_NAME = "testPool";
	private static final String TARGET_SYSTEM_NAME = "testTargetSystem";
	private static final String STATUS = "COMPLETE_W_ERRORS";
	private static final Date START_TIME = new Date();
	private static final Date END_TIME = new Date(System.currentTimeMillis() + 10000);
	private static final int PAGE_SIZE = 15;

	private PoolData pool;
	private TargetSystemData targetSystem;

	@Mock
	private PublicationClient client;
	@InjectMocks
	private PublicationsInErrorFieldSearchFacadeStrategy strategy;

	@Before
	public void setup()
	{
		pool = new PoolData();
		pool.setPoolName(POOL_NAME);
		targetSystem = new TargetSystemData();
		targetSystem.setTargetSystemName(TARGET_SYSTEM_NAME);
	}

	@Test
	public void testSearchWithNonEmptyCriteria()
	{
		searchWith(toSearchData(COMPLETE_W_ERRORS.getCode(), pool, targetSystem, START_TIME, END_TIME));

		final TargetSystemPublicationFilterDto dto = interceptClientRequest();
		assertThat(dto.getPoolName()).isEqualTo(POOL_NAME);
		assertThat(dto.getTargetSystemName()).isEqualTo(TARGET_SYSTEM_NAME);
		assertThat(dto.getStartDate()).isEqualTo(START_TIME);
		assertThat(dto.getEndDate()).isEqualTo(END_TIME);
		assertThat(dto.getStatuses()[0]).isEqualTo(STATUS);
		assertThat(dto.getStatuses()).containsExactly(COMPLETE_W_ERRORS.getCode());
	}

	@Test
	public void testSearchWithEmptyCriteria()
	{
		searchWith(toSearchData(null, null, null, null, null));

		final TargetSystemPublicationFilterDto dto = interceptClientRequest();
		assertThat(dto.getPoolName()).isNull();
		assertThat(dto.getTargetSystemName()).isNull();
		assertThat(dto.getStartDate()).isNull();
		assertThat(dto.getEndDate()).isNull();
		assertThat(dto.getStatuses()).containsExactly(COMPLETE_W_ERRORS.getCode(), FAILURE.getCode());
	}

	@Test
	public void testSearchWithoutSpecifyingCriteriaThroughSettingNoStatus()
	{
		searchWith(toSearchData("", null, null, null, null));

		final TargetSystemPublicationFilterDto dto = interceptClientRequest();
		assertThat(dto.getPoolName()).isNull();
		assertThat(dto.getTargetSystemName()).isNull();
		assertThat(dto.getStartDate()).isNull();
		assertThat(dto.getEndDate()).isNull();
		assertThat(dto.getStatuses()).containsExactly(COMPLETE_W_ERRORS.getCode(), FAILURE.getCode());
	}

	private AdvancedSearchQueryData toSearchData(final String status, final PoolData pool, final TargetSystemData targetSystem, final Date startTime, final Date endTime)
	{
		return new AdvancedSearchQueryData.Builder(PublicationConstants.PUBLICATIONS_IN_ERROR_TYPE_CODE)
				.conditions(
						toCondition("status", ValueComparisonOperator.EQUALS, status, 0),
						toCondition("pool", ValueComparisonOperator.EQUALS, pool, 1),
						toCondition("targetSystem", ValueComparisonOperator.EQUALS, targetSystem, 2),
						toCondition("startTime", startTime != null ? ValueComparisonOperator.GREATER : null, startTime, 3),
						toCondition("endTime", endTime != null ? ValueComparisonOperator.LESS_OR_EQUAL : null, endTime, 4)
				).pageSize(PAGE_SIZE)
				.build();
	}

	private void searchWith(final AdvancedSearchQueryData data)
	{
		strategy.search(data).getCurrentPage();
	}

	private TargetSystemPublicationFilterDto interceptClientRequest()
	{
		final ArgumentCaptor<TargetSystemPublicationFilterDto> captor = ArgumentCaptor.forClass(TargetSystemPublicationFilterDto.class);
		verify(client).getTargetSystemPublications(Matchers.eq(0), Matchers.eq(PAGE_SIZE), captor.capture());
		return captor.getValue();
	}

	private SearchQueryCondition toCondition(final String attr, final ValueComparisonOperator op, final Object val, final int pos)
	{
		return new SearchQueryCondition(new SearchAttributeDescriptor(attr, pos), val, op);
	}

	public void addAttribute(final Map<SearchAttributeDescriptor, Map.Entry<Object, ValueComparisonOperator>> attributeMap,
			final String attrName, final Object attrValue, final ValueComparisonOperator comparisonOperator, final int attrOrder)
	{
		if (attrValue != null)
		{
			attributeMap.put(new SearchAttributeDescriptor(attrName, attrOrder),
					new ImmutablePair<>(attrValue, comparisonOperator));
		}
	}
}
