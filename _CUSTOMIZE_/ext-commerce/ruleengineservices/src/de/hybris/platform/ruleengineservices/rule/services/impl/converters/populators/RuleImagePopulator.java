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
import de.hybris.platform.ruleengineservices.rule.data.ImageData;

import org.springframework.util.Assert;


/**
 * Converter implementation for {@link de.hybris.platform.core.model.media.MediaModel} as source and
 * {@link de.hybris.platform.ruleengineservices.rule.data.ImageData} as target type.
 */
public class RuleImagePopulator implements Populator<MediaModel, ImageData>
{

	@Override
	public void populate(final MediaModel source, final ImageData target)
	{
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		target.setUrl(source.getURL());
		target.setAltText(source.getAltText());
		if (source.getMediaFormat() != null)
		{
			target.setFormat(source.getMediaFormat().getQualifier());
		}
	}
}
