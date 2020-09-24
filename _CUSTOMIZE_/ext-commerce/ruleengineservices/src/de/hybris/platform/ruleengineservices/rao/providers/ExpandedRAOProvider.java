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
package de.hybris.platform.ruleengineservices.rao.providers;

import java.util.Collection;
import java.util.Set;


/**
 * A ExpandedRAOProvider allows to expand RAO objects (according to the options) to be inserted into the rule engine for
 * evaluation purposes.
 */
public interface ExpandedRAOProvider<T> extends RAOProvider<T>
{
	Set expandFactModel(T modelFact, final Collection<String> options);
}
