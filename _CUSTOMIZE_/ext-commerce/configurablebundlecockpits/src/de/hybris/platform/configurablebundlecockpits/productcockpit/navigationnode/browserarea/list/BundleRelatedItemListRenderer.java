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

package de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.list;

import de.hybris.platform.cockpit.components.mvc.listbox.listeners.DeleteListener;
import de.hybris.platform.cockpit.components.mvc.listbox.view.DefaultNodeWithActionsRenderer;
import de.hybris.platform.cockpit.constants.ImageUrls;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.label.LabelService;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.session.UICockpitPerspective;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.AbstractBrowserArea;
import de.hybris.platform.cockpit.util.UITools;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.spring.SpringUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Toolbarbutton;


/**
 * Renders content pages list items
 * 
 */
public class BundleRelatedItemListRenderer extends DefaultNodeWithActionsRenderer
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(BundleRelatedItemListRenderer.class);

	protected static final String EDIT_PRODUCT_TOOLTIP = "configurablebundlecockpits.product.edit";
	protected static final String REMOVE_PRODUCT_TOOLTIP = "configurablebundlecockpits.product.remove";

	private TypeService typeService;
	private LabelService labelService;

	@Override
	public Listitem renderListitem(final Listitem item, final Object data)
	{
		final String labelText = getLabelService().getObjectTextLabelForTypedObject((TypedObject) data);
		final String personalizedIconUrl = getLabelService().getObjectIconPathForTypedObject((TypedObject) data);


		final Listcell labelCell = new Listcell();
		labelCell.setSclass("relatedItemLabel");
		if (StringUtils.isNotBlank(personalizedIconUrl))
		{
			final Toolbarbutton itemIcon = new Toolbarbutton("", UITools.getAdjustedUrl(personalizedIconUrl));
			itemIcon.setParent(labelCell);
		}
		final Label textLabel = new Label(labelText);
		textLabel.setParent(labelCell);

		final Listcell actionsCell = new Listcell();
		actionsCell.setSclass("navigationNodesRelItemActions");
		addActions(actionsCell);
		item.setValue(data);
		labelCell.setParent(item);
		actionsCell.setParent(item);

		return item;
	}

	protected void createLabelActions(final Listcell labelCell, final TypedObject data)
	{
		labelCell.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event)
			{
				final UICockpitPerspective perspective = UISessionUtils.getCurrentSession().getCurrentPerspective();
				if (perspective != null)
				{
					perspective.activateItemInEditor(data);
				}
			}
		});

		labelCell.addEventListener(Events.ON_CLICK, new EventListener()
		{
			@Override
			public void onEvent(final Event event)
			{
				final UICockpitPerspective perspective = UISessionUtils.getCurrentSession().getCurrentPerspective();
				if (perspective != null)
				{
					perspective.activateItemInEditor(data);
					final AbstractBrowserArea browserArea = (AbstractBrowserArea) UISessionUtils.getCurrentSession()
							.getCurrentPerspective().getBrowserArea();
					browserArea.openInspector(data);
				}
			}
		});

	}

	@Override
	protected void addActions(final Listcell actionsCell)
	{
		appendEditButton(actionsCell);
		appendDeleteButton(actionsCell);
	}

	protected void appendEditButton(final Listcell actionsCell)
	{
		final Toolbarbutton editButton = new Toolbarbutton("", ImageUrls.EDIT_ICON);
		editButton.setTooltiptext(Labels.getLabel(EDIT_PRODUCT_TOOLTIP));
		editButton.addEventListener(Events.ON_CLICK, event ->
		{
			final TypedObject value = (TypedObject) ((Listitem) actionsCell.getParent()).getValue();
			UISessionUtils.getCurrentSession().getCurrentPerspective().activateItemInEditor(value);
		});
		actionsCell.appendChild(editButton);
	}

	protected void appendDeleteButton(final Listcell actionsCell)
	{
		final Toolbarbutton deleteButton = new Toolbarbutton("", "/productcockpit/images/cnt_elem_remove_action.png");
		deleteButton.setTooltiptext(Labels.getLabel(REMOVE_LABEL_KEY));
		deleteButton.addEventListener(Events.ON_CLICK, new DeleteListener());
		actionsCell.appendChild(deleteButton);
	}

	protected BundleRelatedItemListController getRelatedItemListController()
	{
		return (BundleRelatedItemListController) SpringUtil.getBean("bundleRelatedItemListController",
				BundleRelatedItemListController.class);
	}

	protected TypeService getTypeService()
	{
		if (this.typeService == null)
		{
			this.typeService = UISessionUtils.getCurrentSession().getTypeService();
		}
		return this.typeService;
	}

	protected LabelService getLabelService()
	{
		if (this.labelService == null)
		{
			this.labelService = UISessionUtils.getCurrentSession().getLabelService();
		}
		return this.labelService;
	}
}
