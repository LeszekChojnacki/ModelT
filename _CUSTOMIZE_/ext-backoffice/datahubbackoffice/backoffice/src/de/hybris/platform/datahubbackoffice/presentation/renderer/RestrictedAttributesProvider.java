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
package de.hybris.platform.datahubbackoffice.presentation.renderer;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.dto.item.ItemData;

import java.util.Collections;
import java.util.List;

/**
 * An attributes retrieval strategy, which does not provide attributes for an item. It's good to use for users who do not have access to attributes.
 */
public class RestrictedAttributesProvider implements CanonicalItemAttributesProvider
{
	@Override
	public List<Attribute> getAttributes(final ItemData item)
	{
		return Collections.emptyList();
	}
}
