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

import static org.assertj.core.api.Assertions.assertThat;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.dto.item.ItemData;

import java.util.List;

import org.junit.Test;

public class RestrictedAttributesProviderUnitTest
{
	private final RestrictedAttributesProvider strategy = new RestrictedAttributesProvider();

	@Test
	public void testGetAttributesForItemData()
	{
		final List<Attribute> attributes = strategy.getAttributes(new ItemData());

		assertThat(attributes).isEmpty();
	}

	@Test
	public void testGetAttributesForNull()
	{
		final List<Attribute> attributes = strategy.getAttributes(null);

		assertThat(attributes).isEmpty();
	}
}