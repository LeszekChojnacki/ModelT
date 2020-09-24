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
package de.hybris.platform.adaptivesearchbackoffice.facades;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;


/**
 * Facade to perform operations on search profile context
 */
public interface AsSearchProfileContextFacade
{
	/**
	 * Creates a search profile context from the navigation context.
	 *
	 * @param navigationContext
	 *           - the navigation context
	 *
	 * @return {@link AsSearchProfileContext}
	 */
	AsSearchProfileContext createSearchProfileContext(NavigationContextData navigationContext);

	/**
	 * Creates a search profile context from the navigation context and search context.
	 *
	 * @param navigationContext
	 *           - the navigation context
	 * @param searchContext
	 *           - the search context
	 *
	 * @return {@link AsSearchProfileContext}
	 */
	AsSearchProfileContext createSearchProfileContext(NavigationContextData navigationContext, SearchContextData searchContext);
}
