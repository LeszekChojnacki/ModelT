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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.hybris.platform.promotionengineservices.model.PromotionActionParameterModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPotentialPromotionMessageActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotionengineservices.promotionengine.coupons.CouponCodeRetrievalStrategy;
import de.hybris.platform.promotions.PromotionResultService;
import de.hybris.platform.promotions.impl.DefaultPromotionResultService;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.services.RuleParametersService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of PromotionResultService
 *
 */
public class DefaultPromotionEngineResultService implements PromotionResultService
{
	protected static final String EMPTY_VALUE = "?";

	protected static final Pattern LIST_PATTERN = Pattern.compile("^List\\((.*)\\)");
	protected static final Pattern MAP_PATTERN = Pattern.compile("^Map\\((.+),\\s*(.+)\\)");

	private static final Logger LOG = LoggerFactory.getLogger(DefaultPromotionEngineResultService.class);

	private CouponCodeRetrievalStrategy couponCodeRetrievalStrategy;
	private DefaultPromotionResultService defaultPromotionResultService;
	private CommonI18NService commonI18NService;
	private ModelService modelService;

	private Map<String, PromotionMessageParameterResolutionStrategy> resolutionStrategies;

	private RuleParametersService ruleParametersService;

	@Override
	public String getDescription(final PromotionResultModel promotionResult)
	{
		return getDescription(promotionResult, null);
	}

	@Override
	public boolean apply(final PromotionResultModel promotionResult)
	{
		return !isRuleBasedPromotion(promotionResult.getPromotion()) && getDefaultPromotionResultService().apply(promotionResult);
	}

	@Override
	public long getConsumedCount(final PromotionResultModel promotionResult, final boolean paramBoolean)
	{
		return !isRuleBasedPromotion(promotionResult.getPromotion())
				? getDefaultPromotionResultService().getConsumedCount(promotionResult, paramBoolean) : 0L;
	}

	@Override
	public boolean getCouldFire(final PromotionResultModel promotionResult)
	{
		return isRuleBasedPromotion(promotionResult.getPromotion()) ? promotionResult.getCertainty().floatValue() < 1.0F
				: getDefaultPromotionResultService().getCouldFire(promotionResult);
	}

	@Override
	public String getDescription(final PromotionResultModel promotionResult, final Locale locale)
	{
		final Locale localeToUse = isNull(locale)
				? getCommonI18NService().getLocaleForLanguage(getCommonI18NService().getCurrentLanguage()) : locale;

		if (isRuleBasedPromotion(promotionResult.getPromotion()))
		{
			final RuleBasedPromotionModel promotion = (RuleBasedPromotionModel) promotionResult.getPromotion();
			final String messageFiredPositional = promotion.getMessageFired();
			try
			{
				if (isEmpty(messageFiredPositional))
				{
					// nothing to replace, so return the string as is
					return messageFiredPositional;
				}

				final AbstractRuleEngineRuleModel rule = promotion.getRule();
				if (isNull(rule))
				{
					// nothing to replace, so return the string as is
					LOG.warn("promotion {} has no corresponding rule. Cannot substitute message parameters, returning message as is.",
							promotion.getCode());
					return messageFiredPositional;
				}

				List<RuleParameterData> parameters = null;
				final String paramString = rule.getRuleParameters();
				if (nonNull(paramString))
				{
					parameters = getRuleParametersService().convertParametersFromString(paramString);
				}

				if (isNull(parameters))
				{
					// nothing to replace, so return the string as is
					LOG.warn(
							"rule with code {} has no rule parameters. Cannot substitute message parameters, returning message as is.",
							rule.getCode());
					return messageFiredPositional;
				}
				else if (nonNull(promotionResult.getActions()))
				{
					final Map<String, Object> messageActionValues = promotionResult.getActions().stream()
							.filter(action -> action instanceof RuleBasedPotentialPromotionMessageActionModel)
							.flatMap(action -> ((RuleBasedPotentialPromotionMessageActionModel) action).getParameters().stream())
							.collect(Collectors.toMap(PromotionActionParameterModel::getUuid, PromotionActionParameterModel::getValue));

					logMissingParametersResolutionStrategies(parameters, messageActionValues);

					parameters = parameters.stream()
							.map(parameter -> replaceRuleParameterValue(promotionResult, messageActionValues, parameter))
							.collect(Collectors.toList());
				}

				return getMessageWithResolvedParameters(promotionResult, localeToUse, messageFiredPositional, parameters);
			}
			catch (final Exception e)
			{
				LOG.error("error during promotion message calculation, returning empty string", e);
				return messageFiredPositional;
			}
		}
		else
		{
			// allow support for display of legacy data (applicable only if the promotion result was persisted)
			if (!getModelService().isNew(promotionResult))
			{
				return getDefaultPromotionResultService().getDescription(promotionResult, localeToUse);
			}
			return null;
		}
	}

	protected void logMissingParametersResolutionStrategies(final List<RuleParameterData> parameters,
			final Map<String, Object> messageActionValues)
	{
		if (LOG.isWarnEnabled())
		{
			parameters.stream()
					.filter(parameter -> messageActionValues.containsKey(parameter.getUuid())
							&& !getResolutionStrategies().containsKey(parameter.getType()))
					.forEach(
							parameter -> LOG.warn("Parameter {} has to be replaced but resolution strategy for type {} is not defined",
									parameter.getUuid(), parameter.getType()));
		}
	}

	protected boolean isRuleBasedPromotion(final AbstractPromotionModel abstractPromotion)
	{
		return abstractPromotion instanceof RuleBasedPromotionModel;
	}

	protected RuleParameterData replaceRuleParameterValue(final PromotionResultModel promotionResult,
			final Map<String, Object> messageActionValues, final RuleParameterData parameter)
	{
		return (messageActionValues.containsKey(parameter.getUuid()) && getResolutionStrategies().containsKey(parameter.getType()))
				? getResolutionStrategies().get(parameter.getType()).getReplacedParameter(parameter, promotionResult,
						messageActionValues.get(parameter.getUuid()))
				: parameter;
	}

	/**
	 * Formats message by resolving placeholders defined in the message. In case a placeholder cannot be evaluated the
	 * placeholder key is preserved in the message
	 *
	 * @param promotionResult
	 * @param locale
	 * @param messageFiredPositional
	 * @param parameters
	 * @return formatted message with resolved parameters
	 */
	protected String getMessageWithResolvedParameters(final PromotionResultModel promotionResult, final Locale locale,
			final String messageFiredPositional, final List<RuleParameterData> parameters)
	{
		final Map<String, Object> valuesMap = new HashMap();
		for (final RuleParameterData parameter : parameters)
		{
			if (messageFiredPositional.contains(parameter.getUuid()))
			{
				valuesMap.put(parameter.getUuid(), resolveParameterValue(parameter, promotionResult, locale));
			}
		}
		final String substitorInputMessage = messageFiredPositional.replace("{", "${");
		final String resolvedMessage = new StrSubstitutor(valuesMap).replace(substitorInputMessage);
		if (resolvedMessage.contains("${"))
		{
			logUnresolvedPlaceholder(promotionResult, resolvedMessage);
			return resolvedMessage.replace("${", "{");
		}
		return resolvedMessage;
	}

	protected void logUnresolvedPlaceholder(final PromotionResultModel promotionResult, final String resolvedMessage)
	{
		LOG.info("One of message placeholders cannot be filled for the \"{}\" promotion and message \"{}\"",
				promotionResult.getPromotion().getCode(), resolvedMessage);
	}

	protected Object resolveParameterValue(final RuleParameterData parameter, final PromotionResultModel promotionResult,
			final Locale locale)
	{
		if (isNull(parameter.getValue()))
		{
			final Matcher listMatcher = LIST_PATTERN.matcher(parameter.getType());
			if (listMatcher.matches())
			{
				return Collections.emptyList();
			}

			final Matcher mapMatcher = MAP_PATTERN.matcher(parameter.getType());
			if (mapMatcher.matches())
			{
				return Collections.emptyMap();
			}

			return EMPTY_VALUE;
		}

		if (MapUtils.isNotEmpty(getResolutionStrategies()))
		{
			final PromotionMessageParameterResolutionStrategy strategy = getResolutionStrategies().get(parameter.getType());
			if (strategy != null)
			{
				return strategy.getValue(parameter, promotionResult, locale);
			}
		}

		return parameter.getValue();
	}
	

	@Override
	public boolean getFired(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return promotionResult.getCertainty().floatValue() >= 1.0F;
		}
		else
		{
			return getDefaultPromotionResultService().getFired(promotionResult);
		}
	}

	@Override
	public double getTotalDiscount(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return 0;
		}
		else
		{
			return getDefaultPromotionResultService().getTotalDiscount(promotionResult);
		}
	}

	@Override
	public boolean isApplied(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return false;
		}
		else
		{
			return getDefaultPromotionResultService().isApplied(promotionResult);
		}
	}

	@Override
	public boolean isAppliedToOrder(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return false;
		}
		else
		{
			return getDefaultPromotionResultService().isAppliedToOrder(promotionResult);
		}
	}

	@Override
	public boolean undo(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return false;
		}
		else
		{
			return getDefaultPromotionResultService().undo(promotionResult);
		}
	}

	@Override
	public List<PromotionResultModel> getPotentialProductPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return Collections.emptyList();
		}
		else
		{
			return getDefaultPromotionResultService().getPotentialProductPromotions(promoResult, promotion);
		}
	}

	@Override
	public List<PromotionResultModel> getPotentialOrderPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return Collections.emptyList();
		}
		else
		{
			return getDefaultPromotionResultService().getPotentialOrderPromotions(promoResult, promotion);
		}
	}

	@Override
	public List<PromotionResultModel> getFiredProductPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return Collections.emptyList();
		}
		else
		{
			return getDefaultPromotionResultService().getFiredProductPromotions(promoResult, promotion);
		}
	}

	@Override
	public List<PromotionResultModel> getFiredOrderPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		if (promotion instanceof RuleBasedPromotionModel)
		{
			return Collections.emptyList();
		}
		else
		{
			return getDefaultPromotionResultService().getFiredOrderPromotions(promoResult, promotion);
		}
	}


	@Override
	public Optional<Set<String>> getCouponCodesFromPromotion(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return getCouponCodeRetrievalStrategy().getCouponCodesFromPromotion(promotionResult);
		}
		else
		{
			return getDefaultPromotionResultService().getCouponCodesFromPromotion(promotionResult);
		}
	}

	protected DefaultPromotionResultService getDefaultPromotionResultService()
	{
		return defaultPromotionResultService;
	}

	@Required
	public void setDefaultPromotionResultService(final DefaultPromotionResultService defaultPromotionResultService)
	{
		this.defaultPromotionResultService = defaultPromotionResultService;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected Map<String, PromotionMessageParameterResolutionStrategy> getResolutionStrategies()
	{
		return resolutionStrategies;
	}

	@Required
	public void setResolutionStrategies(final Map<String, PromotionMessageParameterResolutionStrategy> resolutionStrategies)
	{
		this.resolutionStrategies = resolutionStrategies;
	}

	protected RuleParametersService getRuleParametersService()
	{
		return ruleParametersService;
	}

	@Required
	public void setRuleParametersService(final RuleParametersService ruleParametersService)
	{
		this.ruleParametersService = ruleParametersService;
	}

	protected CouponCodeRetrievalStrategy getCouponCodeRetrievalStrategy()
	{
		return couponCodeRetrievalStrategy;
	}

	@Required
	public void setCouponCodeRetrievalStrategy(final CouponCodeRetrievalStrategy couponCodeRetrievalStrategy)
	{
		this.couponCodeRetrievalStrategy = couponCodeRetrievalStrategy;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
