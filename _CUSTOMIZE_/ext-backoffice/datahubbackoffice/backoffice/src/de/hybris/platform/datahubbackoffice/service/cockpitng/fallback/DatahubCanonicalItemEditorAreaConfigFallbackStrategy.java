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
package de.hybris.platform.datahubbackoffice.service.cockpitng.fallback;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultEditorAreaConfigFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomSection;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.EditorArea;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Tab;

public class DatahubCanonicalItemEditorAreaConfigFallbackStrategy extends DefaultEditorAreaConfigFallbackStrategy
{
	private String componentName;
	private String customSectionDescr;
	private String customSectionName;
	private String customSectionSpringId;

	@Override
	public EditorArea loadFallbackConfiguration(final ConfigContext context, final Class<EditorArea> configurationType)
	{
		EditorArea ret = null;
		if (StringUtils.equals(context.getAttribute(DefaultConfigContext.CONTEXT_COMPONENT), componentName))
		{
			ret = new EditorArea();
			ret.setName(String.format("%s%s", customSectionSpringId, "editor-area-config"));
			final Tab tab = new Tab();
			final CustomSection customSection = new CustomSection();
			customSection.setDescription(customSectionDescr);
			customSection.setName(customSectionName);
			customSection.setSpringBean(customSectionSpringId);
			tab.getCustomSectionOrSection().add(customSection);
			ret.getCustomTabOrTab().add(tab);
		}
		else
		{
			ret = super.loadFallbackConfiguration(context, configurationType);
		}
		return ret;
	}

	public void setComponentName(final String componentName)
	{
		this.componentName = componentName;
	}

	public void setCustomSectionDescr(final String customSectionDescr)
	{
		this.customSectionDescr = customSectionDescr;
	}

	public void setCustomSectionName(final String customSectionName)
	{
		this.customSectionName = customSectionName;
	}

	public void setCustomSectionSpringId(final String customSectionSpringId)
	{
		this.customSectionSpringId = customSectionSpringId;
	}
}
