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

import de.hybris.platform.cockpit.components.ComponentsHelper;
import de.hybris.platform.cockpit.components.mvc.listbox.Listbox;
import de.hybris.platform.cockpit.components.mvc.listbox.ListboxController;
import de.hybris.platform.cockpit.events.impl.ItemChangedEvent;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.SystemService;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.AbstractBrowserArea;
import de.hybris.platform.cockpit.util.ListProvider;
import de.hybris.platform.configurablebundlecockpits.servicelayer.services.BundleNavigationService;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.security.permissions.PermissionsConstants;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.fest.util.Collections;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listitem;


/**
 * Controller of content pages list.
 */
// NOSONAR
public class BundleRelatedItemListController implements ListboxController<TypedObject>
{
	private TypedObject currentNavigationNode;
	private BundleNavigationService bundleNavigationService;
	private SystemService systemService;
	private Set<TypedObject> selectedItems;
	private ModelService modelService;

	@Override
	public void move(final Listbox contentPageList, final TypedObject node, final TypedObject target)
	{
		final Object value = node.getObject();
		if (value instanceof ProductModel)
		{
			final ProductModel sourceModel = (ProductModel) value;
			final ProductModel targetModel = (ProductModel) target.getObject();
			//do the move
			final BundleTemplateModel naviNode = (BundleTemplateModel) currentNavigationNode.getObject();
			getBundleNavigationService().move(naviNode, sourceModel, targetModel);
			//update pages list component

			contentPageList.setModel(new BundleRelatedItemListModel(currentNavigationNode));
			//dispatch update event
			UISessionUtils.getCurrentSession().sendGlobalEvent(new ItemChangedEvent(null, node, null));

		}
	}

	public boolean updateList(final Listbox contentPageList)
	{
		boolean ret = false;
		if (currentNavigationNode != null
				&& CollectionUtils.isNotEmpty(((BundleTemplateModel) currentNavigationNode.getObject()).getProducts()))
		{
			ret = true;
			contentPageList.setModel(new BundleRelatedItemListModel(currentNavigationNode));
		}
		return ret;
	}

	/**
	 * @param currentNavigationNode
	 *           the currentNavigationNode to set
	 */
	public void setCurrentNavigationNode(final TypedObject currentNavigationNode)
	{
		this.currentNavigationNode = currentNavigationNode;
	}

	@Override
	public void delete(final Listbox listbox, final TypedObject typedObject)
	{
		if (getSystemService().checkAttributePermissionOn(BundleTemplateModel._TYPECODE, BundleTemplateModel.PRODUCTS,
				PermissionsConstants.CHANGE))
		{
			ComponentsHelper.displayConfirmationPopup("", Labels.getLabel("general.confirm.delete"), new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD:ZK Specific
				{
					if (((Integer) event.getData()).intValue() == Messagebox.YES)
					{
						final BundleTemplateModel naviNode = (BundleTemplateModel) currentNavigationNode.getObject();
						getBundleNavigationService().remove(naviNode, (ProductModel) typedObject.getObject());
						// update listbox
						listbox.setModel(new BundleRelatedItemListModel(currentNavigationNode));

						//dispatch update event
						UISessionUtils.getCurrentSession().sendGlobalEvent(new ItemChangedEvent(null, typedObject, null));
					}
				}
			});
		}
	}

	protected BundleNavigationService getBundleNavigationService()
	{
		return this.bundleNavigationService;
	}

	@Required
	public void setBundleNavigationService(final BundleNavigationService bundleNavigationService)
	{
		this.bundleNavigationService = bundleNavigationService;
	}

	protected SystemService getSystemService()
	{
		if (this.systemService == null)
		{
			this.systemService = UISessionUtils.getCurrentSession().getSystemService();
		}
		return this.systemService;
	}

	@Override
	public Set<TypedObject> getSelected()
	{
		return selectedItems;
	}

	@Override
	public void selected(final Listbox component, final Set<Listitem> selectedItems)
	{
		final TypedObject data = (TypedObject) selectedItems.iterator().next().getValue();

		final AbstractBrowserArea browserArea = (AbstractBrowserArea) UISessionUtils.getCurrentSession().getCurrentPerspective()
				.getBrowserArea();
		browserArea.updateInfoArea(new ListProvider<TypedObject>()
		{

			@Override
			public List<TypedObject> getList()
			{
				return Collections.list(getFirst());
			}

			@Override
			public TypedObject getFirst()
			{
				return data;
			}

			@Override
			public de.hybris.platform.cockpit.util.ListProvider.ListInfo getListInfo()
			{
				return ListInfo.SINGLETON;
			}

			@Override
			public int getListSize()
			{
				return 1;
			}
		}, false);
		browserArea.getInfoArea().setVisible(browserArea.isOpenInspectorOnSelect());
	}

	protected ModelService getModelService()
	{
		if (this.modelService == null)
		{
			this.modelService = UISessionUtils.getCurrentSession().getModelService();
		}
		return this.modelService;
	}
}
