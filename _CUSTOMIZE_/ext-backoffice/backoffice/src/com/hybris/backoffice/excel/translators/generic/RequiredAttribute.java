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
package com.hybris.backoffice.excel.translators.generic;

import de.hybris.platform.core.model.type.TypeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Represents hierarchical structure of required attributes. Children of each level are sorted by their branch length.
 * In other words, children are sorted by the number of descendants. For example: CatalogVersion(version[0 descendants],
 * catalog[1 descendant](id[0 descendants]))
 */
public class RequiredAttribute implements Comparable<RequiredAttribute>
{
	private final String enclosingType;
	private final String qualifier;
	private final TypeModel typeModel;
	private final boolean unique;
	private final boolean mandatory;
	private final boolean partOf;
	private RequiredAttribute parent;
	private final List<RequiredAttribute> children = new ArrayList<>();

	public RequiredAttribute(final TypeModel typeModel, final String enclosingType, final String qualifier, final boolean unique,
			final boolean mandatory, final boolean partOf)
	{
		this.typeModel = typeModel;
		this.enclosingType = enclosingType;
		this.qualifier = qualifier;
		this.unique = unique;
		this.mandatory = mandatory;
		this.partOf = partOf;
	}

	public TypeModel getTypeModel()
	{
		return typeModel;
	}

	public boolean isUnique()
	{
		return unique;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void addChild(final RequiredAttribute child)
	{
		child.parent = this;
		children.add(child);
		Collections.sort(children);
	}

	public String getEnclosingType()
	{
		return enclosingType;
	}


	public String getQualifier()
	{
		return qualifier;
	}

	public List<RequiredAttribute> getChildren()
	{
		return children;
	}

	public boolean isPartOf()
	{
		return partOf;
	}

	private int calculateBranchDepth()
	{
		int sum = children.size();
		for (final RequiredAttribute child : children)
		{
			sum += child.calculateBranchDepth();
		}
		return sum;
	}

	public boolean isRoot()
	{
		return parent == null;
	}

	@Override
	public int compareTo(final RequiredAttribute o)
	{
		return calculateBranchDepth() - o.calculateBranchDepth();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null)
		{
			return false;
		}
		if (o.getClass() != this.getClass())
		{
			return false;
		}
		final RequiredAttribute that = (RequiredAttribute) o;
		return Objects.equals(getEnclosingType(), that.getEnclosingType()) && Objects.equals(getQualifier(), that.getQualifier());
	}



	@Override
	public int hashCode()
	{
		return Objects.hash(getEnclosingType(), getQualifier());
	}
}
