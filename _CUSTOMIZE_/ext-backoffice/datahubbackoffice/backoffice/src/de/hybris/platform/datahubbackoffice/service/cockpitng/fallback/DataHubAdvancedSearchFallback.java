/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.service.cockpitng.fallback;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultAdvancedSearchConfigurationFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.AdvancedSearch;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.ConnectionOperatorType;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldListType;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;

public class DataHubAdvancedSearchFallback extends DefaultAdvancedSearchConfigurationFallbackStrategy
{
	@Override
	public AdvancedSearch loadFallbackConfiguration(final ConfigContext context, final Class<AdvancedSearch> configurationType)
	{
		if (StringUtils.startsWith(context.getAttribute("type"), "Datahub_Canonical")
				&& !StringUtils.equals(context.getAttribute("type"), "Datahub_CanonicalItem"))
		{
			final AdvancedSearch ret = new AdvancedSearch();
			final FieldListType fieldListType = new FieldListType();
			final FieldType field = new FieldType();
			field.setName("pool");
			field.setSelected(true);
			fieldListType.getField().add(field);
			ret.setFieldList(fieldListType);

			ret.setConnectionOperator(ConnectionOperatorType.OR);
			return ret;
		}
		return super.loadFallbackConfiguration(context, configurationType);
	}
}
