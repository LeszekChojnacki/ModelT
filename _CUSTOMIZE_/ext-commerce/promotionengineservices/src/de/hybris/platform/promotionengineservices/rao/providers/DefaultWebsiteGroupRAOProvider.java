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
package de.hybris.platform.promotionengineservices.rao.providers;

import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.ruleengineservices.rao.WebsiteGroupRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * The default provider for WebsiteGroupRAOs. It converts the provided {@link PromotionGroupModel} into a
 * {@link WebsiteGroupRAO}
 */
public class DefaultWebsiteGroupRAOProvider implements RAOProvider
{

	private Converter<PromotionGroupModel, WebsiteGroupRAO> websiteGroupRaoConverter;

	protected WebsiteGroupRAO createRAO(final PromotionGroupModel source)
	{
		return getWebsiteGroupRaoConverter().convert(source);
	}

	protected Converter<PromotionGroupModel, WebsiteGroupRAO> getWebsiteGroupRaoConverter()
	{
		return websiteGroupRaoConverter;
	}

	@Required
	public void setWebsiteGroupRaoConverter(final Converter<PromotionGroupModel, WebsiteGroupRAO> websiteGroupRaoConverter)
	{
		this.websiteGroupRaoConverter = websiteGroupRaoConverter;
	}

	@Override
	public Set<?> expandFactModel(final Object modelFact)
	{
		if (modelFact instanceof PromotionGroupModel)
		{
			return Collections.singleton(createRAO((PromotionGroupModel) modelFact));
		}
		else
		{
			return Collections.emptySet();
		}
	}
}
