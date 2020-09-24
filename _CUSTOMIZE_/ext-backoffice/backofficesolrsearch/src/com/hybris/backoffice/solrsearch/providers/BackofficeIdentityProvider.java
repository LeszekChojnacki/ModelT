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
package com.hybris.backoffice.solrsearch.providers;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.provider.IdentityProvider;

import java.io.Serializable;


public class BackofficeIdentityProvider implements IdentityProvider<ItemModel>, Serializable
{
	@Override
	public String getIdentifier(final IndexConfig indexConfig, final ItemModel model)
	{
		return model.getPk().getLongValueAsString();
	}
}
