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

import de.hybris.platform.datahubbackoffice.service.datahub.UserContext;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.dto.item.ItemData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

/**
 * An implementation of the {@code AttributesRetrievalStrategy}, which uses different attributes provider based on the context user role (aka User Group).
 * The providers can implement different approaches for retrieving item attributes and/or restrict access to all or some attributes.
 */
public class UserGroupBasedAttributesRetrievalStrategy implements AttributesRetrievalStrategy
{
	private final CanonicalItemAttributesProvider fallbackProvider = new RestrictedAttributesProvider();
	private final Map<String, CanonicalItemAttributesProvider> providerMap = new HashMap<>();

	private UserContext userContext;

	@Override
	public List<Attribute> retrieveAttributes(final Object object)
	{
		return object instanceof ItemData
				? findProviderForTheContextUserGroup().getAttributes((ItemData) object)
				: Collections.emptyList();
	}

	private CanonicalItemAttributesProvider findProviderForTheContextUserGroup()
	{
		final Optional<String> contextUserGroup = providerMap.keySet().stream()
				.filter(userContext::isMemberOf)
				.findAny();
		return contextUserGroup.map(providerMap::get)
				.orElse(fallbackProvider);
	}

	public void setProviders(final Map<String, CanonicalItemAttributesProvider> providers)
	{
		providerMap.putAll(providers);
	}

	@Required
	public void setUserContext(final UserContext context)
	{
		userContext = context;
	}
}
