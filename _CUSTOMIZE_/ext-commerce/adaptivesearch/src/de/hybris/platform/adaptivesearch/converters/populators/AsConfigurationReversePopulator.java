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
package de.hybris.platform.adaptivesearch.converters.populators;

import de.hybris.platform.adaptivesearch.converters.AsConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.strategies.AsUidGenerator;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AbstractAsConfigurationModel} from {@link AbstractAsConfiguration}.
 */
public class AsConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsConfiguration, AbstractAsConfigurationModel, AsConfigurationReverseConverterContext>
{
	private AsUidGenerator asUidGenerator;

	@Override
	public void populate(final AbstractAsConfiguration source, final AbstractAsConfigurationModel target,
			final AsConfigurationReverseConverterContext context)
	{
		target.setCatalogVersion(context.getCatalogVersion());

		if (StringUtils.isNotBlank(source.getUid()))
		{
			// we need to generate a new uid
			target.setUid(asUidGenerator.generateUid());
		}
	}

	public AsUidGenerator getAsUidGenerator()
	{
		return asUidGenerator;
	}

	@Required
	public void setAsUidGenerator(final AsUidGenerator asUidGenerator)
	{
		this.asUidGenerator = asUidGenerator;
	}
}
