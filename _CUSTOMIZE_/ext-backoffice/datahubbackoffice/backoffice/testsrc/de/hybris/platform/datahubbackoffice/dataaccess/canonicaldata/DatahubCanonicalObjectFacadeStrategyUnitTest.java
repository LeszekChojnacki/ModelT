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
package de.hybris.platform.datahubbackoffice.dataaccess.canonicaldata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import de.hybris.platform.datahubbackoffice.dataaccess.search.strategy.DatahubCanonicalObjectFacadeStrategy;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.datahub.client.DataHubClientException;
import com.hybris.datahub.client.DataItemClient;
import com.hybris.datahub.dto.item.ItemData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatahubCanonicalObjectFacadeStrategyUnitTest
{
	private static final Long EXISTING_ITEM_ID = 1L;
	private static final Long NOT_EXISTING_ITEM_ID = 2L;
	private static final String ERROR_MESSAGE = "Not found";
	@InjectMocks
	private final DatahubCanonicalObjectFacadeStrategy strategy = new DatahubCanonicalObjectFacadeStrategy();
	@Mock
	private DataItemClient client;

	@Before
	public void setUp()
	{
		doReturn(new ItemData()).when(client).getCanonicalItem(EXISTING_ITEM_ID);
		doThrow(exception(ERROR_MESSAGE)).when(client).getCanonicalItem(NOT_EXISTING_ITEM_ID);
	}

	private DataHubClientException exception(final String msg)
	{
		return new DataHubClientException(msg);
	}

	@Test
	public void testLoadExistingItemByLongId() throws ObjectNotFoundException
	{
		final ItemData item = strategy.load(EXISTING_ITEM_ID);

		assertThat(item).isNotNull();
	}

	@Test
	public void testLoadExistingItemByStringId() throws ObjectNotFoundException
	{
		final ItemData item = strategy.load(EXISTING_ITEM_ID.toString(), null);

		assertThat(item).isNotNull();
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testLoadNonExistingItemByLongId() throws ObjectNotFoundException
	{
		strategy.load(NOT_EXISTING_ITEM_ID);
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testLoadNonExistingItemByStringId() throws ObjectNotFoundException
	{
		strategy.load(NOT_EXISTING_ITEM_ID.toString(), null);
	}

	@Test
	public void testNotFoundExceptionContainsMessage()
	{
		assertThatThrownBy(() -> strategy.load(NOT_EXISTING_ITEM_ID.toString(), null))
				.isInstanceOf(ObjectNotFoundException.class)
				.hasMessageContaining(ERROR_MESSAGE);
	}

	@Test
	public void testReload() throws ObjectNotFoundException
	{
		final ItemData original = new ItemData();
		original.setId(EXISTING_ITEM_ID);

		final Object reloaded = strategy.reload(original, null);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded).isNotSameAs(original);
	}

	@Test
	public void testReloadNullObject() throws ObjectNotFoundException
	{
		final Object reloaded = strategy.reload((Object)null, null);

		assertThat(reloaded).isNull();
	}
}
