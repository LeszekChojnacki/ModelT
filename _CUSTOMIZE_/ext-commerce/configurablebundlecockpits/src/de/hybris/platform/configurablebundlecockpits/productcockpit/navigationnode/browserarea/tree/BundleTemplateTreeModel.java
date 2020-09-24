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

package de.hybris.platform.configurablebundlecockpits.productcockpit.navigationnode.browserarea.tree;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.model.search.ResultObject;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.session.impl.DefaultSearchBrowserModel;
import de.hybris.platform.configurablebundleservices.bundle.BundleTemplateService;
import de.hybris.platform.configurablebundleservices.model.BundleTemplateModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.spring.SpringUtil;
import org.zkoss.zul.AbstractTreeModel;


public class BundleTemplateTreeModel extends AbstractTreeModel
{
	private final BundleTemplateService bundleTemplateService;
	private final TypeService typeService;

	public BundleTemplateTreeModel(final Object root)
	{
		super(root);
		this.bundleTemplateService = (BundleTemplateService) SpringUtil.getBean("bundleTemplateService",
				BundleTemplateService.class);
		this.typeService = (TypeService) SpringUtil.getBean("cockpitTypeService", TypeService.class);

	}

	@Override
	public boolean isLeaf(final Object node)
	{
		return getChildCount(node) == 0;
	}

	@Override
	public Object getChild(final Object parent, final int index)
	{
		final List<BundleTemplateModel> children = getChildren(parent);
		if ((children != null) && (children.size() > index))
		{
			return getTypeService().wrapItem(children.get(index));
		}
		else
		{
			return null;
		}
	}

	@Override
	public int getChildCount(final Object parent)
	{
		return getChildren(parent).size();
	}

	protected List<BundleTemplateModel> getChildren(final Object nodeObj)
	{
		List<BundleTemplateModel> bundleList = new ArrayList<BundleTemplateModel>();
		if (nodeObj instanceof DefaultSearchBrowserModel)
		{
			final DefaultSearchBrowserModel model = (DefaultSearchBrowserModel) nodeObj;
			for (final ResultObject ro : model.getResult().getResult())
			{
				if (ro.getObject() instanceof BundleTemplateModel)
				{
					bundleList.add((BundleTemplateModel) ro.getObject());
				}
			}
			if (model.getResult().getResult().size() == model.getResult().getTotalCount())
			{
				Collections.sort(bundleList, new ParentBundleTemplateComparator());
			}
			return bundleList;
		}
		else
		{
			final ItemModel parent = (ItemModel) ((TypedObject) nodeObj).getObject();
			if (parent instanceof CatalogVersionModel)
			{
				final List<BundleTemplateModel> rootBundles = getBundleTemplateService().getAllRootBundleTemplates(
						(CatalogVersionModel) parent);
				bundleList.addAll(rootBundles);
				Collections.sort(bundleList, new ParentBundleTemplateComparator());
				return bundleList;
			}
			bundleList = ((BundleTemplateModel) parent).getChildTemplates();
		}
		return bundleList;
	}

	protected BundleTemplateService getBundleTemplateService()
	{
		return bundleTemplateService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}
}
