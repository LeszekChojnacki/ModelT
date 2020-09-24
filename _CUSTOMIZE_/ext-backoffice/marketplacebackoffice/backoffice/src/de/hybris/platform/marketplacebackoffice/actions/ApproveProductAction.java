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
package de.hybris.platform.marketplacebackoffice.actions;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.marketplaceservices.strategies.AutoApproveProductStrategy;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.validation.coverage.CoverageInfo;
import de.hybris.platform.validation.coverage.strategies.impl.ValidationBasedCoverageCalculationStrategy;

import java.util.EnumSet;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;


/**
 * An action for approving a product.
 */
public class ApproveProductAction implements CockpitAction<ProductModel, Object>
{

	private static final String APPROVE_PRODUCT_EVENT_TYPE = "ApproveProduct";

	@Resource
	private ValidationBasedCoverageCalculationStrategy validationCoverageCalculationStrategy;

	@Resource
	private ModelService modelService;

	@Resource
	private AutoApproveProductStrategy autoApproveProductStrategy;
	
	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<ProductModel> ctx)
	{
		final ProductModel product = ctx.getData();

		final ActionResult<Object> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		final EnumSet<StatusFlag> statusFlags = EnumSet.of(StatusFlag.OBJECT_MODIFIED);

		final CoverageInfo coverageInfo = autoApproveProductStrategy.autoApproveVariantAndApparelProduct(product);
		if (coverageInfo != null)
		{
			coverageInfo.getPropertyInfoMessages()
					.forEach(
							x -> notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx),
									APPROVE_PRODUCT_EVENT_TYPE,
									NotificationEvent.Level.FAILURE, x.getMessage()));
			actionResult.setResultCode(ActionResult.ERROR);
		}
		else
		{
			product.setSaleable(Boolean.TRUE);
			product.setApprovalStatus(ArticleApprovalStatus.APPROVED);
			modelService.save(product);

			notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx),
					APPROVE_PRODUCT_EVENT_TYPE,
					NotificationEvent.Level.SUCCESS, product);
			actionResult.setStatusFlags(statusFlags);
		}

		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<ProductModel> ctx)
	{
		final ProductModel product = ctx.getData();
		return product != null && !ArticleApprovalStatus.APPROVED.getCode().equals(product.getApprovalStatus().getCode());
	}

}