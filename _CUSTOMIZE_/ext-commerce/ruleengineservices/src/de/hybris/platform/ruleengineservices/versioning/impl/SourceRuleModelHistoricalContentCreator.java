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
package de.hybris.platform.ruleengineservices.versioning.impl;

import com.google.common.collect.Lists;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.ruleengineservices.versioning.HistoricalRuleContentProvider;
import de.hybris.platform.ruleengineservices.versioning.RuleModelHistoricalContentCreator;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Long.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;


/**
 * Helper class, that manages the historical version of the SourceRule object creation
 */
public class SourceRuleModelHistoricalContentCreator implements RuleModelHistoricalContentCreator
{
	private static final Logger LOG = LoggerFactory.getLogger(SourceRuleModelHistoricalContentCreator.class);

	private List<HistoricalRuleContentProvider> historicalRuleContentProviders;
	private RuleDao ruleDao;

	@Override
	public void createHistoricalVersion(@Nonnull final SourceRuleModel sourceRule, @Nonnull final InterceptorContext context)
			throws InterceptorException
	{
		checkArgument(nonNull(sourceRule), "Model should not be null here");
		checkArgument(nonNull(context), "InterceptorContext should not be null here");

		if (!isUnpublished(sourceRule, context))
		{
			createHistoricalVersionIfNeeded(sourceRule, context);
		}
	}

	protected boolean isUnpublished(final SourceRuleModel sourceRule, final InterceptorContext context)
	{
		return hasStatus(context, sourceRule, RuleStatus.UNPUBLISHED);
	}

	protected void createHistoricalVersionIfNeeded(final SourceRuleModel sourceRule, final InterceptorContext ctx)
			throws InterceptorException
	{
		if (historicalVersionMustBeCreated(sourceRule, ctx))
		{

			final Optional<AbstractRuleModel> latestUnpublishedRule = getRuleDao().findRuleByCodeAndStatus(sourceRule.getCode(),
					RuleStatus.UNPUBLISHED);
			if (latestUnpublishedRule.isPresent())
			{
				final AbstractRuleModel unpublishedRule = latestUnpublishedRule.get();
				throw new InterceptorException(
						"The modifications are allowed to be made for version [" + unpublishedRule.getVersion() + "] only");
			}
			final SourceRuleModel newSourceRuleVersion = doCreateHistoricalVersion(sourceRule, ctx);
			incrementRuleModelVersion(newSourceRuleVersion);
			newSourceRuleVersion.setStatus(RuleStatus.UNPUBLISHED);
			newSourceRuleVersion.setRulesModules(Lists.newArrayList());
			ctx.registerElementFor(newSourceRuleVersion, PersistenceOperation.SAVE);
			resetModifiedFields(sourceRule, ctx);
		}
		LOG.debug("Registering modified source rule model: PK={}, code={}, uuid={}, version={}", sourceRule.getPk(),
				sourceRule.getCode(), sourceRule.getUuid(), sourceRule.getVersion());
	}

	protected void resetModifiedFields(final SourceRuleModel toSourceRule, final InterceptorContext ctx)
	{
		if (isNull(toSourceRule))
		{
			LOG.warn("Target SourceRule is null");
			return;
		}
		final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(toSourceRule);
		final Map<String, Set<Locale>> dirtyAttributes = ctx.getDirtyAttributes(toSourceRule);
		dirtyAttributes.entrySet().forEach(e -> copyField(modelContext, toSourceRule, e));
	}

	protected void copyField(final ItemModelContext modelContext, final SourceRuleModel sourceRule,
			final Map.Entry<String, Set<Locale>> attribute)
	{
		final String propertyName = attribute.getKey();
		if (CollectionUtils.isEmpty(attribute.getValue()))
		{
			sourceRule.setProperty(propertyName, modelContext.getOriginalValue(propertyName));
		}
		else
		{
			attribute.getValue()
					.forEach(l -> sourceRule.setProperty(propertyName, l, modelContext.getOriginalValue(propertyName, l)));
		}
	}

	protected void incrementRuleModelVersion(final SourceRuleModel ruleModel)
	{
		final Long maxRuleVersion = getRuleDao().getRuleVersion(ruleModel.getCode());
		final long nextVersion = 1 + maxRuleVersion.longValue();
		ruleModel.setVersion(valueOf(nextVersion));
	}

	protected boolean historicalVersionMustBeCreated(final SourceRuleModel sourceRule, final InterceptorContext context)
	{
		return modelIsBeingModified(sourceRule, context) && modelIsValid(sourceRule);
	}

	protected boolean ruleStatusChangedToPublished(final SourceRuleModel model, final InterceptorContext context)
	{
		return !model.getStatus().equals(this.<RuleStatus>getOriginal(model, context, SourceRuleModel.STATUS))
				&& model.getStatus().equals(RuleStatus.PUBLISHED);
	}

	protected SourceRuleModel doCreateHistoricalVersion(final SourceRuleModel sourceRule, final InterceptorContext context)
	{
		final SourceRuleModel historicalSourceRule = context.getModelService().clone(sourceRule);
		putOriginalValuesIntoHistoricalVersion(sourceRule, historicalSourceRule, context);
		return historicalSourceRule;
	}

	protected void putOriginalValuesIntoHistoricalVersion(final SourceRuleModel sourceRule,
			final SourceRuleModel historicalSourceRule, final InterceptorContext ctx)
	{
		getHistoricalRuleContentProviders()
				.forEach(p -> p.copyOriginalValuesIntoHistoricalVersion(sourceRule, historicalSourceRule, ctx));
	}

	protected boolean modelIsValid(final SourceRuleModel sourceRule)
	{
		return nonNull(sourceRule.getActions()) && nonNull(sourceRule.getConditions());
	}

	protected boolean modelIsBeingModified(final AbstractRuleModel ruleModel, final InterceptorContext ctx)
	{
		return !ctx.isNew(ruleModel) && !ctx.isRemoved(ruleModel) &&
				(essentialFieldsAreModified(ruleModel, ctx) || associatedTypesChanged(ruleModel, ctx));
	}

	protected boolean essentialFieldsAreModified(final AbstractRuleModel ruleModel, final InterceptorContext ctx)
	{
		if(ctx.isModified(ruleModel))
		{
			final Map<String, Set<Locale>> dirtyAttributes = ctx.getDirtyAttributes(ruleModel);
			return isNotEmpty(dirtyAttributes) && dirtyAttributes.entrySet().stream()
					.anyMatch(e -> !matchAnyOf(e.getKey(), getNonEssentialAttributes()));
		}
		return false;
	}

	protected boolean associatedTypesChanged(final AbstractRuleModel ruleModel, final InterceptorContext ctx)  // NOSONAR
	{
		return false;
	}

	protected String[] getNonEssentialAttributes()
	{
		return new String[] { SourceRuleModel.ENGINERULES, SourceRuleModel.STATUS, SourceRuleModel.VERSION,
				SourceRuleModel.RULESMODULES };
	}

	protected boolean matchAnyOf(final String sample, final String... probes)
	{
		if (nonNull(sample) && isNotEmpty(probes))
		{
			for (final String probe : probes)
			{
				if (sample.equals(probe))
				{
					return true;
				}
			}
		}
		return false;
	}

	protected boolean hasStatus(final InterceptorContext ctx, final SourceRuleModel sourceRule, final RuleStatus ruleStatus)
	{
		final RuleStatus originalRuleStatus = getOriginal(sourceRule, ctx, SourceRuleModel.STATUS);
		return ruleStatus.equals(originalRuleStatus);
	}

	protected <T> T getOriginal(final SourceRuleModel sourceRule, final InterceptorContext context,
			final String attributeQualifier)
	{
		if (context.isModified(sourceRule, attributeQualifier))
		{
			final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(sourceRule);
			return modelContext.getOriginalValue(attributeQualifier);
		}
		final ModelService modelService = requireNonNull(context.getModelService());
		try
		{
			return modelService.getAttributeValue(sourceRule, attributeQualifier);
		}
		catch (final RuntimeException e)
		{
			LOG.error("Exception caught: ", e);
			return null;
		}
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

	protected List<HistoricalRuleContentProvider> getHistoricalRuleContentProviders()
	{
		return historicalRuleContentProviders;
	}

	@Required
	public void setHistoricalRuleContentProviders(final List<HistoricalRuleContentProvider> historicalRuleContentProviders)
	{
		this.historicalRuleContentProviders = historicalRuleContentProviders;
	}

}
