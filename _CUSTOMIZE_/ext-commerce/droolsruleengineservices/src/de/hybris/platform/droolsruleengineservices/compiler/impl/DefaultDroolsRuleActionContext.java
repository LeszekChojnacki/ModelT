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
package de.hybris.platform.droolsruleengineservices.compiler.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.newHashSet;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MODULENAME;
import static de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator.CONTAINER_PATH_SEPARATOR;
import static de.hybris.platform.ruleengineservices.util.RAOConstants.STACKABLE_PARAM;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.drools.core.spi.KnowledgeHelper;


/**
 * Default implementation of {@link RuleActionContext} for the drools rule engine.
 *
 */
public class DefaultDroolsRuleActionContext implements RuleActionContext
{
	private final Map<String, Object> variables;
	private final KnowledgeHelper helper;
	private Map<String, Object> parameters;
	private final Set<Object> factsToUpdate;

	public DefaultDroolsRuleActionContext(final Map<String, Object> variables, final Object helper)
	{
		this.variables = variables;
		this.helper = (KnowledgeHelper) helper;
		factsToUpdate = newHashSet();
	}

	public Map<String, Object> getVariables()
	{
		return variables;
	}

	@Override
	public Object getDelegate()
	{
		return helper;
	}

	@Override
	public String getMetaData(final String key)
	{
		final Object value = helper.getRule().getMetaData().get(key);
		return value == null ? null : value.toString();
	}

	@Override
	public <T> T getValue(final Class<T> type)
	{
		final Set<T> values = findValues(type);

		if (isNotEmpty(values))
		{
			return values.iterator().next();
		}

		return null;
	}

	@Override
	public void scheduleForUpdate(final Object... facts)
	{
		factsToUpdate.addAll(asList(facts));
	}

	@Override
	public <T> Set<T> getValues(final Class<T> type)
	{
		return findValues(type);
	}

	@Override
	public <T> T getValue(final Class<T> type, final String... path)
	{
		final Set<T> values = findValues(type, path);

		if (CollectionUtils.isNotEmpty(values))
		{
			return values.iterator().next();
		}

		return null;
	}

	@Override
	public <T> Set<T> getValues(final Class<T> type, final String... path)
	{
		return findValues(type, path);
	}


	@Override
	public void insertFacts(final Object... facts)
	{
		if (isNotEmpty(facts))
		{
			stream(facts).forEach(helper::insert);
		}
	}

	@Override
	public void updateScheduledFacts()
	{
		if (isNotEmpty(factsToUpdate))
		{
			updateFacts(factsToUpdate.toArray(new Object[0]));
			factsToUpdate.clear();
		}
	}

	@Override
	public void updateFacts(final Object... facts)
	{
		if (isNotEmpty(facts))
		{
			stream(facts).forEach(helper::update);
		}
	}

	@Override
	public void insertFacts(final Collection facts)
	{
		if (isNotEmpty(facts))
		{
			facts.forEach(helper::insert);
		}
	}

	protected <T> Set<T> findValues(final Class<T> type, final String... path)
	{
		String key;

		key = path.length == 0 ? type.getName() : StringUtils.join(path, CONTAINER_PATH_SEPARATOR) + CONTAINER_PATH_SEPARATOR
				+ type.getName();

		final Object value = variables.get(key);

		if (value instanceof Set)
		{
			final Set<T> values = (Set<T>) value;

			return evaluateValues(type, values, path);
		}
		else if (value instanceof List)
		{
			final List<T> values = (List<T>) value;
			return evaluateValues(type, values, path);
		}
		else if (nonNull(value))
		{
			return of((T) value);
		}
		else
		{
			return emptySet();
		}
	}

	protected <T> Set<T> evaluateValues(final Class<T> type, final List<T> values, final String... path)
	{
		if (isNotEmpty(values))
		{
			return new HashSet<>(values);
		}
		else if (ArrayUtils.isEmpty(path))
		{
			return emptySet();
		}
		else
		{
			return findValues(type, Arrays.copyOf(path, path.length - 1));
		}
	}

	protected <T> Set<T> evaluateValues(final Class<T> type, final Set<T> values, final String... path)
	{
		if (isNotEmpty(values))
		{
			return values.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		}
		else if (ArrayUtils.isEmpty(path))
		{
			return emptySet();
		}
		else
		{
			return findValues(type, Arrays.copyOf(path, path.length - 1));
		}
	}



	@Override
	public CartRAO getCartRao()
	{
		return getValue(CartRAO.class);
	}

	@Override
	public RuleEngineResultRAO getRuleEngineResultRao()
	{
		return getValue(RuleEngineResultRAO.class);
	}

	@Override
	public String getRuleName()
	{
		final KnowledgeHelper knowledgeHelper = checkAndGetRuleContext(getDelegate());
		return knowledgeHelper.getRule().getName();
	}

	@Override
	public Optional<String> getRulesModuleName()
	{
		Optional<String> rulesModuleName = Optional.empty();
		final KnowledgeHelper knowledgeHelper = checkAndGetRuleContext(getDelegate());
		final Map<String, Object> metaData = knowledgeHelper.getRule().getMetaData();
		if (MapUtils.isNotEmpty(metaData))
		{
			rulesModuleName = ofNullable((String) metaData.get(RULEMETADATA_MODULENAME));
		}
		return rulesModuleName;
	}

	@Override
	public Map<String, Object> getRuleMetadata()
	{
		final KnowledgeHelper knowledgeHelper = checkAndGetRuleContext(getDelegate());
		return knowledgeHelper.getRule().getMetaData();
	}

	protected KnowledgeHelper checkAndGetRuleContext(final Object ruleContext)
	{
		checkState(ruleContext instanceof KnowledgeHelper, "ruleContext must be of type org.drools.core.spi.KnowledgeHelper.");
		return (KnowledgeHelper) ruleContext;
	}

	@Override
	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	@Override
	public Object getParameter(final String parameterName)
	{
		final Map<String, Object> params = getParameters();
		return params != null ? params.get(parameterName) : null;
	}

	@Override
	public <T> T getParameter(final String parameterName, final Class<T> type)
	{
		final Map<String, Object> params = getParameters();
		checkArgument(
				params != null && params.containsKey(parameterName) && params.get(parameterName).getClass().isAssignableFrom(type),
				String.format("Property '%1$s' must not be null and must be of type %2$s", parameterName, type.getName()));

		return (T) params.get(parameterName); //NOSONAR
	}

	@Override
	public void setParameters(final Map<String, Object> parameters)
	{
		this.parameters = parameters == null ? null : Collections.unmodifiableMap(parameters);
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	@Override
	public boolean isStackable()
	{
		return Boolean.TRUE.equals(getParameter(STACKABLE_PARAM));
	}

	@Override
	public void halt()
	{
		final KnowledgeHelper knowledgeHelper = checkAndGetRuleContext(getDelegate());
		knowledgeHelper.halt();
	}
}
