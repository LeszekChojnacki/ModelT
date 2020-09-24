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
package de.hybris.platform.ruleengineservices.rule.services.impl.converters.populators;


import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionCategoryModel;
import de.hybris.platform.ruleengineservices.rule.data.ImageData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionCategoryData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


public class RuleActionDefinitionCategoryPopulator implements
		Populator<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData>
{
	private Converter<MediaModel, ImageData> imageConverter;

	@Override
	public void populate(final RuleActionDefinitionCategoryModel source, final RuleActionDefinitionCategoryData target)
	{
		target.setId(source.getId());
		target.setName(source.getName());
		target.setPriority(source.getPriority());
		if (source.getIcon() != null)
		{
			target.setIcon(getImageConverter().convert(source.getIcon()));
		}
	}

	public Converter<MediaModel, ImageData> getImageConverter()
	{
		return imageConverter;
	}

	@Required
	public void setImageConverter(final Converter<MediaModel, ImageData> imageConverter)
	{
		this.imageConverter = imageConverter;
	}
}
