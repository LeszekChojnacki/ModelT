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
package de.hybris.platform.promotionengineservices.interceptors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContextFactory;
import de.hybris.platform.ruleengineservices.compiler.impl.DefaultRuleCompilerContext;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;


/**
 * A prepare interceptor for AbstractRuleEngineRuleModel which creates a RuleBasedPromotion for the rule, if it doesn't
 * already exist. For existing {@code RuleBasedPromotion}s the interceptor also updates the the
 * {@link AbstractRuleEngineRuleModel#getMessageFired()}.
 */
public class PromotionRulePrepareInterceptor implements PrepareInterceptor<AbstractRuleEngineRuleModel>
{
	private ModelService modelService;
	private CommonI18NService commonI18NService;
	private RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory;
	private RuleDao ruleDao;

	@Override
	public void onPrepare(final AbstractRuleEngineRuleModel model, final InterceptorContext context) throws InterceptorException
	{
		if (!model.getRuleType().equals(RuleType.PROMOTION))
		{
			return;
		}

		doOnPrepare(model, context);
	}

	protected void doOnPrepare(final AbstractRuleEngineRuleModel model, final InterceptorContext context)
	{
		RuleBasedPromotionModel ruleBasedPromotion = model.getPromotion();
		if (isNull(ruleBasedPromotion) || !model.getVersion().equals(ruleBasedPromotion.getRuleVersion()))
		{
			ruleBasedPromotion = createNewPromotionAndAddToRuleModel(model);
		}

		final AbstractRuleModel rule = getRuleDao().findRuleByCode(model.getCode());
		if (nonNull(rule))
		{
			setLocalizedDescription(rule, ruleBasedPromotion);
			ruleBasedPromotion.setPriority(rule.getPriority());
			ruleBasedPromotion.setStartDate(rule.getStartDate());
			ruleBasedPromotion.setEndDate(rule.getEndDate());
			if (rule instanceof PromotionSourceRuleModel)
			{
				ruleBasedPromotion.setPromotionGroup(((PromotionSourceRuleModel) rule).getWebsite());
			}
			context.registerElementFor(ruleBasedPromotion, PersistenceOperation.SAVE);
		}

		// update the messageFired attribute if it has been modified
		if (context.isModified(model, AbstractRuleEngineRuleModel.MESSAGEFIRED)
				&& setLocalizedMessageFired(model, ruleBasedPromotion))
		{
			context.registerElementFor(ruleBasedPromotion, PersistenceOperation.SAVE);
		}
	}

	protected RuleBasedPromotionModel createNewPromotionAndAddToRuleModel(final AbstractRuleEngineRuleModel ruleModel)
	{
		final RuleBasedPromotionModel ruleBasedPromotion = getModelService().create(RuleBasedPromotionModel.class);
		// sets the rule version
		ruleBasedPromotion.setRuleVersion(ruleModel.getVersion());
		ruleBasedPromotion.setCode(ruleModel.getCode());
		ruleBasedPromotion.setTitle(ruleModel.getCode());

		setLocalizedMessageFired(ruleModel, ruleBasedPromotion);

		ruleBasedPromotion.setEnabled(Boolean.TRUE);
		ruleBasedPromotion.setRule(ruleModel);
		ruleModel.setPromotion(ruleBasedPromotion);

		return ruleBasedPromotion;
	}

	/**
	 * Copies attribute {@code messageFired} for all locales from given AbstractRuleEngineRuleModel to given
	 * RuleBasedPromotionModel.
	 *
	 * @param engineRule
	 *           AbstractRuleEngineRuleModel to copy messageFired from
	 * @param promotion
	 *           RuleBasedPromotionModel to update it's messageFired
	 * @return true if the promotion has been modified (i.e. the messageFired attribute has been modified in at least one
	 *         language), otherwise false
	 */
	protected boolean setLocalizedMessageFired(final AbstractRuleEngineRuleModel engineRule,
			final RuleBasedPromotionModel promotion)
	{
		boolean changed = false;
		for (final LanguageModel language : getCommonI18NService().getAllLanguages())
		{
			final Locale locale = getCommonI18NService().getLocaleForLanguage(language);
			promotion.setMessageFired(engineRule.getMessageFired(locale), locale);
			changed = true;
		}
		return changed;
	}

	/**
	 * Copies attribute {@code name} for all locals from given AbstractRuleModel to the {@code promotionDescription} of
	 * given RuleBasedPromotionModel.
	 *
	 * @param rule
	 *           AbstractRuleModel to copy name from
	 * @param promotion
	 *           RuleBasedPromotionModel to update it's promotionDescription
	 */
	protected void setLocalizedDescription(final AbstractRuleModel rule, final RuleBasedPromotionModel promotion)
	{
		for (final LanguageModel language : getCommonI18NService().getAllLanguages())
		{
			final Locale locale = getCommonI18NService().getLocaleForLanguage(language);
			promotion.setPromotionDescription(rule.getDescription(locale), locale);
		}
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

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected RuleCompilerContextFactory<DefaultRuleCompilerContext> getRuleCompilerContextFactory()
	{
		return ruleCompilerContextFactory;
	}

	@Required
	public void setRuleCompilerContextFactory(
			final RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory)
	{
		this.ruleCompilerContextFactory = ruleCompilerContextFactory;
	}

	protected RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
	}
}
