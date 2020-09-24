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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.datahub.dto.pool.PoolData;

public class PoolTypeFacadeStrategyUnitTest
{
	private static final String POOL_TYPE_CODE = "Datahub_Pool";

	private PoolTypeFacadeStrategy strategy;
	private DataType type;

	@Before
	public void setUp() throws TypeNotFoundException
	{
		strategy = new PoolTypeFacadeStrategy();
		type = strategy.load(POOL_TYPE_CODE);
	}

	@Test
	public void handlesDataHubPoolTypeCode()
	{
		final boolean res = strategy.canHandle(POOL_TYPE_CODE);
		assertThat(res).isTrue();
	}

	@Test
	public void handlesPoolData()
	{
		final boolean res = strategy.canHandle(PoolData.class.getName());
		assertThat(res).isTrue();
	}

	@Test
	public void returnsTypeCodeForPoolDataObjects()
	{
		final String typeCode = strategy.getType(new PoolData());
		assertThat(typeCode).isEqualTo(POOL_TYPE_CODE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionForObjectsOtherThanPoolData()
	{
		strategy.getType(new Object());
	}

	@Test
	public void poolTypeIsNotAtomic()
	{
		final boolean res = type.isAtomic();
		assertThat(res).isFalse();
	}

	@Test
	public void poolTypeIsSearchable()
	{
		final boolean res = type.isSearchable();
		assertThat(res).isTrue();
	}

	@Test
	public void poolTypeHasAttributesDefinedAsExpected()
	{
		final Map<String, Pair<DataType, Boolean>> map = new LinkedHashMap<>();
		map.put("poolId", new ImmutablePair<>(DataType.LONG, false));
		map.put("poolName", new ImmutablePair<>(DataType.STRING, true));
		map.put("type", new ImmutablePair<>(DataType.STRING, false));
		map.put("compositionStrategy", new ImmutablePair<>(DataType.STRING, false));
		map.put("publicationStrategy", new ImmutablePair<>(DataType.STRING, false));
		map.put("deletable", new ImmutablePair<>(DataType.BOOLEAN, true));

		map.forEach((attrName, pair) -> {
			final DataAttribute attr = type.getAttribute(attrName);

			assertThat(attr.isSearchable()).isTrue();
			assertThat(attr.getValueType()).isEqualTo(pair.getLeft());
			assertThat(attr.isWritable()).isEqualTo(pair.getRight());
		});
	}
}
