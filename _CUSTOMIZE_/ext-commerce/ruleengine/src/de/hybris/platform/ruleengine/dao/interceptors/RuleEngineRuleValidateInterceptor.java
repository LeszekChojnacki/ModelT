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
package de.hybris.platform.ruleengine.dao.interceptors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.AbstractValidationResult;
import de.hybris.platform.ruleengine.versioning.RuleModelValidator;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import org.springframework.beans.factory.annotation.Required;


/**
 * AbstractRuleEngineRuleModel validation interceptor. Makes sure we do not save the model state, which could lead to
 * versioned instance inconsistency
 */
public class RuleEngineRuleValidateInterceptor implements ValidateInterceptor<DroolsRuleModel>
{

	private RuleModelValidator validator;

	@Override
	public void onValidate(final DroolsRuleModel droolsRule, final InterceptorContext ctx) throws InterceptorException
	{
		if (isEmpty(ctx.getElementsRegisteredFor(PersistenceOperation.DELETE)))
		{
			final AbstractValidationResult validationResult = getValidationResult(droolsRule, ctx);
			if (!validationResult.succeeded())
			{
				throw new InterceptorException(
						"Validation of " + droolsRule + " has failed with message '" + validationResult.getErrorMessage() + "'.", this);
			}
		}
	}

	protected AbstractValidationResult getValidationResult(final DroolsRuleModel droolsRule, final InterceptorContext ctx)
	{
		return getValidator().validate(droolsRule, ctx);
	}

	protected RuleModelValidator getValidator()
	{
		return validator;
	}

	@Required
	public void setValidator(final RuleModelValidator validator)
	{
		this.validator = validator;
	}



}
