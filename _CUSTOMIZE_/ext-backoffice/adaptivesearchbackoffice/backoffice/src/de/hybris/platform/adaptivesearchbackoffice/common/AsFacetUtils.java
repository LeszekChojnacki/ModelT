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
package de.hybris.platform.adaptivesearchbackoffice.common;

import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractFacetConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;

import java.util.List;


/**
 * Contains helper methods for facet editors.
 */
public interface AsFacetUtils
{
	/**
	 * Checks wether a facet should be open or not.
	 *
	 * @param facet
	 *           - the facet
	 *
	 * @return <code>true</code> is the facet should be open, <code>false</code> otherwise
	 */
	boolean isOpen(AsFacetData facet);

	/**
	 * Updates the facet labels according to the language of the search context.
	 *
	 * @param navigationContext
	 *           - the navigation context
	 * @param searchContext
	 *           - the search context
	 * @param facets
	 *           - the facets
	 */
	void localizeFacets(final NavigationContextData navigationContext, final SearchContextData searchContext,
			final List<? extends AbstractFacetConfigurationEditorData> facets);
}
