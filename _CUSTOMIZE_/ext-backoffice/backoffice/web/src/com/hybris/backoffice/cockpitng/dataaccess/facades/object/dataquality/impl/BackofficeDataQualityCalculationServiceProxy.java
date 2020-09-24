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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.dataquality.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.proxy.DataQualityCalculationServiceProxy;
import com.hybris.cockpitng.dataquality.model.DataQuality;


/**
 * Service proxies {@link com.hybris.cockpitng.dataquality.DataQualityCalculationService} for retrieving Object's data
 * coverage.
 */
public class BackofficeDataQualityCalculationServiceProxy implements DataQualityCalculationServiceProxy
{

	private BackofficeDataQualityCalculationService backofficeDataQualityCalculationService;

	@Override
	public Optional<Double> calculate(final Object object, final String domainId)
	{
		final Optional<DataQuality> dataQuality = backofficeDataQualityCalculationService.calculate(object, domainId);
		if (dataQuality.isPresent())
		{
			return Optional.of(dataQuality.get().getDataQualityIndex());
		}
		return Optional.empty();
	}

	/**
	 * Calculates the coverage of the given <code>object<code>.
	 * The <code>domainId<code> can be used to distinguish between multiple
	 * coverage calculation strategies per domain (e.g. text translation coverage,
	 * print related coverage etc.)
	 *
	 * &#64;param object       the object to calculate the coverage for
	 * &#64;param templateCode the object template to be used for finding the proper calculation strategy
	 * &#64;param domainId     the domain Id if applicable
	 * @return the <code>Optional&lt;DataQuality&gt;</code> object with cumulated coverage information or
	 *         <code>Optional.empty()</code> if no strategy was registered for given object and domain ID
	 */
	@Override
	public Optional<Double> calculate(final Object object, final String templateCode, final String domainId)
	{
		final Optional<DataQuality> dataQuality = backofficeDataQualityCalculationService.calculate(object, templateCode, domainId);

		if (dataQuality.isPresent())
		{
			return Optional.of(dataQuality.get().getDataQualityIndex());
		}
		return Optional.empty();
	}

	@Required
	public void setBackofficeDataQualityCalculationService(
			final BackofficeDataQualityCalculationService backofficeDataQualityCalculationService)
	{
		this.backofficeDataQualityCalculationService = backofficeDataQualityCalculationService;
	}
}
