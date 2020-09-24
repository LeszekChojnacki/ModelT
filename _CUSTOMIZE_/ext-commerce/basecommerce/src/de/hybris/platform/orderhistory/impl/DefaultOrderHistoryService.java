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
package de.hybris.platform.orderhistory.impl;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.jalo.BasecommerceManager;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.OrderCloneHelper;
import de.hybris.platform.orderhistory.OrderHistoryService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * Default Implementation class of {@link OrderHistoryService}
 */
public class DefaultOrderHistoryService implements OrderHistoryService
{

	private KeyGenerator versionIDGenerator;
	private ModelService modelService;

	public KeyGenerator getVersionIDGenerator()
	{
		return versionIDGenerator;
	}

	@Required
	public void setVersionIDGenerator(final KeyGenerator versionIDGenerator)
	{
		this.versionIDGenerator = versionIDGenerator;
	}

	@Override
	public OrderModel createHistorySnapshot(final OrderModel currentVersion)
	{
		if (currentVersion == null)
		{
			throw new IllegalArgumentException("current order version was null"); //NOPMD
		}
		if (currentVersion.getVersionID() != null)
		{
			throw new IllegalArgumentException("order is already snapshot");
		}
		final OrderModel copy = getModelService().clone(currentVersion);
		copy.setVersionID((String) getVersionIDGenerator().generate());
		copy.setOriginalVersion(currentVersion);
		return copy;
	}

	@Override
	public void saveHistorySnapshot(final OrderModel snapshot)
	{
		if (snapshot == null)
		{
			throw new IllegalArgumentException("snapshot was null");//NOPMD
		}
		if (snapshot.getVersionID() == null)
		{
			throw new IllegalArgumentException("order is no snapshot");
		}
		if (!getModelService().isNew(snapshot))
		{
			throw new IllegalArgumentException("snapshot has already been persisted");
		}

		getModelService().save(snapshot);
		final Order copyItem = getModelService().getSource(snapshot);
		final Order originalItem = BasecommerceManager.getInstance().getOriginalVersion(copyItem);
		if (originalItem != null)
		{
			OrderCloneHelper.postProcess(originalItem, copyItem);
			getModelService().refresh(snapshot);
			for (final AbstractOrderEntryModel entry : snapshot.getEntries())
			{
				getModelService().refresh(entry);
			}
		}
	}

	@Override
	public Collection<OrderModel> getHistorySnapshots(final OrderModel ownerOrder)
	{
		if (ownerOrder == null)
		{
			throw new JaloInvalidParameterException("Missing ORDER for getting history entries ", 0);
		}
		Collection<OrderModel> result = null;

		final List<OrderHistoryEntryModel> entries = ownerOrder.getHistoryEntries();

		for (final OrderHistoryEntryModel entry : entries)
		{
			final OrderModel snapshot = entry.getOrder();
			if (snapshot != null && snapshot.getVersionID() != null)
			{
				if (result == null)
				{
					result = new ArrayList<OrderModel>(entries.size()); // lazy
				}
				result.add(snapshot);
			}
		}

		return result == null ? Collections.emptyList() : result;

	}

	/**
	 * Returns collection of {@link OrderHistoryEntryModel} or {@link OrderHistoryEntryModel#DESCRIPTION} for a given
	 * time range
	 *
	 * @param ownerOrder
	 *           - required
	 * @param dateFrom
	 *           - possible null value
	 * @param dateTo
	 *           - possible null value
	 * @param descriptionOnly
	 *           - set to true to get descriptions only instead of the whole {@link OrderHistoryEntryModel} objects
	 * @return Collection
	 */
	protected Collection getHistoryEntries(final OrderModel ownerOrder, final Date dateFrom, final Date dateTo,
			final boolean descriptionOnly)
	{
		if (ownerOrder == null)
		{
			throw new JaloInvalidParameterException("Missing ORDER for getting history entries ", 0);
		}
		//if date limits are not specified - return all order's history entries
		if (null == dateFrom && null == dateTo)
		{
			//if descriptionOnly is false, return collection of OrderHistoryEntryModel
			if (descriptionOnly)
			//otherwise return only descriptions
			{
				return getEntriesDescriptions(ownerOrder.getHistoryEntries());
			}
			else
			{
				return ownerOrder.getHistoryEntries();
			}

		}

		final List<OrderHistoryEntryModel> entries = ownerOrder.getHistoryEntries();
		final List<OrderHistoryEntryModel> result = entries.stream().filter(e -> nonNull(e.getTimestamp()))
				.filter(e -> isWithingDateRange(e, dateFrom, dateTo)).collect(toList());

		if (descriptionOnly)
		{
			return getEntriesDescriptions(result);
		}
		else
		{
			return result;
		}

	}

	protected boolean isWithingDateRange(final OrderHistoryEntryModel entry, final Date from, final Date to)
	{
		boolean isInRange = false;
		if (null != from && from.before(entry.getTimestamp()))
		{
			if (null == to || to.after(entry.getTimestamp()))
			{
				isInRange = true;

			}
		}
		else if (null != to && to.after(entry.getTimestamp())
				&& (null == from || from.before(entry.getTimestamp())))
		{
			isInRange = true;
		}
		return isInRange;
	}

	@Override
	public Collection<OrderHistoryEntryModel> getHistoryEntries(final OrderModel ownerOrder, final EmployeeModel employee)
	{

		if (employee == null)
		{
			throw new JaloInvalidParameterException("Missing EMPLOYEE for getting history entries ", 0);
		}
		if (ownerOrder == null)
		{
			throw new JaloInvalidParameterException("Missing ORDER for getting history entries ", 0);
		}

		return ownerOrder.getHistoryEntries().stream()
				.filter(entry -> nonNull(entry.getEmployee()) && entry.getEmployee().equals(employee)).collect(Collectors.toList());
	}

	@Override
	public Collection<OrderHistoryEntryModel> getHistoryEntries(final UserModel user, final Date dateFrom, final Date dateTo)
	{
		if (user == null)
		{
			throw new JaloInvalidParameterException("Missing USER for getting history entries ", 0);
		}
		final Collection<OrderHistoryEntryModel> result = new ArrayList<>();
		final Collection<OrderModel> orders = user.getOrders();

		for (final OrderModel order : orders)
		{
			result.addAll(getHistoryEntries(order, dateFrom, dateTo, false));
		}
		return result;
	}

	/**
	 * Fetches the collection of history entries descriptions
	 *
	 * @param entries
	 *           history entries that you want to fetch descriptions from
	 * @return collection of history entries description
	 */
	protected Collection<String> getEntriesDescriptions(final Collection<OrderHistoryEntryModel> entries)
	{
		return entries.stream().map(OrderHistoryEntryModel::getDescription).collect(Collectors.toList());
	}

	@Override
	public Collection<OrderHistoryEntryModel> getHistoryEntries(final OrderModel ownerOrder, final Date dateFrom,
			final Date dateTo)
	{
		return getHistoryEntries(ownerOrder, dateFrom, dateTo, false);
	}

	@Override
	public Collection<String> getHistoryEntriesDescriptions(final OrderModel ownerOrder, final Date dateFrom, final Date dateTo)
	{
		return getHistoryEntries(ownerOrder, dateFrom, dateTo, true);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
