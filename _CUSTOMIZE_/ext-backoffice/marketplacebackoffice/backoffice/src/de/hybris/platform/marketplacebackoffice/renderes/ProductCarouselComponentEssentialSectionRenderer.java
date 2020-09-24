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
package de.hybris.platform.marketplacebackoffice.renderes;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.editorarea.renderer.EditorAreaRendererUtils;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.AbstractEditorAreaSectionRenderer;

import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.cms2lib.model.components.ProductCarouselComponentModel;
import de.hybris.platform.marketplaceservices.vendor.VendorCMSService;


/**
 * An renderer for rendering synchronization status.
 */
public class ProductCarouselComponentEssentialSectionRenderer
		extends AbstractEditorAreaSectionRenderer<ProductCarouselComponentModel>
{

	private static final String SYNCHRONIZABLE = "synchronizable";
	private static final String LABEL_PREFIX = "label.carousel.";
	private static final String TYPE_STRING = "java.lang.String";
	private static final String TYPE_ENUM = "java.lang.Enum(SyncItemStatus)";
	private static final String EDITOR_STRING = "com.hybris.cockpitng.editor.defaulttext";
	private static final String EDITOR_ENUM = "com.hybris.cockpitng.editor.defaultenum";

	private VendorCMSService vendorCmsService;

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Override
	public void render(final Component parent, final AbstractSection configuration, final ProductCarouselComponentModel data,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{

		final WidgetModel model = widgetInstanceManager.getModel();
		final SyncItemStatus status = getVendorCmsService().getProductCarouselSynchronizationStatus(data);
		model.setValue(SYNCHRONIZABLE, status);
		model.setValue("title", data.getTitle());

		Hbox hbox = new Hbox();
		hbox.setParent(parent);
		renderAttribute(hbox, SYNCHRONIZABLE, status, widgetInstanceManager, TYPE_ENUM, EDITOR_ENUM);
		renderAttribute(hbox, "vendorCarouselCode", data.getVendorCarouselCode(), widgetInstanceManager, TYPE_STRING,
				EDITOR_STRING);

		hbox = new Hbox();
		hbox.setParent(parent);
		renderAttribute(hbox, "title", data.getTitle(), widgetInstanceManager, TYPE_STRING, EDITOR_STRING);

		EditorAreaRendererUtils.setAfterSaveListener(model, SYNCHRONIZABLE, new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final ProductCarouselComponentModel carousel = model.getValue("currentObject", ProductCarouselComponentModel.class);
				model.setValue(SYNCHRONIZABLE, getVendorCmsService().getProductCarouselSynchronizationStatus(carousel));
				model.changed();
			}
		}, false);

	}

	protected void renderAttribute(final Hbox hbox, final String attrName, final Object attr,
			final WidgetInstanceManager widgetInstanceManager, final String type, final String editorId)
	{
		final Cell cell = new Cell();
		hbox.appendChild(cell);
		cell.setSclass("yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-attrcell");

		final Editor editor = createEditor(attrName, attr, type, editorId, widgetInstanceManager);
		final Label label = new Label(Labels.getLabel(LABEL_PREFIX + attrName));
		label.setSclass("yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-attrcell-label");
		label.setTooltiptext(attrName);
		UITools.modifySClass(label, "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-attrcell-label-mandatory-attribute", true);
		cell.appendChild(label);
		cell.appendChild(editor);
		cell.setParent(hbox);
	}

	protected Editor createEditor(final String attrName, final Object obj, final String type, final String editorId,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Editor editor = new Editor();
		editor.setProperty("vendorCarouselCode".equals(attrName) ? ("currentObject." + attrName) : attrName);
		editor.setType(type);
		editor.setWidgetInstanceManager(widgetInstanceManager);
		editor.setDefaultEditor(editorId);
		editor.setEditorLabel(attrName);
		editor.setReadOnly(true);
		editor.afterCompose();

		return editor;
	}


	protected VendorCMSService getVendorCmsService()
	{
		return vendorCmsService;
	}

	@Required
	public void setVendorCmsService(final VendorCMSService vendorCmsService)
	{
		this.vendorCmsService = vendorCmsService;
	}

}
