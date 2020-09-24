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
package com.hybris.backoffice.proxy;

import java.util.Optional;


/**
 * Service proxies {@link com.hybris.cockpitng.dataquality.DataQualityCalculationService} for retrieving Object's data
 * coverage.
 */
public interface DataQualityCalculationServiceProxy
{

	/**
	 * Calculates the coverage of the given <code>object<code>.
	 * The <code>domainId<code> can be used to distinguish between multiple
	 * coverage calculation strategies per domain (e.g. text translation coverage,
	 * print related coverage etc.)
	 *
	 * &#64;param object   the object to calculate the coverage for
	 * &#64;param domainId the domain Id if applicable
	 * @return the <code>Optional&lt;DataQuality&gt;</code> object with cumulated coverage information or
	 *         <code>Optional.empty()</code> value if no strategy was registered for given object and domain ID
	 */
	Optional<Double> calculate(Object object, String domainId);

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
	Optional<Double> calculate(Object object, String templateCode, String domainId);
}
