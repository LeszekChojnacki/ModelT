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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_NULL_IDENTIFIER;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchConfigurationModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import jersey.repackaged.com.google.common.base.Objects;


/**
 * Handler for corrupted attribute of {@link AsCategoryAwareSearchConfigurationModel}.
 */
public class AsCategoryAwareSearchConfigurationCorruptedAttributeHandler
		implements DynamicAttributeHandler<Boolean, AsCategoryAwareSearchConfigurationModel>
{
	private ModelService modelService;

	@Override
	public Boolean get(final AsCategoryAwareSearchConfigurationModel model)
	{
		if (modelService.isNew(model))
		{
			return Boolean.FALSE;
		}

		if (StringUtils.isBlank(model.getUniqueIdx()))
		{
			return Boolean.TRUE;
		}

		final CategoryModel category = model.getCategory();

		if (category != null && category.getPk() == null)
		{
			return Boolean.TRUE;
		}

		final String expectedCategoryIdentifier = StringUtils.substringAfterLast(model.getUniqueIdx(), UNIQUE_IDX_SEPARATOR);
		final String categoryIdentifier = category == null ? UNIQUE_IDX_NULL_IDENTIFIER : category.getPk().getLongValueAsString();

		return Boolean.valueOf(!Objects.equal(expectedCategoryIdentifier, categoryIdentifier));
	}

	@Override
	public void set(final AsCategoryAwareSearchConfigurationModel model, final Boolean value)
	{
		throw new UnsupportedOperationException("Write is not a valid operation for this dynamic attribute");
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
