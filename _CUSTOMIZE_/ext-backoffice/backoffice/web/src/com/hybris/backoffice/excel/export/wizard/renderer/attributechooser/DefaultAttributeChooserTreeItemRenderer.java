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
package com.hybris.backoffice.excel.export.wizard.renderer.attributechooser;

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;


/**
 * Default item renderer for the {@link DefaultAttributeChooserRenderer}
 * 
 * @deprecated since 6.7 {@link com.hybris.backoffice.attributechooser.AttributeChooserTreeItemRenderer}
 */
@Deprecated
public class DefaultAttributeChooserTreeItemRenderer implements TreeitemRenderer<DefaultTreeNode<SelectedAttribute>>
{
	private static final String SCLASS_Y_ATTRIBUTE_PICKER = "y-attributepicker";
	private LabelService labelService;
	private CommonI18NService commonI18NService;


	@Override
	public void render(final Treeitem item, final DefaultTreeNode<SelectedAttribute> data, final int i) throws Exception
	{
		final Treerow treerow = new Treerow();
		treerow.setParent(item);

		final Treecell treecell = new Treecell();
		treecell.setParent(treerow);

		final SelectedAttribute selectedAttribute = data.getData();

		final String attrName = selectedAttribute.getName();
		if (selectedAttribute.isRequired(getCommonI18NService().getCurrentLanguage().getIsocode()))
		{
			UITools.addSClass(treerow, SCLASS_Y_ATTRIBUTE_PICKER + "-mandatory-item");
		}

		treecell.appendChild(new Label(attrName));

		if (isGroupingAttribute(data))
		{
			UITools.addSClass(treerow, SCLASS_Y_ATTRIBUTE_PICKER + "-localized");
		}
		else if (isSubAttribute(selectedAttribute))
		{
			UITools.addSClass(treerow, SCLASS_Y_ATTRIBUTE_PICKER + "-localized-child");
			treecell.appendChild(createLanguageMark(selectedAttribute));
		}

	}

	protected Label createLanguageMark(final SelectedAttribute selectedAttribute)
	{
		final Label attrLanguageLabel = new Label(selectedAttribute.getIsoCode());
		attrLanguageLabel.setSclass(SCLASS_Y_ATTRIBUTE_PICKER + "-language");
		attrLanguageLabel.setTooltiptext(labelService.getObjectDescription(new Locale(selectedAttribute.getIsoCode())));
		return attrLanguageLabel;
	}

	protected boolean isSubAttribute(final SelectedAttribute attribute)
	{
		return attribute.getIsoCode() != null;
	}

	protected boolean isGroupingAttribute(final DefaultTreeNode<SelectedAttribute> node)
	{
		return node.getData().isLocalized() && !node.isLeaf();
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
