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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import de.hybris.platform.regioncache.ConcurrentHashSet;
import de.hybris.platform.ruleengineservices.rao.providers.ExpandedRAOProvider;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;


/**
 * The abstract class encapsulates logic to support RAO expansion processing options.
 *
 */
public abstract class AbstractExpandedRAOProvider<T, R> implements ExpandedRAOProvider<T>, InitializingBean
{

	protected Collection<String> validOptions;
	protected Collection<String> defaultOptions;
	protected Collection<String> minOptions;

	private List<RAOFactsExtractor> factExtractorList;
	private Map<String, BiConsumer<Set<Object>, R>> consumerMap;

	@Override
	public Set expandFactModel(final T modelFact)
	{
		return expandFactModel(modelFact, getDefaultOptions());
	}

	@Override
	public Set expandFactModel(final T modelFact, final Collection<String> options)
	{
		final Collection<String> filteredOptions = getFilteredOptions(options);
		final R raoFact = createRAO(modelFact);
		final Set expandedFactsSet = expandRAO(raoFact, filteredOptions);
		populateRaoFactsExtractorConsumers();
		addExtraRAOFacts(expandedFactsSet, raoFact, filteredOptions);
		return expandedFactsSet;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		setConsumerMap(new ConcurrentHashMap<>());
	}

	protected void populateRaoFactsExtractorConsumers()
	{
		final List<RAOFactsExtractor> myFactExtractorList = getFactExtractorList();
		if (isNotEmpty(myFactExtractorList))
		{
			final Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = getConsumerMap();
			myFactExtractorList.stream().filter(e -> !myConsumerMap.containsKey(e.getTriggeringOption()))
					.forEach(this::addOptionConsumers);
		}
	}

	protected void addExtraRAOFacts(final Set expandedFactsSet, final R raoFact, final Collection<String> filteredOptions)
	{
		final Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = getConsumerMap();
		if (isNotEmpty(myConsumerMap) && isNotEmpty(filteredOptions))
		{
			filteredOptions.stream().filter(myConsumerMap::containsKey)
					.forEach(o -> myConsumerMap.get(o).accept(expandedFactsSet, raoFact));
		}
	}

	protected abstract R createRAO(T modelFact);

	protected void addOptionConsumers(final RAOFactsExtractor raoFactsExtractor)
	{
		final String triggeringOption = raoFactsExtractor.getTriggeringOption();
		final Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = getConsumerMap();
		myConsumerMap.put(triggeringOption, (f, r) -> f.addAll(raoFactsExtractor.expandFact(r)));
	}

	protected Optional<BiConsumer<Set<Object>, R>> getConsumer(final String option)
	{
		final Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = getConsumerMap();
		if (isEmpty(myConsumerMap) || isNull(myConsumerMap.get(option)))
		{
			return Optional.empty();
		}
		return Optional.of(myConsumerMap.get(option));
	}

	protected Set<Object> expandRAO(final R rao, final Collection<String> options)
	{
		final Set<Object> facts = new LinkedHashSet<>();
		if (nonNull(rao))
		{
			options.stream().map(this::getConsumer).filter(Optional::isPresent).forEach(c -> c.get().accept(facts, rao));
		}
		return facts;
	}

	protected Collection<String> getFilteredOptions(final Collection<String> options)
	{
		final Set<String> onlyValidOptions = new HashSet<>(options);
		final Collection<String> localMinOptions = getMinOptions();
		if (isNotEmpty(localMinOptions))
		{
			onlyValidOptions.addAll(localMinOptions);
		}
		final Collection<String> localValidOptions = getValidOptions();
		if (isNotEmpty(localValidOptions))
		{
			onlyValidOptions.retainAll(localValidOptions);
		}
		return onlyValidOptions;
	}

	protected Set<String> addExtraValidOptions(final List<RAOFactsExtractor> raoExtractorList)
	{
		if (CollectionUtils.isNotEmpty(raoExtractorList))
		{
			return raoExtractorList.stream().filter(e -> isNotEmpty(e.getTriggeringOption()))
					.map(RAOFactsExtractor::getTriggeringOption).collect(toSet());
		}
		return Sets.newHashSet();
	}

	protected Set<String> addExtraDefaultOptions(final List<RAOFactsExtractor> raoExtractorList)
	{
		if (CollectionUtils.isNotEmpty(raoExtractorList))
		{
			return raoExtractorList.stream().filter(RAOFactsExtractor::isDefault).map(RAOFactsExtractor::getTriggeringOption)
					.collect(toSet());
		}
		return Sets.newHashSet();
	}

	protected Set<String> addExtraMinOptions(final List<RAOFactsExtractor> raoExtractorList)
	{
		if (CollectionUtils.isNotEmpty(raoExtractorList))
		{
			return raoExtractorList.stream().filter(RAOFactsExtractor::isMinOption).map(RAOFactsExtractor::getTriggeringOption)
					.collect(toSet());
		}
		return SetUtils.EMPTY_SET;
	}

	protected List<RAOFactsExtractor> getFactExtractorList()
	{
		return factExtractorList;
	}

	@Required
	public void setFactExtractorList(final List<RAOFactsExtractor> factExtractorList)
	{
		this.factExtractorList = factExtractorList;
	}

	protected Collection<String> getDefaultOptions()
	{
		final Collection<String> combinedDefaultOptions = getConcurrentlySafeOptions(defaultOptions);
		combinedDefaultOptions.addAll(addExtraDefaultOptions(getFactExtractorList()));

		return combinedDefaultOptions;
	}

	protected Collection<String> getValidOptions()
	{
		final Collection<String> combinedValidOptions = getConcurrentlySafeOptions(validOptions);
		combinedValidOptions.addAll(addExtraValidOptions(getFactExtractorList()));

		return combinedValidOptions;
	}

	protected Collection<String> getMinOptions()
	{
		final Collection<String> combinedMinOptions = getConcurrentlySafeOptions(minOptions);
		combinedMinOptions.addAll(addExtraMinOptions(getFactExtractorList()));

		return combinedMinOptions;
	}

	protected Collection<String> getConcurrentlySafeOptions(final Collection<String> options)
	{
		return new ConcurrentHashSet<>(options);
	}

	public void setConsumerMap(final Map<String, BiConsumer<Set<Object>, R>> consumerMap)
	{
		this.consumerMap = consumerMap;
	}

	protected Map<String, BiConsumer<Set<Object>, R>> getConsumerMap()
	{
		return consumerMap;
	}

}
