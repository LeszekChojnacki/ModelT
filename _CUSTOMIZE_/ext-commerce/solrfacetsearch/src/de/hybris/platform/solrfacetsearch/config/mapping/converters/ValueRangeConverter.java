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
package de.hybris.platform.solrfacetsearch.config.mapping.converters;

import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.mapping.FacetSearchConfigMapping;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;


@FacetSearchConfigMapping
public class ValueRangeConverter extends CustomConverter<ValueRange, ValueRange>
{
	@Override
	public ValueRange convert(final ValueRange source, final Type<? extends ValueRange> destinationType,
			final MappingContext mappingContext)
	{
		final ValueRange target = new ValueRange();
		target.setName(source.getName());
		target.setFrom(source.getFrom());
		target.setTo(source.getTo());
		return target;
	}
}
