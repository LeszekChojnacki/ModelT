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
package de.hybris.platform.solrfacetsearch.provider;


/**
 * Marker interface for value providers that use a {@link QualifierProvider}.
 */
public interface QualifierProviderAware
{
	/**
	 * Returns the qualifier provider.
	 *
	 * @return the qualifier provider.
	 */
	QualifierProvider getQualifierProvider();

	/**
	 * Sets the qualifier provider.
	 *
	 * @param qualifierProvider
	 *           - the qualifier provider
	 */
	void setQualifierProvider(QualifierProvider qualifierProvider);
}
