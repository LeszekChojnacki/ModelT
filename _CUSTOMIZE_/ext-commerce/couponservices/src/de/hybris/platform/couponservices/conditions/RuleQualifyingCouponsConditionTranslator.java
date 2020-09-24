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
package de.hybris.platform.couponservices.conditions;

import static de.hybris.platform.ruledefinitions.conditions.builders.IrConditions.empty;

import de.hybris.platform.couponservices.constants.CouponServicesConstants;
import de.hybris.platform.couponservices.rao.CouponRAO;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Preconditions;

/**
 * Creates the intermediate representation of the CouponRAO.couponId condition.
 */
public class RuleQualifyingCouponsConditionTranslator implements RuleConditionTranslator
{
	public static final String COUPON_RAO_COUPON_ID_ATTRIBUTE = "couponId";
	public static final String COUPONS_PARAM = "coupons";

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		Preconditions.checkNotNull(context, "Rule Compiler Context is not expected to be NULL here");
		Preconditions.checkNotNull(condition, "Rule Condition Data is not expected to be NULL here");

		final RuleParameterData couponsParameter = condition.getParameters().get(COUPONS_PARAM);
		if (couponsParameter == null)
		{
			return empty();
		}
		final List<String> coupons = couponsParameter.getValue();
		if (CollectionUtils.isEmpty(coupons))
		{
			return empty();
		}

		final RuleIrAttributeCondition irCouponCondition = new RuleIrAttributeCondition();
		final String couponRaoVariable = context.generateVariable(CouponRAO.class);

		irCouponCondition.setVariable(couponRaoVariable);
		irCouponCondition.setAttribute(COUPON_RAO_COUPON_ID_ATTRIBUTE);
		irCouponCondition.setOperator(RuleIrAttributeOperator.IN);
		irCouponCondition.setValue(coupons);
		final Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(CouponServicesConstants.COUPON_IDS, coupons);
		irCouponCondition.setMetadata(metadata);
		return irCouponCondition;
	}
}
