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
 *
 */
package de.hybris.platform.warehousing.sourcing.filter;

/**
 * Operator to determine if the sourcing location result sets should be an inclusion set (OR) or an exclusion set (AND).
 * Use NONE for the first filter in the chain of sourcing filters. NONE behaves the same way as the OR operator.
 */
public enum SourcingFilterResultOperator
{
	AND, OR, NOT, NONE;
}
