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

import de.hybris.platform.ruleengineservices.enums.FactContextType;
import de.hybris.platform.ruleengineservices.rao.providers.impl.FactContext;

import java.util.Collection;


/**
 * Creates instances of {@link FactContext}.
 *
 */
public interface FactContextFactory
{
	FactContext createFactContext(final FactContextType type, final Collection<?> facts);
}
