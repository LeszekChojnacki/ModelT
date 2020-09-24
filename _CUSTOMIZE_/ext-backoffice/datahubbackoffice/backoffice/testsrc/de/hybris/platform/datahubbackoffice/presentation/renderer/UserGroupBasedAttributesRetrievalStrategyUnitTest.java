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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import de.hybris.platform.datahubbackoffice.service.datahub.UserContext;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.dto.item.ItemData;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class UserGroupBasedAttributesRetrievalStrategyUnitTest
{
	private static final String CONTEXT_USER_GROUP = "backoffice_user";
	private static final ItemData itemData = new ItemData();

	@Mock
	private UserContext userContext;
	@InjectMocks
	private UserGroupBasedAttributesRetrievalStrategy strategy;

	@Before
	public void setup()
	{
		doReturn(true).when(userContext).isMemberOf(CONTEXT_USER_GROUP);
	}

	@Test
	public void testNoAttributeProvidersConfigured()
	{
		final List<Attribute> attributes = strategy.retrieveAttributes(itemData);

		assertThat(attributes).isEmpty();
	}

	@Test
	public void testProviderForTheContextUserGroupDoesNotExist()
	{
		givenAttributesAvailableForUserGroup("some_user", attribute("some_attribute"));

		final List<Attribute> attributes = strategy.retrieveAttributes(itemData);

		assertThat(attributes).isEmpty();
	}

	@Test
	public void testProviderForTheContextUserGroupExists()
	{
		givenAttributesAvailableForUserGroup("some_user", attribute("attribute1"));
		givenAttributesAvailableForUserGroup(CONTEXT_USER_GROUP, attribute("attribute1"), attribute("attribute2"));

		final List<Attribute> attributes = strategy.retrieveAttributes(itemData);

		assertThat(attributes).extracting("qualifier").containsExactly("attribute1", "attribute2");
	}

	@Test
	public void testItemDataIsNull()
	{
		givenAttributesAvailableForUserGroup(CONTEXT_USER_GROUP, attribute("some_attribute"));

		final List<Attribute> attributes = strategy.retrieveAttributes(null);

		assertThat(attributes).isEmpty();
	}

	@Test
	public void testNotItemDataPassed()
	{
		givenAttributesAvailableForUserGroup(CONTEXT_USER_GROUP, attribute("some_attribute"));

		final List<Attribute> attributes = strategy.retrieveAttributes(new Object());

		assertThat(attributes).isEmpty();
	}

	private void givenAttributesAvailableForUserGroup(final String role, final Attribute... attributes)
	{
		strategy.setProviders(ImmutableMap.of(role, provider(attributes)));
	}

	private CanonicalItemAttributesProvider provider(final Attribute... attributes)
	{
		final CanonicalItemAttributesProvider provider = mock(CanonicalItemAttributesProvider.class);
		doReturn(Arrays.asList(attributes)).when(provider).getAttributes(any(ItemData.class));
		return provider;
	}

	private Attribute attribute(final String name)
	{
		final Attribute attr = new Attribute();
		attr.setQualifier(name);
		return attr;
	}
}