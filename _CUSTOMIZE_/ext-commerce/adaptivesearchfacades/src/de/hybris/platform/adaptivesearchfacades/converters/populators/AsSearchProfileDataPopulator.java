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
package de.hybris.platform.adaptivesearchfacades.converters.populators;

import de.hybris.adaptivesearchfacades.data.AsSearchProfileData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.converters.Populator;


public class AsSearchProfileDataPopulator implements Populator<AbstractAsSearchProfileModel, AsSearchProfileData>
{
	@Override
	public void populate(final AbstractAsSearchProfileModel source, final AsSearchProfileData target)
	{
		target.setCode(source.getCode());
		target.setName(source.getName());
		target.setIndexType(source.getIndexType());
		target.setCatalogVersion(source.getCatalogVersion().getCatalog().getId() + ":" + source.getCatalogVersion().getVersion());
	}
}
