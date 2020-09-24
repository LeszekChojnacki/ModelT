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

import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.event.ListDataListener;


/**
 * Model for list of navigation node related item
 */
public class BundleRelatedItemListModel implements ListModel
{
	private final TypedObject naviNode;
	private TypeService typeService;

	private final List<ListDataListener> listeners = new ArrayList<ListDataListener>();

	/**
	 * @param naviNode
	 *           the navigation node for which items will be displayed
	 */
	public BundleRelatedItemListModel(final TypedObject naviNode)
	{
		this.naviNode = naviNode;
	}

	@Override
	public Object getElementAt(final int index)
	{
		Object ret = null;
		final List items = extractRelatedItems(getNaviNode());
		if (0 <= index && index < items.size())
		{
			ret = naviNode == null ? null : getTypeService().wrapItem(items.get(index));
		}
		return ret;
	}

	@Override
	public int getSize()
	{
		return naviNode == null ? 0 : extractRelatedItems(getNaviNode()).size();
	}

	/**
	 * @return model from typed node
	 */
	protected BundleTemplateModel getNaviNode()
	{
		return (BundleTemplateModel) naviNode.getObject();
	}

	@Override
	public void addListDataListener(final ListDataListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	@Override
	public void removeListDataListener(final ListDataListener listener)
	{
		if (this.listeners.contains(listener))
		{
			listeners.remove(listener);
		}
	}

	protected TypeService getTypeService()
	{
		if (this.typeService == null)
		{
			this.typeService = UISessionUtils.getCurrentSession().getTypeService();
		}
		return this.typeService;
	}

	protected List<ItemModel> extractRelatedItems(final BundleTemplateModel bundleTemplateNode)
	{

		final List<ItemModel> ret = new ArrayList<ItemModel>();
		if (bundleTemplateNode == null)
		{
			return ret;
		}
		for (final ProductModel entry : bundleTemplateNode.getProducts())
		{
			ret.add(entry);
		}
		return ret;
	}
}
