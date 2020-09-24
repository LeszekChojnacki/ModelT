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
package de.hybris.platform.adaptivesearch.model.attributes;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;


/**
 * Handler for corrupted attribute of {@link AbstractAsConfigurationModel}.
 */
public class AsConfigurationCorruptedAttributeHandler implements DynamicAttributeHandler<Boolean, AbstractAsConfigurationModel>
{
	@Override
	public Boolean get(final AbstractAsConfigurationModel model)
	{
		return Boolean.FALSE;
	}

	@Override
	public void set(final AbstractAsConfigurationModel model, final Boolean value)
	{
		throw new UnsupportedOperationException("Write is not a valid operation for this dynamic attribute");
	}
}
