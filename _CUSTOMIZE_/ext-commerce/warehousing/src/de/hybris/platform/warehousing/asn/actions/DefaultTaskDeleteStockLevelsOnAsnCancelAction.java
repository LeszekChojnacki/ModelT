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
package de.hybris.platform.warehousing.asn.actions;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.asn.service.AsnService;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.taskassignment.actions.AbstractTaskAssignmentActions;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * An automated Task to Delete {@link de.hybris.platform.ordersplitting.model.StockLevelModel} from the cancelled {@link de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel}
 */
public class DefaultTaskDeleteStockLevelsOnAsnCancelAction extends AbstractTaskAssignmentActions
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskDeleteStockLevelsOnAsnCancelAction.class);
	private AsnService asnService;

	@Override
	public WorkflowDecisionModel perform(final WorkflowActionModel workflowActionModel)
	{
		if (getAttachedAsn(workflowActionModel).isPresent())
		{
			final AdvancedShippingNoticeModel attachedAsn = (AdvancedShippingNoticeModel) getAttachedAsn(workflowActionModel).get();
			final List<StockLevelModel> stockLevels = getAsnService().getStockLevelsForAsn(attachedAsn);
			if (CollectionUtils.isNotEmpty(stockLevels))
			{
				getModelService().removeAll(stockLevels);
				LOGGER.info("{} Stocklevels for product code: {} are being removed because ASN: {} got cancelled", stockLevels.size(),
						stockLevels.iterator().next().getProductCode(), attachedAsn.getInternalId());
			}
		}

		return workflowActionModel.getDecisions().isEmpty() ? null : workflowActionModel.getDecisions().iterator().next();
	}


	protected AsnService getAsnService()
	{
		return asnService;
	}

	@Required
	public void setAsnService(final AsnService asnService)
	{
		this.asnService = asnService;
	}

}
