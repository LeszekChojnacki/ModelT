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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;

import java.io.Serializable;


/**
 * The Class represents a logically coupled Facet Query Fieled Constraint. </br> I.e:
 * <code>((catalogId:TestCatalog) AND (version:(Online OR Staged)) OR ((catalogId:(TestCatalogA OR TestCatalogB)) AND (version:Online))).</code>
 * The inner constraints are represented by {@link QueryField}s which can be joined with the
 * <code>innerCouplingOperator</code>.<br>
 * You can group more than one {@link CoupledQueryField} just by setting the same
 * <code>coupleId<code> and some  <code>outerCouplingOperator</code>.
 */
public class CoupledQueryField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String coupleId;

	private final QueryField field1;
	private final QueryField field2;

	private final Operator innerCouplingOperator;
	private final Operator outerCouplingOperator;

	/**
	 * @param coupleId
	 *           - couple ID to identify groups.
	 * @param field1
	 *           - {@link QueryField} - left query field for {@link #innerCouplingOperator}
	 * @param field2
	 *           - {@link QueryField} - right query field for {@link #innerCouplingOperator}
	 * @param innerCouplingOperator
	 *           - operator joining {@link #field1} and {@link #field2}
	 * @param outerCouplingOperator
	 *           - operator to join couples with the same {@link #coupleId}
	 */
	public CoupledQueryField(final String coupleId, final QueryField field1, final QueryField field2,
			final Operator innerCouplingOperator, final Operator outerCouplingOperator)
	{
		this.coupleId = coupleId;
		this.field1 = field1;
		this.field2 = field2;
		this.innerCouplingOperator = innerCouplingOperator;
		this.outerCouplingOperator = outerCouplingOperator;
	}

	public String getCoupleId()
	{
		return coupleId;
	}

	public QueryField getField1()
	{
		return field1;
	}

	public QueryField getField2()
	{
		return field2;
	}

	public Operator getInnerCouplingOperator()
	{
		return innerCouplingOperator;
	}

	public Operator getOuterCouplingOperator()
	{
		return outerCouplingOperator;
	}
}
