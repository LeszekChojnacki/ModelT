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
package de.hybris.order.calculation.domain;

import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Represents the tax, which applies generally to line items or additional charges.
 */
public class Tax
{
	private final AbstractAmount amount;
	private List<Taxable> targets = new ArrayList<>();

	/**
	 * Creates a new tax with a given amount.
	 */
	public Tax(final AbstractAmount amount)
	{
		if (amount == null)
		{
			throw new IllegalArgumentException("Given amount is null!");
		}
		this.amount = amount;
	}

	/**
	 * Returns the amount of this tax. May be either {@link Percentage} or {@link Money} in some rare occasions.
	 */
	public AbstractAmount getAmount()
	{
		return amount;
	}

	/**
	 * Adds a new target to this tax.
	 */
	public void addTarget(final Taxable target)
	{
		if (!targets.contains(target)) // NOSONAR
		{
			this.targets.add(target);
		}
	}

	/**
	 * Adds multiple targets to this tax.
	 */
	public void addTargets(final List<? extends Taxable> targets)
	{
		this.targets = new ArrayList(targets);
	}

	/**
	 * Adds multiple targets to this tax.
	 */
	public void addTargets(final Taxable... targets)
	{
		addTargets(Arrays.asList(targets));
	}

	/**
	 * Returns all targets of this tax.
	 */
	public Collection<Taxable> getTargets()
	{
		return Collections.unmodifiableList(targets);
	}

	/**
	 * Removes a single target from this tax.
	 */
	public void removeTarget(final Taxable target)
	{
		if (!this.targets.remove(target))
		{
			throw new IllegalArgumentException("Tax target " + target + " doesnt belong to tax " + this + " - cannot remove.");
		}
	}

	public void clearTargets()
	{
		this.targets.clear();
	}

	@Override
	public String toString()
	{
		return "Tax:" + amount.toString() + " Items:" + this.targets;
	}
}
