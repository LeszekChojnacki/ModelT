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

import de.hybris.platform.datahubbackoffice.ItemDataConstants;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.client.CanonicalItemClassClient;
import com.hybris.datahub.dto.item.ItemData;
import com.hybris.datahub.dto.metadata.CanonicalAttributeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

public class UnrestrictedCanonicalItemAttributesProvider implements CanonicalItemAttributesProvider
{
	private CanonicalItemClassClient canonicalItemClassClient;

	@Override
	public List<Attribute> getAttributes(final ItemData item)
	{
		final String itemType = item.getType();
		return StringUtils.isNotBlank(itemType)
				? orderCanonicalItemAttributes(canonicalItemClassClient.getAttributes(itemType).stream()
						.map(UnrestrictedCanonicalItemAttributesProvider::toAttribute)
						.collect(Collectors.toList()))
				: Collections.emptyList();
	}

	private static List<Attribute> orderCanonicalItemAttributes(final List<Attribute> attributes)
	{
		final List<Attribute> sorted = new ArrayList<>(attributes);
		sorted.sort((attr1, attr2) -> {
			if (attr1.getQualifier().equals(ItemDataConstants.ID))
			{
				return -1;
			}
			else if (attr2.getQualifier().equals(ItemDataConstants.ID))
			{
				return 1;
			}
			else if (attr1.getQualifier().equals(ItemDataConstants.INTEGRATION_KEY))
			{
				return attr2.getQualifier().equals(ItemDataConstants.ID) ? 1 : -1;
			}
			else if (attr2.getQualifier().equals(ItemDataConstants.INTEGRATION_KEY))
			{
				return attr1.getQualifier().equals(ItemDataConstants.ID) ? -1 : 1;
			}
			return attr1.getQualifier().compareTo(attr2.getQualifier());
		});
		return sorted;
	}

	private static Attribute toAttribute(final CanonicalAttributeData data)
	{
		final Attribute attribute = new Attribute();
		attribute.setQualifier(data.getName());
		return attribute;
	}

	@Required
	public void setCanonicalItemClassClient(final CanonicalItemClassClient client)
	{
		canonicalItemClassClient = client;
	}
}