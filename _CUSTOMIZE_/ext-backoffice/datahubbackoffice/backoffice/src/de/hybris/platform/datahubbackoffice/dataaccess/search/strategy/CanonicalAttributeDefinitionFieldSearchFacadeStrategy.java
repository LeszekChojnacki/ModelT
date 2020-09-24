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

import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.client.CanonicalItemClassClient;
import com.hybris.datahub.dto.metadata.CanonicalAttributeData;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;

public class CanonicalAttributeDefinitionFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<CanonicalAttributeData>
{
	private static final String CANONICAL_PRODUCT_TYPE = "CanonicalProduct";
	private static final String DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE = "Datahub_AttributeDefinition";

	private CanonicalItemClassClient canonicalItemClassClient;

	@Override
	public boolean canHandle(final String code)
	{
		return DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE.equals(code);
	}

	@Override
	public Pageable<CanonicalAttributeData> search(final SearchQueryData searchQueryData)
	{
		final Collection<CanonicalAttributeData> collection = canonicalItemClassClient.getAttributes(CANONICAL_PRODUCT_TYPE);

		return new MyPageable(collection);
	}

	@Required
	public void setCanonicalItemClassClient(final CanonicalItemClassClient client)
	{
		canonicalItemClassClient = client;
	}

	private static class MyPageable implements Pageable<CanonicalAttributeData>
	{
		private Collection<CanonicalAttributeData> collection;

		MyPageable(final Collection<CanonicalAttributeData> collection)
		{
			this.collection = collection;
		}

		@Override
		public List<CanonicalAttributeData> getCurrentPage()
		{
			return Lists.newArrayList(collection);
		}

		@Override
		public void refresh()
		{
			// not implemented
		}

		@Override
		public List<CanonicalAttributeData> nextPage()
		{
			return Collections.emptyList();
		}

		@Override
		public List<CanonicalAttributeData> previousPage()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean hasNextPage()
		{
			return false;
		}

		@Override
		public boolean hasPreviousPage()
		{
			return false;
		}

		@Override
		public void setPageNumber(final int i)
		{
			// not implemented
		}

		@Override
		public int getPageSize()
		{
			return 20;
		}

		@Override
		public String getTypeCode()
		{
			return DATAHUB_CANONICAL_ATTRIBUTE_DEFINITION_TYPECODE;
		}

		@Override
		public List<CanonicalAttributeData> setPageSize(final int i)
		{
			return Collections.emptyList();
		}

		@Override
		public int getTotalCount()
		{
			return collection.size();
		}

		@Override
		public int getPageNumber()
		{
			return 0;
		}

		@Override
		public SortData getSortData()
		{
			return null;
		}

		@Override
		public void setSortData(final SortData sortData)
		{
			// not implemented
		}

		@Override
		public List<CanonicalAttributeData> getAllResults()
		{
			return Lists.newArrayList(collection);
		}
	}
}
