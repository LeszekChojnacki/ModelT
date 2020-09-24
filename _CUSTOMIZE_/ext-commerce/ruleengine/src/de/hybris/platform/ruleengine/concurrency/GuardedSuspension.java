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
package de.hybris.platform.ruleengine.concurrency;

/**
 * Guarded suspension interface for concurrent executions preconditions checks
 */
public interface GuardedSuspension<T>
{
	/**
	 * Checks the preconditions based on <code>checkedProperty</code>
	 *
	 * @param checkedProperty
	 * 			 the object to check the preconditions against
	 * @return instance of {@link GuardStatus}
	 */
	GuardStatus checkPreconditions(T checkedProperty);

}
