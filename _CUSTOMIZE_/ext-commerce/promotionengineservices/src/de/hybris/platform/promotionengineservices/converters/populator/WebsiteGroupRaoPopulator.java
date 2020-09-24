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
package de.hybris.platform.promotionengineservices.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.ruleengineservices.rao.WebsiteGroupRAO;


/**
 * Populates WebsiteGroupRAO from PromotionGroupModel
 *
 */
public class WebsiteGroupRaoPopulator implements Populator<PromotionGroupModel, WebsiteGroupRAO>
{

	@Override
	public void populate(final PromotionGroupModel source, final WebsiteGroupRAO target)
	{
		target.setId(source.getIdentifier());
	}

}
