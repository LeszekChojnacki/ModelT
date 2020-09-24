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
package com.hybris.backoffice.renderer;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.product.ProductModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Span;

import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;

import java.util.Locale;


public class ProductApprovalStatusRenderer extends AbstractWidgetComponentRenderer<Component, Object, ProductModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductApprovalStatusRenderer.class);
	private static final String APPROVAL_STATUS_NOT_RENDERED = "Approval status not rendered";

	public static final String YW_IMAGE_ATTRIBUTE_APPROVAL_STATUS = "yw-image-attribute-approval-status-";
	public static final String YW_IMAGE_ATTRIBUTE_ICON = "yw-image-attribute-approval-icon";
	public static final String SCLASS_YW_IMAGE_ATTRIBUTE_SYNC_STATUS_NO_READ = "yw-image-attribute-sync-status-no-read";

	private LabelService labelService;
	private PermissionFacade permissionFacade;

	@Override
	public void render(final Component parent, final Object configuration, final ProductModel data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final ArticleApprovalStatus approvalStatus = data.getApprovalStatus();
		if (approvalStatus == null)
		{
			LOG.warn(APPROVAL_STATUS_NOT_RENDERED);
			return;
		}

		parent.appendChild(createIcon(data, approvalStatus));
		fireComponentRendered(parent, configuration, data);
	}

	protected HtmlBasedComponent createIcon(final ProductModel productModel, final ArticleApprovalStatus approvalStatus)
	{
		final Span icon = new Span();
		UITools.addSClass(icon, YW_IMAGE_ATTRIBUTE_ICON);

		if (getPermissionFacade().canReadInstanceProperty(productModel, ProductModel.APPROVALSTATUS))
		{
			final String sclass = YW_IMAGE_ATTRIBUTE_APPROVAL_STATUS.concat(approvalStatus.name().toLowerCase(Locale.ENGLISH));
			UITools.addSClass(icon, sclass);
			icon.setTooltiptext(getTooltipText(productModel));
		}
		else
		{
			UITools.addSClass(icon, SCLASS_YW_IMAGE_ATTRIBUTE_SYNC_STATUS_NO_READ);
			icon.setTooltiptext(labelService.getAccessDeniedLabel(productModel));
		}

		return icon;
	}

	protected String getTooltipText(final ProductModel data)
	{
		return Labels.getLabel("product.tooltip.approval.status", new Object[]
		{ labelService.getObjectLabel(data.getApprovalStatus()) });
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

}
