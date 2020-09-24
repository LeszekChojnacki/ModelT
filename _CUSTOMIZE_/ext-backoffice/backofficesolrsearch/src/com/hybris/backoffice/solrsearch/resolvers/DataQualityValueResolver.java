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
package com.hybris.backoffice.solrsearch.resolvers;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.proxy.DataQualityCalculationServiceProxy;


/**
 * Provides list of FieldValue for data quality for given domainGroupId for product.
 */
public class DataQualityValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(DataQualityValueResolver.class);

	private DataQualityCalculationServiceProxy dataQualityCalculationServiceProxy;
	private String domainGroupId;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{

		// get value for quality
		final Optional<Double> qualityIndex = dataQualityCalculationServiceProxy.calculate(model, getDomainGroupId());

		if (qualityIndex.isPresent())
		{
			// round the result
			document.addField(indexedProperty,
					BigDecimal.valueOf(qualityIndex.get()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
					resolverContext.getFieldQualifier());
		}
		else
		{
			LOG.error("Could not calculate quality for {} and domanGroupId {}!", model, domainGroupId);
		}
	}

	public void setDataQualityCalculationServiceProxy(final DataQualityCalculationServiceProxy dataQualityCalculationServiceProxy)
	{
		this.dataQualityCalculationServiceProxy = dataQualityCalculationServiceProxy;
	}

	public DataQualityCalculationServiceProxy getDataQualityCalculationServiceProxy()
	{
		return dataQualityCalculationServiceProxy;
	}

	@Required
	public void setDomainGroupId(final String domainGroupId)
	{
		this.domainGroupId = domainGroupId;
	}

	public String getDomainGroupId()
	{
		return domainGroupId;
	}
}
