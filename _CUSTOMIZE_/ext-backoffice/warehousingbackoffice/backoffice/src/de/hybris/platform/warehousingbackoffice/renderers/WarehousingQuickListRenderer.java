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
 */
package de.hybris.platform.warehousingbackoffice.renderers;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.Iterator;

import com.hybris.backoffice.widgets.quicklist.renderer.DefaultQuickListItemRenderer;
import com.hybris.cockpitng.config.quicklist.jaxb.QuickList;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vbox;


/**
 * Renderer that lists consignment entries within a summary context
 */
public class WarehousingQuickListRenderer extends DefaultQuickListItemRenderer
{
	protected static final String SCLASS_HBOX_CONTAINER = "yw-quicklist-warehousing-hbox-container";
	protected static final String SCLASS_VBOX_CONTAINER = "yw-quicklist-warehousing-vbox-container";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_TITLE_CUST = "yw-quicklist-warehousing--tile-title";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_CUST = "yw-quicklist-warehousing--tile";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_IMG_CUST = "yw-quicklist-warehousing--tile--img";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_PROD_CODE = "yw-quicklist-warehousing--tile--prod-code";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_PROD_NAME = "yw-quicklist-warehousing--tile--prod-name";
	protected static final String SCLASS_YW_QUICK_LIST_TILE_PROD_QTY = "yw-quicklist-warehousing--tile--prod-qty";

	@Override
	public void render(final HtmlBasedComponent parent, final QuickList configuration, final Object data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (data instanceof WorkflowActionModel)
		{
			parent.setSclass(SCLASS_YW_QUICK_LIST_TILE_CUST);

			final ConsignmentModel consignment = ((ConsignmentModel) ((WorkflowActionModel) data).getAttachmentItems().iterator()
					.next());

			final Label consignmentCodeLabel = new Label(
					resolveLabel("warehousingbackoffice.quick.list.consignment.code") + " " + consignment.getCode());
			consignmentCodeLabel.setSclass(SCLASS_YW_QUICK_LIST_TILE_TITLE_CUST);
			parent.appendChild(consignmentCodeLabel);

			consignment.getConsignmentEntries().stream().filter(consignmentEntry -> consignmentEntry.getQuantity() != 0)
					.forEach(consignmentEntry -> renderConsignmentEntry(parent, consignmentEntry));
		}
	}

	/**
	 * Returns a {@link Label} of the given property while applying the corresponding styling class
	 *
	 * @param attributeToRender
	 * 		The attribute to render as a {@link Label}
	 * @return the label of the given property
	 */
	protected Label labelizeProperty(final String attributeToRender)
	{
		final Label attributeLabel = new Label(attributeToRender);
		attributeLabel.setSclass(SCLASS_YW_QUICK_LIST_TILE_SUBTITLE);
		return attributeLabel;
	}

	/**
	 * Returns localized label by key, or a "[labelKey]" if localized label was nor registered.
	 *
	 * @param labelKey
	 * 		the key of the label to retrieve
	 * @return The localized label
	 */
	protected String resolveLabel(final String labelKey)
	{
		final String defaultValue = LabelUtils.getFallbackLabel(labelKey);
		return Labels.getLabel(labelKey, defaultValue);
	}

	/**
	 * Renders each {@link ConsignmentEntryModel} that will be displayed in the quick list.
	 *
	 * @param parent
	 * 		parent component of the {@link ConsignmentEntryModel}
	 * @param consignmentEntry
	 * 		{@link ConsignmentEntryModel} to be rendered
	 */
	protected void renderConsignmentEntry(final HtmlBasedComponent parent, final ConsignmentEntryModel consignmentEntry)
	{
		final ProductModel product = consignmentEntry.getOrderEntry().getProduct();

		final Hbox consignmentEntryBox = new Hbox();
		consignmentEntryBox.setSclass(SCLASS_HBOX_CONTAINER);

		final Vbox infoList = new Vbox();
		infoList.setSclass(SCLASS_VBOX_CONTAINER);

		if (product.getThumbnail() != null)
		{

			final Image productImage = new Image(product.getThumbnail().getURL());
			productImage.setSclass(SCLASS_YW_QUICK_LIST_TILE_IMG_CUST);

			consignmentEntryBox.appendChild(productImage);
		}

		final HtmlBasedComponent productCode = labelizeProperty(product.getCode());
		productCode.setSclass(SCLASS_YW_QUICK_LIST_TILE_PROD_CODE);
		infoList.appendChild(productCode);

		final HtmlBasedComponent productName = labelizeProperty(product.getName());
		productName.setSclass(SCLASS_YW_QUICK_LIST_TILE_PROD_NAME);
		infoList.appendChild(productName);

		final HtmlBasedComponent quantityPending = labelizeProperty(
				resolveLabel("warehousingbackoffice.quick.list.quantity.pending") + " " + consignmentEntry.getQuantityPending()
						.toString());
		quantityPending.setSclass(SCLASS_YW_QUICK_LIST_TILE_PROD_QTY);
		infoList.appendChild(quantityPending);

		consignmentEntryBox.appendChild(infoList);
		parent.appendChild(consignmentEntryBox);
	}

}
