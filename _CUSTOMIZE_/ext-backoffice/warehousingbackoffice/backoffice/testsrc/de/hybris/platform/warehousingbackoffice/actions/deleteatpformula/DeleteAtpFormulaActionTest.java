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
 *
 */
package de.hybris.platform.warehousingbackoffice.actions.deleteatpformula;

import com.google.common.collect.Sets;
import com.hybris.cockpitng.actions.ActionContext;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.model.AtpFormulaModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DeleteAtpFormulaActionTest
{
	@InjectMocks
	private DeleteAtpFormulaAction action;
	@Mock
	private ActionContext context;
	@Mock
	private AtpFormulaModel atpFormula;
	@Mock
	private BaseStoreModel baseStore;

	@Before
	public void setUp()
	{
		when(context.getData()).thenReturn(atpFormula);
	}

	@Test
	public void returnTrueInstanceOfAtpFormulaNoBaseStore()
	{
		//When
		final boolean result = action.canPerform(context);

		//Then
		assertTrue(result);
	}

	@Test
	public void returnFalseNotInstanceOfAtpFormula()
	{
		//Given
		when(context.getData()).thenReturn("test");

		//When
		final boolean result = action.canPerform(context);

		//Then
		assertFalse(result);
	}

	@Test
	public void returnFalseNullAtpFormula()
	{
		//Given
		when(context.getData()).thenReturn(null);

		//When
		final boolean result = action.canPerform(context);

		//Then
		assertFalse(result);
	}

	@Test
	public void returnFalseAtpFormulaWithBaseStore()
	{
		//Given
		when(atpFormula.getBaseStores()).thenReturn(Sets.newHashSet(baseStore));

		//When
		final boolean result = action.canPerform(context);

		//Then
		assertFalse(result);
	}

}
