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

import java.util.Set;


/**
 * A RAOProvider allows to extract RAO objects to be inserted into the rule engine for evaluation purposes.
 */
public interface RAOProvider<T>
{
	Set expandFactModel(T modelFact);
}
