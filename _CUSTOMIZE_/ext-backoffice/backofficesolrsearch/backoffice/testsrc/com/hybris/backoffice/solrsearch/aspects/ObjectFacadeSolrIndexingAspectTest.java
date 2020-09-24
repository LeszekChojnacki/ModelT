/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.backoffice.solrsearch.aspects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hybris.backoffice.solrsearch.events.SolrIndexSynchronizationStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectAccessException;


public class ObjectFacadeSolrIndexingAspectTest
{

	private static final String PRODUCT_TYPE = "Product";
	private static final PK CHANGED_PRODUCT_PK = PK.fromLong(1L);
	private static final PK FAILED_PRODUCT_PK = PK.fromLong(2L);

	@InjectMocks
	private ObjectFacadeSolrIndexingAspect solrIndexingAspect;
	@Mock
	private SolrIndexSynchronizationStrategy synchronizationStrategy;
	@Mock
	private ModelService modelService;
	@Mock
	private ProductModel changedProduct;
	@Mock
	private ProductModel failedProduct;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		when(changedProduct.getPk()).thenReturn(CHANGED_PRODUCT_PK);
		when(modelService.getModelType(changedProduct)).thenReturn(PRODUCT_TYPE);

		when(failedProduct.getPk()).thenReturn(FAILED_PRODUCT_PK);
		when(modelService.getModelType(failedProduct)).thenReturn(PRODUCT_TYPE);
	}

	@Test
	public void shouldUpdateIndexForChangedModel()
	{
		solrIndexingAspect.updateChanged(new JoinPointStub(changedProduct), null);

		ArgumentCaptor<String> typeCodeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> pkCaptor = ArgumentCaptor.forClass(List.class);

		verify(synchronizationStrategy).updateItems(typeCodeCaptor.capture(), pkCaptor.capture());

		assertThat(typeCodeCaptor.getValue()).isEqualToIgnoringCase(PRODUCT_TYPE);
		assertThat(pkCaptor.getValue()).containsExactly(CHANGED_PRODUCT_PK);
	}

	@Test
	public void shouldCleanIndexForRemovedModel()
	{
		solrIndexingAspect.updateRemoved(new JoinPointStub(changedProduct), null);

		ArgumentCaptor<String> typeCodeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> pkCaptor = ArgumentCaptor.forClass(List.class);

		verify(synchronizationStrategy).removeItems(typeCodeCaptor.capture(), pkCaptor.capture());

		assertThat(typeCodeCaptor.getValue()).isEqualToIgnoringCase(PRODUCT_TYPE);
		assertThat(pkCaptor.getValue()).containsExactly(CHANGED_PRODUCT_PK);
	}

	@Test
	public void shouldUpdateIndexForChangedModels()
	{
		final ObjectFacadeOperationResult result = new ObjectFacadeOperationResult();
		result.addFailedObject(failedProduct, new ObjectAccessException("", new RuntimeException()));

		solrIndexingAspect.updateChanged(new JoinPointStub(Collections.singletonList(changedProduct)), result);

		ArgumentCaptor<String> typeCodeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> pkCaptor = ArgumentCaptor.forClass(List.class);

		verify(synchronizationStrategy).updateItems(typeCodeCaptor.capture(), pkCaptor.capture());

		assertThat(typeCodeCaptor.getValue()).isEqualToIgnoringCase(PRODUCT_TYPE);
		assertThat(pkCaptor.getValue()).containsExactly(CHANGED_PRODUCT_PK);
	}

	static class JoinPointStub implements JoinPoint
	{
		private final Object[] args;

		public JoinPointStub(final Object...args)
		{
			this.args = args;
		}

		@Override
		public String toShortString()
		{
			return null;
		}

		@Override
		public String toLongString()
		{
			return null;
		}

		@Override
		public Object getThis()
		{
			return null;
		}

		@Override
		public Object getTarget()
		{
			return null;
		}

		@Override
		public Object[] getArgs()
		{
			return args;
		}

		@Override
		public Signature getSignature()
		{
			return null;
		}

		@Override
		public SourceLocation getSourceLocation()
		{
			return null;
		}

		@Override
		public String getKind()
		{
			return null;
		}

		@Override
		public StaticPart getStaticPart()
		{
			return null;
		}
	}
}