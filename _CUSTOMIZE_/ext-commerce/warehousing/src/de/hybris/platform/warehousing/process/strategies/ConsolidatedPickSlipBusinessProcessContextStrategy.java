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
 *
 */
package de.hybris.platform.warehousing.process.strategies;


import de.hybris.platform.acceleratorservices.process.strategies.impl.AbstractProcessContextStrategy;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import static de.hybris.platform.warehousing.constants.WarehousingConstants.CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME;


/***
 * Strategy to get the CmsSite of a {@link BusinessProcessModel}
 */
public class ConsolidatedPickSlipBusinessProcessContextStrategy extends AbstractProcessContextStrategy
{

	@Override
	public BaseSiteModel getCmsSite(final BusinessProcessModel businessProcessModel)
	{
		BaseSiteModel baseSite = null;
		if (businessProcessModel.getContextParameters().iterator().hasNext())
		{
			final BusinessProcessParameterModel param = businessProcessModel.getContextParameters().iterator().next();
			if(CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME.equals(param.getName()))
			{
				final List<ConsignmentModel> consignmentList = (List<ConsignmentModel>) param.getValue();

				if (CollectionUtils.isNotEmpty(consignmentList))
				{
					baseSite = consignmentList.iterator().next().getOrder().getSite();
				}
			}

		}
		return baseSite;
	}

	@Override
	protected CustomerModel getCustomer(final BusinessProcessModel businessProcess)
	{
		return null;
	}
}
