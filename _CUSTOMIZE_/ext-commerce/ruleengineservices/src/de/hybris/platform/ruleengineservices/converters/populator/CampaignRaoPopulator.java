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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.rao.CampaignRAO;


/**
 * Populates CampaignRAO from CampaignModel.
 */
public class CampaignRaoPopulator implements Populator<CampaignModel, CampaignRAO>
{

	@Override
	public void populate(final CampaignModel source, final CampaignRAO target)
	{
		target.setCode(source.getCode());
	}

}
