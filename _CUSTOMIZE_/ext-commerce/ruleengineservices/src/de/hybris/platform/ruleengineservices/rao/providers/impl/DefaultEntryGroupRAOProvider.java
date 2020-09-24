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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.order.EntryGroup;
import de.hybris.platform.order.EntryGroupService;
import de.hybris.platform.ruleengineservices.rao.OrderEntryGroupRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * The default implementation for the order {@code RAO}
 */
public class DefaultEntryGroupRAOProvider implements RAOProvider<AbstractOrderModel>
{
	private EntryGroupService entryGroupService;
	private Converter<EntryGroup, OrderEntryGroupRAO> entryGroupRaoConverter;

	@Override
	public Set expandFactModel(final AbstractOrderModel order)
	{
		if (isEmpty(order.getEntryGroups()))
		{
			return emptySet();
		}

		final Set<OrderEntryGroupRAO> entryGroups = newHashSet();

		for (final AbstractOrderEntryModel orderEntry : order.getEntries())
		{
			for (final Integer entryGroupNumber : orderEntry.getEntryGroupNumbers())
			{
				final EntryGroup rootEntryGroup = getEntryGroupService().getRoot(orderEntry.getOrder(), entryGroupNumber);

				final OrderEntryGroupRAO rootEntryGroupRao = getEntryGroupRaoConverter().convert(rootEntryGroup);

				entryGroups.addAll(collectNestedGroups(rootEntryGroup, rootEntryGroupRao));
			}
		}

		return entryGroups;
	}

	protected Set<OrderEntryGroupRAO> collectNestedGroups(final EntryGroup rootEntryGroup,
			final OrderEntryGroupRAO rootEntryGroupRao)
	{
		return getEntryGroupService().getNestedGroups(rootEntryGroup).stream().map(eg ->
		{
			final OrderEntryGroupRAO rao = getEntryGroupRaoConverter().convert(eg);
			rao.setRootEntryGroup(rootEntryGroupRao);
			return rao;
		}).collect(toSet());
	}

	protected EntryGroupService getEntryGroupService()
	{
		return entryGroupService;
	}

	@Required
	public void setEntryGroupService(final EntryGroupService entryGroupService)
	{
		this.entryGroupService = entryGroupService;
	}


	protected Converter<EntryGroup, OrderEntryGroupRAO> getEntryGroupRaoConverter()
	{
		return entryGroupRaoConverter;
	}

	@Required
	public void setEntryGroupRaoConverter(final Converter<EntryGroup, OrderEntryGroupRAO> entryGroupRaoConverter)
	{
		this.entryGroupRaoConverter = entryGroupRaoConverter;
	}
}
