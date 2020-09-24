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
package de.hybris.platform.datahubbackoffice.dataaccess;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;

public class PredefinableTypeCacheUnitTest
{
	private static final String TYPE1 = "type1";
	private static final String TYPE2 = "type2";

	private PredefinableTypeCache cache;

	@Before
	public void setUp()
	{
		cache = new PredefinableTypeCache();
	}

	@Test
	public void emptyAfterCreation()
	{
		final boolean res = cache.isEmpty();
		assertThat(res).isTrue();
	}

	@Test
	public void notEmptyAfterTypeAdded()
	{
		final PredefinableTypeCache res = cache.add(new DataType.Builder("code").build());

		assertThat(cache.isEmpty()).isFalse();
		assertThat(res.isEmpty()).isFalse();
	}

	@Test
	public void readsBackAddedType()
	{
		final DataType type = new DataType.Builder("code").build();
		final PredefinableTypeCache res = cache.add(type);

		assertThat(res.get("code")).isEqualTo(type);
	}

	@Test
	public void readsTypeThatHasNotBeenAddedYet()
	{
		final DataType type = cache.get("code");
		assertThat(type).isNull();
	}

	@Test
	public void notDefinedWhenWasNotAddedOrExplicitlyDefined()
	{
		final boolean res = cache.isDefined("code");
		assertThat(res).isFalse();
	}

	@Test
	public void typeIsDefinedAfterItHasBeenAdded()
	{
		final PredefinableTypeCache res = cache.add(new DataType.Builder("code").build());

		final boolean r = res.isDefined("code");
		assertThat(r).isTrue();
	}

	@Test
	public void defineTypeWithoutStoringItInTheCache()
	{
		final PredefinableTypeCache res = cache.define("code");

		final boolean r = res.isDefined("code");
		assertThat(r).isTrue();
	}

	@Test
	public void defineNullType()
	{
		final PredefinableTypeCache res = cache.define((Collection<String>) null);

		final boolean r = res.isEmpty();
		assertThat(r).isTrue();
	}

	@Test
	public void defineDoesNotChangePreviouslyAddedType()
	{
		final DataType type = new DataType.Builder("code").build();
		final PredefinableTypeCache res = cache.add(type);

		res.define("code");

		assertThat(res.isDefined("code")).isTrue();
		assertThat(res.get("code")).isEqualTo(type);
	}

	@Test
	public void addTypeAfterDefiningIt()
	{
		cache.define("code");
		final DataType type = new DataType.Builder("code").build();

		final PredefinableTypeCache res = cache.add(type);

		assertThat(res.isDefined("code")).isTrue();
		assertThat(res.get("code")).isEqualTo(type);
	}

	@Test
	public void defineMultipleTypesAtOnce()
	{
		final PredefinableTypeCache res = cache.define(TYPE1, TYPE2);

		assertThat(res.isDefined(TYPE1)).isTrue();
		assertThat(res.isDefined(TYPE2)).isTrue();
	}

	@Test
	public void retrievesAllDefinedCodes()
	{
		final PredefinableTypeCache res = cache.define(TYPE1, TYPE2);

		assertThat(res.definedCodes()).containsExactlyInAnyOrder(TYPE1, TYPE2);
	}
}
