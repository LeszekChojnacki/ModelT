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
package de.hybris.platform.solrfacetsearch.provider;

/**
 * Expression evaluator interface to be used to evaluate any expressions such as spel
 */
public interface ExpressionEvaluator
{
	/**
	 * @param expression
	 * 		the expression to evaluate
	 * @param contex
	 * 		the contex that the expression will be evlauted from
	 *
	 * @return Object value of the evaluated expression
	 */
	Object evaluate(final String expression, final Object contex);
}
