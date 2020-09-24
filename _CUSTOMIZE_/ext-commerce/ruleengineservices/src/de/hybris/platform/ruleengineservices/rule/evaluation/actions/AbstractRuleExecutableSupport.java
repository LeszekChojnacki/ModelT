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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MODULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_CODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_EXCLUSIVE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengineservices.calculation.EntriesSelectionStrategy;
import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.enums.OrderEntrySelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductConsumedRAO;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.impl.RuleAndRuleGroupExecutionTracker;
import de.hybris.platform.ruleengineservices.util.CurrencyUtils;
import de.hybris.platform.ruleengineservices.util.RaoUtils;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



public class AbstractRuleExecutableSupport implements BeanNameAware, RAOAction
{

	private RaoUtils raoUtils;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractRuleExecutableSupport.class);

	private ConfigurationService configurationService;
	private RuleEngineCalculationService ruleEngineCalculationService;
	private Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> entriesSelectionStrategies;
	private Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> qualifyingEntriesSelectionStrategies;
	private CurrencyUtils currencyUtils;
	private List<ActionSupplementStrategy> actionSupplementStrategies;

	private String beanName;

	private boolean useDeprecatedRRDs = true;

	private boolean consumptionEnabled;

	/**
	 * @deprecated since 6.6 Use the method w/o 'parameters' argument instead. {@link RuleActionContext} should
	 * encapsulate this information.
	 */
	@Override
	@Deprecated
	public void performAction(final RuleActionContext context, final Map<String, Object> parameters)
	{
		if (nonNull(context.getParameters()))
		{
			throw new IllegalStateException(
					"Use method RAOAction.performAction(RuleActionContext context) instead for context with set parameters.");
		}
		context.setParameters(parameters);
		this.performAction(context);
	}

	@Override
	public void performAction(final RuleActionContext context)
	{
		validateParameters(context.getParameters());
		validateRule(context);

		if (performActionInternal(context))
		{
			trackConsumedProducts(context);
			trackActionExecution(context);
			context.updateScheduledFacts();
		}
	}

	protected void validateParameters(final Map<String, Object> parameters)
	{
		checkArgument(isNotEmpty(parameters), "Properties passed as a method argument must not be empty");
	}

	/**
	 * template method called inside of {@link #performAction(RuleActionContext, Map)}
	 *
	 * @param context
	 * 		the context
	 * @return true if the action was performed successfully, otherwise false
	 */
	protected boolean performActionInternal(final RuleActionContext context) //NOSONAR
	{
		// default implementation returns true
		return true;
	}

	protected void postProcessAction(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		getActionSupplementStrategies().stream().filter(strategy -> strategy.isActionProperToHandle(actionRao, context))
				.forEach(strategy -> strategy.postProcessAction(actionRao, context));
	}

	protected void trackActionExecution(final RuleActionContext context)
	{
		final String ruleCode = getRuleCode(context);
		if (isUseDeprecatedRRDs())
		{
			final RuleConfigurationRRD config = getRuleConfigurationRRD(ruleCode, context);
			config.setActionExecutionStarted(true);
		}
		else
		{
			final RuleAndRuleGroupExecutionTracker tracker = getRuntimeTracker(context);
			tracker.trackActionExecutionStarted(ruleCode);
		}
	}

	protected void trackConsumedProducts(final RuleActionContext context)
	{
		final List<ProductConsumedRAO> productConsumedRAOS = lookupRAOObjectsByType(ProductConsumedRAO.class, context);
		if (CollectionUtils.isNotEmpty(productConsumedRAOS) && isConsumptionEnabled())
		{
			for (final ProductConsumedRAO productConsumedRAO : productConsumedRAOS)
			{
				final OrderEntryRAO orderEntry = productConsumedRAO.getOrderEntry();
				final int availableQuantityInOrderEntry = getRuleEngineCalculationService()
						.getProductAvailableQuantityInOrderEntry(orderEntry);
				productConsumedRAO.setAvailableQuantity(availableQuantityInOrderEntry);

			}
			context.scheduleForUpdate(productConsumedRAOS.toArray());
		}
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	protected void updateOrderConsumedQuantity(final OrderConsumedRAO orderConsumedRAO, final RuleActionContext context)
	{
		// empty
	}

	/**
	 * Checks that the given {@code ruleContext} is of type {@link KnowledgeHelper} (i.e. drools-specific)
	 *
	 * @param context
	 *           the RuleActionContext instance
	 * @return the {@link KnowledgeHelper}
	 */
	protected KnowledgeHelper checkAndGetRuleContext(final RuleActionContext context)
	{
		final Object delegate = context.getDelegate();
		checkState(delegate instanceof KnowledgeHelper, "context must be of type org.kie.api.runtime.rule.RuleContext.");
		return (KnowledgeHelper) delegate;
	}

	/**
	 * Validates that the required meta-data is available. Validation can be disabled by setting the property
	 * {@code droolsruleengineservices.validate.droolsrule.rulecode} to false.
	 *
	 * @param context
	 * 		instance of {@link RuleActionContext}
	 */
	protected void validateRule(final RuleActionContext context)
	{
		final boolean validateRuleCode = getConfigurationService().getConfiguration().getBoolean(
				"droolsruleengineservices.validate.droolsrule.rulecode", true);
		if (!validateRuleCode)
		{
			LOG.debug(
					"ignoring validation of rule code. Set 'droolsruleengineservices.validate.droolsrule.rulecode' to true to re-enable validation.");
			return;
		}
		final Map<String, Object> ruleMetaData = context.getRuleMetadata();
		checkState(ruleMetaData.get(RULEMETADATA_RULECODE) != null, "rule %s is missing metadata key %s for the rule code.",
				context.getRuleName(), RULEMETADATA_RULECODE);
		final boolean validateRulesModuleName = getConfigurationService().getConfiguration().getBoolean(
				"droolsruleengineservices.validate.droolsrule.moduleName", true);
		if (!validateRulesModuleName)
		{
			LOG.debug(
					"ignoring validation of rules module name. Set 'droolsruleengineservices.validate.droolsrule.modulename' to true to re-enable validation.");
			return;
		}
		checkState(ruleMetaData.get(RULEMETADATA_MODULENAME) != null, "rule %s is missing metadata key %s for the module name.",
				context.getRulesModuleName(), RULEMETADATA_MODULENAME);
	}

	/**
	 * Returns the rule's rule code meta data.
	 *
	 * @param context
	 * 		the context
	 * @return the rule's rule code (or null, if none is found)
	 */
	protected String getRuleCode(final RuleActionContext context)
	{
		return getMetaDataFromRule(context, RULEMETADATA_RULECODE);
	}

	/**
	 * Returns the rule's rule group code meta data.
	 *
	 * @param context
	 *           the context
	 * @return the rule's rule code (or null, if none is found)
	 */
	protected String getRuleGroupCode(final RuleActionContext context)
	{
		return getMetaDataFromRule(context, RULEMETADATA_RULEGROUP_CODE);
	}

	/**
	 * Returns whether the rule's rule group is exclusive.
	 *
	 * @param context
	 *           the context
	 * @return true if the rule's rule group is exclusive, otherwise false
	 */
	protected boolean isRuleGroupExclusive(final RuleActionContext context)
	{
		final String exclusive = getMetaDataFromRule(context, RULEMETADATA_RULEGROUP_EXCLUSIVE);
		return Boolean.parseBoolean(exclusive);
	}

	/**
	 * Returns the rule's meta-data for the given key (or {@code null}). Calls {@code toString()} on the meta-data
	 * object.
	 *
	 * @param context
	 * 		the RuleActionContext instance
	 * @param key
	 * 		the key of the meta-data
	 * @return the string representation of the meta-data (or null)
	 */
	protected String getMetaDataFromRule(final RuleActionContext context, final String key)
	{
		final Object value = context.getRuleMetadata().get(key);
		return isNull(value) ? null : value.toString();
	}

	/**
	 * query method to allow for rule execution by {@link RuleConfigurationRRD}
	 *
	 * @param context
	 * 		instance of {@link RuleActionContext}
	 * @return true if the execution is allowed
	 */
	protected boolean allowedByRuntimeConfiguration(final RuleActionContext context)
	{
		final String ruleCode = getRuleCode(context);
		final RuleConfigurationRRD config = getRuleConfigurationRRD(ruleCode, context);

		final Optional<RuleGroupExecutionRRD> ruleExecutionRRD = lookupRAOByType(RuleGroupExecutionRRD.class, context,
				filterByRuleGroup(config));
		boolean allowedToExecute = true;
		if (ruleExecutionRRD.isPresent())
		{
			allowedToExecute = ruleExecutionRRD.get().allowedToExecute(config);
		}
		return allowedToExecute;
	}

	protected Predicate<RuleGroupExecutionRRD> filterByRuleGroup(final RuleConfigurationRRD config)
	{
		return e -> Objects.equals(e.getCode(), config.getRuleGroupCode());
	}

	protected Map<String, String> getMetaDataFromRule(final RuleActionContext context)
	{
		return context.getRuleMetadata().entrySet().stream().collect(toMap(Map.Entry::getKey, e -> valueOf(e.getValue())));
	}

	public void setRAOMetaData(final RuleActionContext context, final AbstractRuleActionRAO... raos)
	{
		if (nonNull(raos))
		{
			Stream.of(raos).filter(Objects::nonNull).forEach(r -> addMetadataToRao(r, context));
		}
	}

	protected void addMetadataToRao(final AbstractRuleActionRAO rao, final RuleActionContext context)
	{
		rao.setFiredRuleCode(getMetaDataFromRule(context, RuleEngineConstants.RULEMETADATA_RULECODE));
		rao.setModuleName(getMetaDataFromRule(context, RuleEngineConstants.RULEMETADATA_MODULENAME));
		rao.setActionStrategyKey(getBeanName());
		rao.setMetadata(getMetaDataFromRule(context));
	}

	/**
	 * @deprecated since 18.08 no longer used
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected void trackRuleExecution(final RuleActionContext context)
	{
		//  empty
	}

	/**
	 * @deprecated since 18.08 no longer used
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected void trackRuleGroupExecutions(final RuleActionContext context)
	{
		//  empty
	}

	/**
	 * @deprecated since 18.08 no longer used
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected void trackRuleGroupCode(final String ruleGroupCode, final RuleActionContext context,
			final RuleConfigurationRRD config)
	{
		//  empty
	}

	/**
	 * @deprecated since 18.08 no longer used
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected void trackRuleGroupExecution(final RuleGroupExecutionRRD execution, final RuleConfigurationRRD config)
	{
		// empty
	}

	protected RuleAndRuleGroupExecutionTracker getRuntimeTracker(final RuleActionContext context)
	{
		return lookupRAOByType(RuleAndRuleGroupExecutionTracker.class, context).orElse(null);
	}

	/**
	 * retrieves the RuleGroupExecutionRRD for the given ruleGroupCode from the given context.
	 *
	 * @param ruleGroupCode
	 * 		the rule group code
	 * @param context
	 * 		the drools rule context
	 * @return the matching RuleGroupExecutionRRD or null if none can be found
	 */
	protected RuleGroupExecutionRRD getRuleGroupExecutionRRD(final String ruleGroupCode, final RuleActionContext context)
	{
		return lookupRAOByType(RuleGroupExecutionRRD.class, context, getRuleGroupExecutionRRDFilter(ruleGroupCode)).orElse(null);
	}

	protected Predicate<RuleGroupExecutionRRD> getRuleGroupExecutionRRDFilter(final String ruleGroupCode)
	{
		return o -> Objects.equals(ruleGroupCode, o.getCode());
	}

	@SuppressWarnings("unchecked")
	protected <T> Optional<T> lookupRAOByType(final Class<T> raoType, final RuleActionContext context,
			final Predicate<T>... raoFilters)
	{
		final List<T> raoFacts = lookupRAOObjectsByType(raoType, context, raoFilters);

		if (raoFacts.size() == 1)
		{
			return Optional.ofNullable(raoFacts.iterator().next());
		}
		else if (CollectionUtils.isEmpty(raoFacts))
		{
			if (!skipErrorLogging(raoType))
			{
				LOG.error("No RAO facts of type {} are found in the Knowledgebase working memory", raoType.getName());
			}
		}
		else
		{
			LOG.error("Multiple instances of RAO facts of type {} are found in the Knowledgebase working memory", raoType.getName());
		}
		return Optional.empty();
	}

	protected <T> boolean skipErrorLogging(final Class<T> raoType)
	{
		return !isConsumptionEnabled() && raoType.isAssignableFrom(ProductConsumedRAO.class);
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> lookupRAOObjectsByType(final Class<T> raoType, final RuleActionContext context,
			final Predicate<T>... raoFilters)
	{
		final KnowledgeHelper helper = checkAndGetRuleContext(context);
		final Predicate<T> composedRaoFilter = isNotEmpty(raoFilters) ? Stream.of(raoFilters).reduce(o -> true, Predicate::and)
				: o -> true;
		final Collection<FactHandle> factHandles = helper.getWorkingMemory().getFactHandles(
				o -> raoType.isInstance(o) && composedRaoFilter.test((T) o));
		if (CollectionUtils.isNotEmpty(factHandles))
		{
			return factHandles.stream().map(h -> (T) ((InternalFactHandle) h).getObject()).collect(toList());
		}
		return Collections.emptyList();
	}

	/**
	 * retrieves the RuleConfigurationRRD for the given ruleCode from the given context.
	 *
	 * @param ruleCode
	 * 		the rule code
	 * @param context
	 * 		the drools rule context
	 * @return the RuleConfigurationRRD or null if none (or more than one) are found
	 */
	protected RuleConfigurationRRD getRuleConfigurationRRD(final String ruleCode, final RuleActionContext context)
	{
		return lookupRAOByType(RuleConfigurationRRD.class, context, getRuleConfigurationRRDFilter(ruleCode)).orElse(null);
	}

	protected Predicate<RuleConfigurationRRD> getRuleConfigurationRRDFilter(final String ruleCode)
	{
		return o -> ruleCode.equals(o.getRuleCode());
	}

	/**
	 * @deprecated since 6.6. Non type-safe implementation
	 */
	@Deprecated
	protected <T> Optional<T> extractFact(final RuleActionContext context, final Predicate<Object> objectFilter)
	{

		final KnowledgeHelper helper = checkAndGetRuleContext(context);
		final Collection factHandles = helper.getWorkingMemory().getFactHandles(objectFilter::test);

		if (nonNull(factHandles) && factHandles.size() == 1)
		{
			final T fact = (T) ((InternalFactHandle) factHandles.iterator().next()).getObject();
			return Optional.of(fact);
		}
		if (LOG.isErrorEnabled())
		{
			final StringBuilder builder = new StringBuilder("Cannot apply rule with rule code: ").append(getRuleCode(context));
			final int size = nonNull(factHandles) ? factHandles.size() : 0;
			if (size > 1)
			{
				builder.append("not unique, found ").append(size).append("Ignoring this rule's action.");
			}
			else
			{
				builder.append("not present in working memory. Ignoring this rule's action.");
			}
			LOG.error(builder.toString());
		}
		return Optional.empty();
	}

	/**
	 * @param orderEntryRAO
	 * 		the order entry to consume
	 * @param actionRAO
	 * 		the action rao to add the consumed entry to
	 * @param quantity
	 * 		the quantity to consume
	 * @param discountValue
	 * 		the discount this order entry consumption gave
	 * @return the newly created consumed entry
	 */
	protected OrderEntryConsumedRAO consumeOrderEntry(final OrderEntryRAO orderEntryRAO, final int quantity,
			final BigDecimal discountValue, final AbstractRuleActionRAO actionRAO)
	{
		final OrderEntryConsumedRAO orderEntryConsumedRAO = createOrderEntryConsumedRAO(orderEntryRAO, quantity, discountValue);
		updateActionRAOWithConsumed(actionRAO, orderEntryConsumedRAO);
		return orderEntryConsumedRAO;
	}

	/**
	 * Creates a new {@code OrderEntryConusmedRAO} that consumes the given orderEntryRAO and adds the consumed entry to
	 * the given actionRAO
	 *
	 * @param orderEntryRAO
	 * 		the order entry to consume
	 * @param actionRAO
	 * 		the action rao to add the consumed entry to
	 * @return the newly created consumed entry
	 */
	protected OrderEntryConsumedRAO consumeOrderEntry(final OrderEntryRAO orderEntryRAO, final AbstractRuleActionRAO actionRAO)
	{
		return consumeOrderEntry(orderEntryRAO, orderEntryRAO.getQuantity(), adjustUnitPrice(orderEntryRAO), actionRAO);
	}

	/**
	 * Creates a new {@code OrderEntryConusmedRAO} that consumes the given orderEntryRAO with the given quantity
	 * and adds the consumed entry to the given actionRAO
	 *
	 * @param orderEntryRAO
	 * 		the order entry to consume
	 * @param quantity
	 * 		the quantity to consume
	 * @param actionRAO
	 * 		the action rao to add the consumed entry to
	 * @return the newly created consumed entry
	 */
	protected OrderEntryConsumedRAO consumeOrderEntry(final OrderEntryRAO orderEntryRAO, final int quantity, final AbstractRuleActionRAO actionRAO)
	{
		return consumeOrderEntry(orderEntryRAO, quantity, adjustUnitPrice(orderEntryRAO, quantity), actionRAO);
	}

	/**
	 * Creates new {@code OrderEntryConusmedRAO}s for the given strategies, links them to the given {@code actionRAO} and
	 * reduces available quantity of order entry by given quantity for every Order Entry from the set and using quantity
	 * from {@code discountedOrderEntryMap}.
	 *
	 * @param context
	 * 		instance of {@link RuleActionContext}
	 * @param strategies
	 * 		the selection strategies
	 * @param actionRAO
	 * 		the {@link AbstractRuleActionRAO} to add the newly created consumed entries to
	 */

	protected <T extends AbstractRuleActionRAO> void consumeOrderEntries(final RuleActionContext context,
			final Collection<EntriesSelectionStrategyRPD> strategies, final T actionRAO)
	{
		final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, strategies);
		final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(strategies, selectedOrderEntryMap);
		consumeOrderEntries(selectedOrderEntryRaos, selectedOrderEntryMap, actionRAO);
	}


	/**
	 * Creates new {@code OrderEntryConusmedRAO}s with the given quantity, links it to the given {@code actionRAO} and
	 * reduces available quantity of order entry by given quantity for every Order Entry from the set and using quantity
	 * from {@code discountedOrderEntryMap}.
	 *
	 * @param selectedEntries
	 *           Order Entries to be consumed
	 * @param selectedEntriesMap
	 *           Map having orderEntry.entryNumber as keys and Unit Quantity To Be Consumed as values
	 * @param actionRAO
	 *           the {@link AbstractRuleActionRAO} to add the newly created consumed entries to
	 */
	protected Set<OrderEntryConsumedRAO> consumeOrderEntries(final Set<OrderEntryRAO> selectedEntries,
			final Map<Integer, Integer> selectedEntriesMap, final AbstractRuleActionRAO actionRAO)
	{
		final Set<OrderEntryConsumedRAO> result = Sets.newLinkedHashSet();

		for (final OrderEntryRAO selectedEntry : selectedEntries)
		{
			result.add(consumeOrderEntry(selectedEntry, selectedEntriesMap.get(selectedEntry.getEntryNumber()).intValue(),
					BigDecimal.ZERO, actionRAO));
		}

		return result;
	}

	/**
	 * Gets Order Entry identifiers (orderEntry.entryNumber) and Unit Quantity To Be Consumed for the Order Entries.
	 *
	 * @param strategies
	 * 		list of {@link EntriesSelectionStrategyRPD} to pickup units according to the strategies
	 * @return Map having orderEntry.entryNumber as keys and Unit Quantity To Be Consumed as values
	 */
	protected Map<Integer, Integer> getSelectedOrderEntryQuantities(final RuleActionContext context,
			final Collection<EntriesSelectionStrategyRPD> strategies)
	{
		final Map<Integer, Integer> result = Maps.newHashMap();
		for (final EntriesSelectionStrategyRPD strategy : strategies)
		{
			final Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> strategyBeans = strategy.isTargetOfAction()
					? getEntriesSelectionStrategies()
					: getQualifyingEntriesSelectionStrategies();

			if (!strategyBeans.containsKey(strategy.getSelectionStrategy()))
			{
				throw new IllegalStateException(String.format("UnitForBundleSelector Strategy with identifier '%s' not defined",
						strategy.getSelectionStrategy()));
			}
			final List<OrderEntryRAO> orderEntriesForStrategy = strategy.getOrderEntries();
			final Map<Integer, Integer> consumableQtyByOrderEntry = Maps.newHashMap();
			for (final OrderEntryRAO orderEntryRAO : orderEntriesForStrategy)
			{
				final int consumableQuantity = getConsumableQuantity(orderEntryRAO);
				consumableQtyByOrderEntry.put(orderEntryRAO.getEntryNumber(), consumableQuantity);
			}

			final Map<Integer, Integer> consumptionByOrderEntryMap = strategyBeans.get(
					strategy.getSelectionStrategy()).pickup(strategy, consumableQtyByOrderEntry);
			result.putAll(consumptionByOrderEntryMap);
		}
		return result;
	}


	/**
	 * Gets Order Entry set to be discounted as a result of bundle processing.
	 *
	 * @param selectionStrategyRPDs
	 * 		list of {@link EntriesSelectionStrategyRPD} to pickup units according to the strategies
	 * @param selectedOrderEntryMap
	 * 		Map having orderEntry.entryNumber as keys and Unit Quantity To Be Consumed as values
	 */
	protected Set<OrderEntryRAO> getSelectedOrderEntryRaos(final Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs,
			final Map<Integer, Integer> selectedOrderEntryMap)
	{

		Set<OrderEntryRAO> orderEntryRAOS = selectionStrategyRPDs.stream()
				.flatMap(selectionStrategy -> selectionStrategy.getOrderEntries().stream())
				.filter(orderEntry -> selectedOrderEntryMap.containsKey(orderEntry.getEntryNumber())).collect(Collectors.toSet());
		orderEntryRAOS = orderEntryRAOS.stream().filter(e -> getConsumableQuantity(e) > 0).collect(Collectors.toSet());
		return orderEntryRAOS;
	}

	/**
	 * Gets Order Entry set to be discounted as a result of bundle processing.
	 *
	 * @deprecated since 6.7
	 *
	 * @param selectionStrategyRPDs
	 * 		list of {@link EntriesSelectionStrategyRPD} to pickup units according to the strategies
	 * @param selectedOrderEntryMap
	 * 		Map having orderEntry.entryNumber as keys and Unit Quantity To Be Consumed as values
	 */
	@Deprecated
	protected Set<OrderEntryRAO> getSelectedOrderEntryRaosStackable(
			final Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs, final Map<Integer, Integer> selectedOrderEntryMap)
	{

		final Set<OrderEntryRAO> orderEntryRAOS = selectionStrategyRPDs.stream()
				.flatMap(selectionStrategy -> selectionStrategy.getOrderEntries().stream())
				.filter(orderEntry -> selectedOrderEntryMap.containsKey(orderEntry.getEntryNumber())).collect(Collectors.toSet());
		return orderEntryRAOS;
	}

	/**
	 * Creates new OrderEntryConsumedRAO for given OrderEntryRAO and with given quantity.
	 *
	 * @param orderEntryRAO
	 * 		OrderEntryRAO to create consumed entry for
	 * @param quantity
	 * 		Consumed quantity
	 * @param discountValue
	 * 		the discount this order entry consumption gave
	 * @return created OrderEntryConsumedRAO
	 */
	protected OrderEntryConsumedRAO createOrderEntryConsumedRAO(final OrderEntryRAO orderEntryRAO, final int quantity,
			final BigDecimal discountValue)
	{
		final OrderEntryConsumedRAO orderEntryConsumed = new OrderEntryConsumedRAO();
		orderEntryConsumed.setOrderEntry(orderEntryRAO);
		orderEntryConsumed.setQuantity(quantity);

		final BigDecimal unitPrice = orderEntryRAO.getPrice();
		final BigDecimal adjustedUnitPrice = unitPrice.subtract(discountValue);
		final BigDecimal roundedAdjustedUnitPrice = getCurrencyUtils().applyRounding(adjustedUnitPrice,
				orderEntryRAO.getCurrencyIsoCode());

		orderEntryConsumed.setAdjustedUnitPrice(roundedAdjustedUnitPrice);

		return orderEntryConsumed;
	}

	/**
	 * Adds given OrderEntryConsumedRAO to the AbstractRuleActionRAO.consumedEntries.
	 *
	 * @param actionRAO
	 * 		AbstractRuleActionRAO to be updated
	 * @param orderEntryConsumedRAO
	 * 		to be added to the AbstractRuleActionRAO
	 */
	protected void updateActionRAOWithConsumed(final AbstractRuleActionRAO actionRAO,
			final OrderEntryConsumedRAO orderEntryConsumedRAO)
	{
		if (actionRAO != null)
		{
			Set<OrderEntryConsumedRAO> consumedEntries = actionRAO.getConsumedEntries();
			if (isNull(consumedEntries))
			{
				consumedEntries = Sets.newLinkedHashSet();
				actionRAO.setConsumedEntries(consumedEntries);
			}
			final Integer entryNumber = orderEntryConsumedRAO.getOrderEntry().getEntryNumber();
			final String firedRuleCode = actionRAO.getFiredRuleCode();
			final Optional<OrderEntryConsumedRAO> existingOrderEntryConsumedRAO = consumedEntries.stream().filter(
					e -> e.getOrderEntry().getEntryNumber().equals(entryNumber) && e.getFiredRuleCode().equals(firedRuleCode)).findFirst();
			orderEntryConsumedRAO.setFiredRuleCode(firedRuleCode);
			if (existingOrderEntryConsumedRAO.isPresent())
			{
				mergeOrderEntryConsumed(existingOrderEntryConsumedRAO.get(), orderEntryConsumedRAO);
			}
			else
			{
				consumedEntries.add(orderEntryConsumedRAO);
			}
		}
	}

	/**
	 * Method merges the properties of two instances of {@link OrderEntryConsumedRAO} if the objects refer to the same order entry
	 * number and same rule
	 *
	 * @param consumedTarget
	 * 		where to merge the data to
	 * @param consumedSource
	 * 		where to merge the data from
	 */
	protected void mergeOrderEntryConsumed(final OrderEntryConsumedRAO consumedTarget, final OrderEntryConsumedRAO consumedSource)
	{
		consumedTarget.setQuantity(consumedTarget.getQuantity() + consumedSource.getQuantity());
		consumedSource.setQuantity(consumedTarget.getQuantity());
	}

	protected BigDecimal adjustUnitPrice(final OrderEntryRAO orderEntryRao)
	{
		return adjustUnitPrice(orderEntryRao, orderEntryRao.getQuantity());
	}

	protected BigDecimal adjustUnitPrice(final OrderEntryRAO orderEntryRao, final int quantity)
	{
		return getRuleEngineCalculationService().getAdjustedUnitPrice(quantity, orderEntryRao);
	}

	/**
	 * Checks if all {@code entriesSelectionStrategyRPDs} have enough quantity.
	 *
	 * @param selectionStrategyRPDs
	 *           List of EntriesSelectionStrategyRPD to check
	 * @return true if all entries selection strategies have enough quantity. false if at least one doesn't have.
	 */
	protected boolean hasEnoughQuantity(final RuleActionContext context,
			final Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs)
	{
		final Map<Integer, Integer> entriesToBeConsumedMap = getEligibleEntryQuantities(selectionStrategyRPDs);
		return selectionStrategyRPDs.stream().flatMap(s -> s.getOrderEntries().stream()).noneMatch(e -> {
			final int consumableQuantity = getConsumableQuantity(e);
			return entriesToBeConsumedMap.get(e.getEntryNumber()) > consumableQuantity;
		});
	}

	protected Map<Integer, Integer> getEligibleEntryQuantities(final Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs)
	{
		final Map<Integer, Integer> entriesToBeConsumedMap = Maps.newHashMap();
		for (final EntriesSelectionStrategyRPD strategy : selectionStrategyRPDs)
		{
			putEligibleEntryQuantities(entriesToBeConsumedMap, strategy);
		}
		return entriesToBeConsumedMap;
	}

	protected void putEligibleEntryQuantities(final Map<Integer, Integer> entriesToBeConsumedMap,
											final EntriesSelectionStrategyRPD strategy)
	{
		int quantityToBeConsumed = strategy.getQuantity();
		final List<OrderEntryRAO> orderEntries = strategy.getOrderEntries();
		int orderEntryCnt = 0;
		for (final OrderEntryRAO orderEntry : orderEntries)
		{
			orderEntryCnt++;
			final Integer entryNumber = orderEntry.getEntryNumber();
			Integer entryConsumedQty = entriesToBeConsumedMap.get(entryNumber);
			if (Objects.isNull(entryConsumedQty))
			{
				entryConsumedQty = 0;
			}
			int eligibleEntryQuantity;
			if (orderEntryCnt < orderEntries.size())
			{
				final int orderEntryConsumableQuantity = getConsumableQuantity(orderEntry);
				final int availableOrderEntryQuantity = orderEntryConsumableQuantity - entryConsumedQty;
				if (availableOrderEntryQuantity <= quantityToBeConsumed)
				{
					eligibleEntryQuantity = orderEntryConsumableQuantity;
					quantityToBeConsumed -= availableOrderEntryQuantity;
				}
				else
				{
					eligibleEntryQuantity = entryConsumedQty + quantityToBeConsumed;
					quantityToBeConsumed -= eligibleEntryQuantity;
				}
			}
			else
			{
				eligibleEntryQuantity = entryConsumedQty + quantityToBeConsumed;
			}
			entriesToBeConsumedMap.put(entryNumber, eligibleEntryQuantity);
		}
	}

	/**
	 * Calculate the order entry consumable quantity
	 *
	 * @param orderEntryRao
	 * 		instance of {@link OrderEntryRAO}
	 * @return number of units available for consumption
	 */
	protected int getConsumableQuantity(final OrderEntryRAO orderEntryRao)
	{
		return orderEntryRao.getQuantity() - getRuleEngineCalculationService().getConsumedQuantityForOrderEntry(orderEntryRao);
	}

	/**
	 * Calculate the order entry consumable quantity in case of stackable rule.
	 *
	 * @deprecated since 6.7
	 *
	 * @param orderEntryRao
	 * 		instance of {@link OrderEntryRAO}
	 * @return number of units available for consumption
	 */
	@Deprecated
	protected int getConsumableQuantityStackable(final OrderEntryRAO orderEntryRao)
	{
		return orderEntryRao.getQuantity();
	}

	/**
	 * Adjusts the quantity fields of EntriesSelectionStrategyRPD such that the maximum possible number of entries will
	 * be consumed by the action. This is done by finding the highest integer multiple that the strategy.quantity can be
	 * multiplied by that there is sufficient quantity in the associated OrderEntryRAO object. This figure is then
	 * multiplied by the original quantity of the selection strategy.
	 *
	 * @param selectionStrategyRPDs
	 *           list of EntriesSelectionStrategyRPD to be updated
	 * @param maxCount
	 *           when maxCount is greater than 0, this parameter acts as an absolute maximum for the multiple.
	 * @return The multiple count used to scale the quantity fields of the strategy objects
	 */
	protected int adjustStrategyQuantity(final Collection<EntriesSelectionStrategyRPD> selectionStrategyRPDs, // NOSONAR
			final int maxCount)
	{
		int count = 0;
		final Map<Integer, Integer> entriesToBeConsumedMap = getEligibleEntryQuantities(selectionStrategyRPDs);
		for (final EntriesSelectionStrategyRPD strategy : selectionStrategyRPDs)
		{

			final List<OrderEntryRAO> orderEntries = strategy.getOrderEntries();
			final int countForAllEntries = orderEntries.stream().mapToInt(OrderEntryRAO::getQuantity).sum();
			final int entryQtyNeeded = orderEntries.stream().mapToInt(e -> entriesToBeConsumedMap.get(e.getEntryNumber())).sum();
			final int tempCount = countForAllEntries / entryQtyNeeded;

			if (count == 0)
			{
				count = tempCount;
			}
			else
			{
				count = Math.min(tempCount, count);
			}
		}
		if (maxCount > 0)
		{
			count = Math.min(count, maxCount);
		}

		final int finalCnt = count;
		selectionStrategyRPDs.forEach(s -> s.setQuantity(s.getQuantity() * finalCnt));

		return count;
	}

	/**
	 * Validates {@code entriesSelectionStrategyRPDs} checking if the collection is not empty, if orderEntries list of
	 * each entry of entriesSelectionStrategyRPDs is not empty and all the orderEntries are from the same Order.
	 *
	 * @param strategies
	 * 		collection of {@link EntriesSelectionStrategyRPD} to check.
	 * @param context
	 * 		the RuleActionContext instance
	 */
	protected void validateSelectionStrategy(final Collection<EntriesSelectionStrategyRPD> strategies,
			final RuleActionContext context)
	{
		final KnowledgeHelper helper = checkAndGetRuleContext(context);
		final String ruleName = helper.getRule().getName();
		checkState(CollectionUtils.isNotEmpty(strategies), "rule %s has empty list of entriesSelectionStrategyRPDs.", ruleName);
		AbstractOrderRAO orderRao = null;
		for (final EntriesSelectionStrategyRPD strategy : strategies)
		{
			checkState(isNotEmpty(strategy.getOrderEntries()),
					"rule %s has empty order entry list in entriesSelectionStrategyRPDs.", ruleName);
			if (orderRao == null)
			{
				orderRao = strategy.getOrderEntries().get(0).getOrder();
			}
			for (final OrderEntryRAO orderEntryRao : strategy.getOrderEntries())
			{
				checkState(orderEntryRao.getOrder() != null && orderEntryRao.getOrder().equals(orderRao),
						"rule %s has inconsistent OrderRao in different OrderEntryRao-s of entriesSelectionStrategyRPDs.", ruleName);
			}
		}
	}

	protected void splitEntriesSelectionStrategies(final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs,
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction,
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering)
	{
		entriesSelectionStrategyRPDs.stream().filter(EntriesSelectionStrategyRPD::isTargetOfAction)
				.forEach(selectionStrategyRPDsForAction::add);
		entriesSelectionStrategyRPDs.stream().filter(s -> !s.isTargetOfAction()).forEach(selectionStrategyRPDsForTriggering::add);
	}

	protected boolean mergeDiscounts(final RuleActionContext context, final DiscountRAO discountRao, final OrderEntryRAO entry)
	{
		final Optional<AbstractRuleActionRAO> actionOptional = entry.getActions().stream().filter(a -> a instanceof DiscountRAO)
				.filter(a -> nonNull(a.getFiredRuleCode())).filter(a -> a.getFiredRuleCode().equals(context.getRuleName()))
				.findFirst();
		if (actionOptional.isPresent())
		{
			final DiscountRAO originalDiscount = (DiscountRAO) actionOptional.get();
			originalDiscount.setAppliedToQuantity(originalDiscount.getAppliedToQuantity() + discountRao.getAppliedToQuantity());
		}
		return actionOptional.isPresent();
	}

	protected void validateCurrencyIsoCode(final boolean absolute, final String currencyIsoCode)
	{
		if (absolute)
		{
			validateParameterNotNull(currencyIsoCode, "currencyIsoCode must not be bull");
		}
		else
		{
			if (!isEmpty(currencyIsoCode))
			{
				LOG.error("currencyIsoCode '{}' will be ignored as absolute is set to false.", currencyIsoCode);
			}
		}
	}

	protected Optional<BigDecimal> extractAmountForCurrency(final RuleActionContext context, final Object currencyAmount)
	{
		checkArgument(nonNull(currencyAmount),
				"The currency-amount map must not be empty: specify at least one CURRENCY->AMOUNT entry.");
		Optional<BigDecimal> amountForCurrency = empty();
		if (currencyAmount instanceof BigDecimal)
		{
			amountForCurrency = ofNullable((BigDecimal) currencyAmount);
		}
		else if (currencyAmount instanceof Map)
		{
			final Map<String, BigDecimal> currencyAmountMap = (Map<String, BigDecimal>) currencyAmount;
			final CartRAO cartRao = context.getCartRao();
			amountForCurrency = nonNull(cartRao) ? ofNullable(currencyAmountMap.get(cartRao.getCurrencyIsoCode()))
					: of(currencyAmountMap.entrySet().iterator().next().getValue());
		}
		return amountForCurrency;
	}

	public String getBeanName()
	{
		return beanName;
	}

	@Override
	public void setBeanName(final String beanName)
	{
		this.beanName = beanName;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setRuleEngineCalculationService(final RuleEngineCalculationService ruleEngineCalculationService)
	{
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

	protected RuleEngineCalculationService getRuleEngineCalculationService()
	{
		return ruleEngineCalculationService;
	}

	protected Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> getEntriesSelectionStrategies()
	{
		return entriesSelectionStrategies;
	}

	@Required
	public void setEntriesSelectionStrategies(
			final Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> entriesSelectionStrategies)
	{
		this.entriesSelectionStrategies = entriesSelectionStrategies;
	}

	protected Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> getQualifyingEntriesSelectionStrategies()
	{
		return qualifyingEntriesSelectionStrategies;
	}

	@Required
	public void setQualifyingEntriesSelectionStrategies(
			final Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> qualifyingEntriesSelectionStrategies)
	{
		this.qualifyingEntriesSelectionStrategies = qualifyingEntriesSelectionStrategies;
	}

	protected CurrencyUtils getCurrencyUtils()
	{
		return currencyUtils;
	}

	@Required
	public void setCurrencyUtils(final CurrencyUtils currencyUtils)
	{
		this.currencyUtils = currencyUtils;
	}

	protected List<ActionSupplementStrategy> getActionSupplementStrategies()
	{
		return actionSupplementStrategies;
	}

	@Required
	public void setActionSupplementStrategies(final List<ActionSupplementStrategy> actionSupplementStrategies)
	{
		this.actionSupplementStrategies = actionSupplementStrategies;
	}

	protected RaoUtils getRaoUtils()
	{
		return raoUtils;
	}

	@Required
	public void setRaoUtils(final RaoUtils raoUtils)
	{
		this.raoUtils = raoUtils;
	}

	protected boolean isConsumptionEnabled()
	{
		return consumptionEnabled;
	}

	@Required
	public void setConsumptionEnabled(final boolean consumptionEnabled)
	{
		this.consumptionEnabled = consumptionEnabled;
	}


	/**
	 * @deprecated since 18.11 flag is present only to enable deprecated RRD usage (backwards compatibility)
	 */
	@Deprecated
	public void setUseDeprecatedRRDs(final boolean useDeprecatedRRDs)
	{
		this.useDeprecatedRRDs = useDeprecatedRRDs;
	}

	/**
	 * @deprecated since 18.11 flag is present only to enable deprecated RRD usage (backwards compatibility)
	 */
	@Deprecated
	protected boolean isUseDeprecatedRRDs()
	{
		return useDeprecatedRRDs;
	}
}
