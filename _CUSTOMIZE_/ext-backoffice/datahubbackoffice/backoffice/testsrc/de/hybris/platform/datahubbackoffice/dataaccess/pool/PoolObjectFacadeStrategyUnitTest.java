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
package de.hybris.platform.datahubbackoffice.dataaccess.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectCreationException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.datahub.client.DataPoolClient;
import com.hybris.datahub.dto.pool.PoolData;

@RunWith(MockitoJUnitRunner.class)
public class PoolObjectFacadeStrategyUnitTest
{
	private static final String POOL_TYPE_CODE = "Datahub_Pool";

	@Mock
	private DataPoolClient poolClient;

	@InjectMocks
	private final PoolObjectFacadeStrategy strategy = new PoolObjectFacadeStrategy();

	@Test
	public void handlesPoolDataObjects()
	{
		final boolean res = strategy.canHandle(new PoolData());
		assertThat(res).isTrue();
	}

	@Test
	public void handlesPoolDataTypeCode()
	{
		final boolean res = strategy.canHandle(POOL_TYPE_CODE);
		assertThat(res).isTrue();
	}

	@Test
	public void doesNotHandleNullObjects()
	{
		final boolean res = strategy.canHandle(null);
		assertThat(res).isFalse();
	}

	@Test
	public void doesNotHandleObjectsOtherThanPoolData()
	{
		final boolean res = strategy.canHandle(new Object());
		assertThat(res).isFalse();
	}

	@Test
	public void createsANewObjectEveryTimeItIsCalled() throws ObjectCreationException
	{
		// create two pools
		final PoolData pool1 = strategy.create("", null);
		final PoolData pool2 = strategy.create("", null);

		// both pools are created and they are different instances
		assertThat(pool1).isNotEqualTo(pool2);
	}

	@Test
	public void savesValidPoolData() throws ObjectSavingException
	{
		// client successfully creates pool for the requested pool data
		final PoolData validData = new PoolData();
		when(poolClient.createDataPool(validData)).thenReturn(new PoolData());

		// valid pool data was used
		final PoolData saved = strategy.save(validData, null);

		// pool data received from the client is returned
		assertThat(saved).isNotEqualTo(validData);
	}

	@Test
	public void reportsErrorWhenTheSpecifiedPoolDataAreInvalid()
	{
		final PoolData invalidData = new PoolData();
		when(poolClient.createDataPool(invalidData)).thenReturn(null);

		// saving pool with empty name
		assertThatThrownBy(() -> strategy.save(invalidData, null))
				.isInstanceOf(ObjectSavingException.class)
				.hasMessageContaining("datahub.pool.creation.error");
	}
}
