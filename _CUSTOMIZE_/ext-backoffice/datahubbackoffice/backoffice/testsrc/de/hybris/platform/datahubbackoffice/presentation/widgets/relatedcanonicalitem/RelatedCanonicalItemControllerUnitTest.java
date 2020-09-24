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
package de.hybris.platform.datahubbackoffice.presentation.widgets.relatedcanonicalitem;

import static de.hybris.platform.datahubbackoffice.dto.item.ErrorDataBuilder.errorData;
import static de.hybris.platform.datahubbackoffice.dto.item.ItemDataBuilder.itemData;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import de.hybris.platform.datahubbackoffice.dataaccess.search.strategy.DatahubCanonicalObjectFacadeStrategy;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.item.ItemData;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DeclaredInput(value = RelatedCanonicalItemController.SOCKET_IN_ERROR, socketType = ErrorData.class)
public class RelatedCanonicalItemControllerUnitTest extends AbstractWidgetUnitTest<RelatedCanonicalItemController>
{
	@InjectMocks
	private final RelatedCanonicalItemController controller = new RelatedCanonicalItemController();
	@Mock
	private DatahubCanonicalObjectFacadeStrategy objectFacadeStrategy;

	@Test
	public void testGetRelatedCanonicalItem_ErrorDataWasNotSelected()
	{
		controller.getRelatedCanonicalItem(null);

		assertNoSocketOutputInteractions(RelatedCanonicalItemController.SOCKET_OUT_CANONICAL_ITEM);
	}

	@Test
	public void testGetRelatedCanonicalItem_ErrorDataHasNullItemId()
	{
		final ErrorData error = errorData().forItemId(null).build();

		controller.getRelatedCanonicalItem(error);

		assertNoSocketOutputInteractions(RelatedCanonicalItemController.SOCKET_OUT_CANONICAL_ITEM);
	}

	@Test
	public void testGetRelatedCanonicalItem_CanonicalItemNotFound() throws ObjectNotFoundException
	{
		final ErrorData error = errorData().forItemId(1L).build();
		givenCanonicalItemsDoNotExist();

		controller.getRelatedCanonicalItem(error);

		assertNoSocketOutputInteractions(RelatedCanonicalItemController.SOCKET_OUT_CANONICAL_ITEM);
	}

	@Test
	public void testGetRelatedCanonicalItem_itemFound() throws ObjectNotFoundException
	{
		final ItemData item = givenCanonicalItemExists(itemData().withId(1L).build());
		final ErrorData error = errorData().forItem(item).build();

		controller.getRelatedCanonicalItem(error);

		assertSocketOutput(RelatedCanonicalItemController.SOCKET_OUT_CANONICAL_ITEM, item);
	}

	private ItemData givenCanonicalItemExists(final ItemData item) throws ObjectNotFoundException
	{
		doReturn(item).when(objectFacadeStrategy).load(item.getId());
		return item;
	}

	private void givenCanonicalItemsDoNotExist() throws ObjectNotFoundException
	{
		doThrow(new ObjectNotFoundException("")).when(objectFacadeStrategy).load(anyLong());
	}

	@Override
	protected RelatedCanonicalItemController getWidgetController()
	{
		return controller;
	}
}