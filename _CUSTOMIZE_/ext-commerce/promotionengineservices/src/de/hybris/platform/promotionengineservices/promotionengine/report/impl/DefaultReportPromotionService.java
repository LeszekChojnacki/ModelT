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
package de.hybris.platform.promotionengineservices.promotionengine.report.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.ReportPromotionService;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResults;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 * Default implementation of @{link ReportPromotionService}
 */
public class DefaultReportPromotionService implements ReportPromotionService
{
	private Converter<AbstractOrderModel, PromotionEngineResults> promotionEngineResultsConverter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PromotionEngineResults report(AbstractOrderModel order)
	{
		checkArgument(nonNull(order),"Order cannot be null");
		return getPromotionEngineResultsConverter().convert(order);
	}

	protected Converter<AbstractOrderModel, PromotionEngineResults> getPromotionEngineResultsConverter()
	{
		return promotionEngineResultsConverter;
	}

	@Required
	public void setPromotionEngineResultsConverter(final Converter<AbstractOrderModel, PromotionEngineResults> promotionEngineResultsConverter)
	{
		this.promotionEngineResultsConverter = promotionEngineResultsConverter;
	}
}
