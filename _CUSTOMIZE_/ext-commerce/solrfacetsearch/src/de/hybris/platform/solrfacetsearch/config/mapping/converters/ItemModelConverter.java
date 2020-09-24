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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.mapping.FacetSearchConfigMapping;

import org.springframework.beans.factory.annotation.Required;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;


@FacetSearchConfigMapping
public class ItemModelConverter extends CustomConverter<ItemModel, ItemModel>
{
	private ModelService modelService;

	@Override
	public boolean canConvert(final Type<?> sourceType, final Type<?> destinationType)
	{
		return this.sourceType.isAssignableFrom(sourceType) && this.destinationType.isAssignableFrom(destinationType);
	}

	@Override
	public ItemModel convert(final ItemModel source, final Type<? extends ItemModel> destinationType,
			final MappingContext mappingContext)
	{
		return modelService.get(source.getPk());
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	public boolean equals(final Object other)
	{
		return super.equals(other);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
