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

package de.hybris.platform.configurablebundlecockpits.services.search.impl;

import de.hybris.platform.cockpit.model.search.SearchParameterValue;
import de.hybris.platform.cockpit.services.search.ConditionTranslatorContext;
import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericSearchField;

import org.apache.log4j.Logger;


public class ReferenceGenericQueryConditionTranslator extends
		de.hybris.platform.cockpit.services.search.impl.ReferenceGenericQueryConditionTranslator
{
	private static final String IS_EMPTY = "isEmpty";
	private static final Logger LOG = Logger.getLogger(ReferenceGenericQueryConditionTranslator.class);

	@Override
	public GenericCondition translate(final SearchParameterValue paramValue, final ConditionTranslatorContext ctx)
	{
		if (IS_EMPTY.equals(paramValue.getOperator().getQualifier()))
		{
			final GenericSearchField field = new GenericSearchField(getTypeService().getAttributeCodeFromPropertyQualifier(
					paramValue.getParameterDescriptor().getQualifier()));
			return GenericCondition.createIsNullCondition(field);
		}
		else
		{
			LOG.error(String.format("Operator '%s' is not supported by %s. Condition will be ignored.", paramValue.getOperator()
					.getQualifier(), this.getClass().getName()));
			return null;
		}
	}
}
