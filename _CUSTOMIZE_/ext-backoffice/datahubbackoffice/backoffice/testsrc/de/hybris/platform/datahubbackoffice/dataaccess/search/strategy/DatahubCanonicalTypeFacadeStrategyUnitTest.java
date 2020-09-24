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
package de.hybris.platform.datahubbackoffice.dataaccess.search.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.hybris.platform.datahubbackoffice.dataaccess.metadata.DataHubTypeService;

import com.hybris.datahub.dto.item.ItemData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatahubCanonicalTypeFacadeStrategyUnitTest
{
	private static final String EXISTS = "exists";
	private static final String DOES_NOT_EXIST = "doesNotExist";
	private static final String GENERATES_EXCEPTION = "generatesException";
	private static final String ITEM_DATA = ItemData.class.getName();

	@Mock
	private DataHubTypeService canonicalTypes;
	@InjectMocks
	private DatahubCanonicalTypeFacadeStrategy datahubCanonicalTypeFacadeStrategy;

	@Before
	public void setUp()
	{
		when(canonicalTypes.exists(EXISTS)).thenReturn(Boolean.TRUE);
		when(canonicalTypes.exists(DOES_NOT_EXIST)).thenReturn(Boolean.FALSE);
		doThrow(Exception.class).when(canonicalTypes).exists(GENERATES_EXCEPTION);
	}

	@Test
	public void testCanHandleCanonicalTypeExists()
	{
		final boolean canHandle = datahubCanonicalTypeFacadeStrategy.canHandle(EXISTS);

		assertThat(canHandle).isTrue();
	}

	@Test
	public void testCanHandleCanonicalTypeItemData()
	{
		final boolean canHandle = datahubCanonicalTypeFacadeStrategy.canHandle(ITEM_DATA);

		assertThat(canHandle).isTrue();
	}

	@Test
	public void testCanHandleCanonicalTypeDoesNotExist()
	{
		final boolean canHandle = datahubCanonicalTypeFacadeStrategy.canHandle(DOES_NOT_EXIST);

		assertThat(canHandle).isFalse();
	}

	@Test
	public void testCanHandleCanonicalTypeThrowsException()
	{
		final boolean canHandle = datahubCanonicalTypeFacadeStrategy.canHandle(GENERATES_EXCEPTION);

		assertThat(canHandle).isFalse();
	}
}