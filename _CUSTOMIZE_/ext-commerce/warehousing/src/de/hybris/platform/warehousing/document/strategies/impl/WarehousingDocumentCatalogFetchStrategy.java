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
package de.hybris.platform.warehousing.document.strategies.impl;

import de.hybris.platform.acceleratorservices.document.strategy.DocumentCatalogFetchStrategy;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.returns.model.ReturnProcessModel;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static de.hybris.platform.warehousing.constants.WarehousingConstants.CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME;
import static org.springframework.util.Assert.isTrue;


/**
 * Implementation of {@link DocumentCatalogFetchStrategy} that extracts the catalog from a {@link BusinessProcessModel}
 */
public class WarehousingDocumentCatalogFetchStrategy implements DocumentCatalogFetchStrategy
{
	@Override
	public CatalogVersionModel fetch(final BusinessProcessModel businessProcessModel)
	{
		validateParameterNotNullStandardMessage("businessProcessModel", businessProcessModel);
		OrderModel order = null;

		if (businessProcessModel instanceof OrderProcessModel)
		{
			order = ((OrderProcessModel) businessProcessModel).getOrder();
		}
		else if (businessProcessModel instanceof ConsignmentProcessModel)
		{
			order = (OrderModel) ((ConsignmentProcessModel) businessProcessModel).getConsignment().getOrder();
		}
		else if (businessProcessModel instanceof ReturnProcessModel)
		{
			order = ((ReturnProcessModel) businessProcessModel).getReturnRequest().getOrder();
		}
		else if (businessProcessModel.getContextParameters().iterator().hasNext())
		{
			final BusinessProcessParameterModel param = businessProcessModel.getContextParameters().iterator().next();
			if (CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME.equals(param.getName()))
			{
				final List<ConsignmentModel> consignmentList = (List<ConsignmentModel>) param.getValue();

				if (CollectionUtils.isNotEmpty(consignmentList))
				{
					order = (OrderModel) consignmentList.iterator().next().getOrder();
				}
			}

		}

		validateParameterNotNullStandardMessage("order", order);

		isTrue(order.getPotentiallyFraudulent() != null); //NOSONAR
		isTrue(order.getSite() instanceof CMSSiteModel, "No CMSSite found for the order");
		final List<ContentCatalogModel> contentCatalogs = ((CMSSiteModel) order.getSite()).getContentCatalogs();
		isTrue(CollectionUtils.isNotEmpty(contentCatalogs), "Catalog Version cannot be found for the order");

		return contentCatalogs.iterator().next().getActiveCatalogVersion();//NOSONAR
	}

}
