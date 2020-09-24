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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.facades;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;


/**
 * Facade to perform operations on search configuration model.
 */
public interface AsSearchConfigurationFacade
{
	/**
	 * Gets or creates configurable search configuration context.
	 *
	 * @param navigationContext
	 *           - The navigation context.
	 * @param searchContext
	 *           - The search context.
	 *
	 * @return {@link AbstractAsConfigurableSearchConfigurationModel}
	 */
	AbstractAsConfigurableSearchConfigurationModel getOrCreateSearchConfiguration(final NavigationContextData navigationContext,
			final SearchContextData searchContext);

}
