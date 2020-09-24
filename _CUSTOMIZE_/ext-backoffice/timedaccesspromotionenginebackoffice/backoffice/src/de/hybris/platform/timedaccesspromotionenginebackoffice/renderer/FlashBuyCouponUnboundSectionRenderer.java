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
package de.hybris.platform.timedaccesspromotionenginebackoffice.renderer;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Section;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.editorarea.sections.UnboundSectionRenderer;


public class FlashBuyCouponUnboundSectionRenderer extends UnboundSectionRenderer //NOSONAR
{
	private List<String> unDisplayedFlashBuyCouponAttrList;

	@Override
	protected Section getUnboundSection(final WidgetInstanceManager widgetInstanceManager)
	{
		final Section unboundSection = super.getUnboundSection(widgetInstanceManager);
		unboundSection.getAttributeOrCustom().removeIf(p -> {
			final Attribute attr = (Attribute) p;
			return getUnDisplayedFlashBuyCouponAttrList().contains(attr.getQualifier());
		});

		return unboundSection;
	}

	protected List<String> getUnDisplayedFlashBuyCouponAttrList()
	{
		return unDisplayedFlashBuyCouponAttrList;
	}

	@Required
	public void setUnDisplayedFlashBuyCouponAttrList(final List<String> unDisplayedFlashBuyCouponAttrList)
	{
		this.unDisplayedFlashBuyCouponAttrList = unDisplayedFlashBuyCouponAttrList;
	}
}