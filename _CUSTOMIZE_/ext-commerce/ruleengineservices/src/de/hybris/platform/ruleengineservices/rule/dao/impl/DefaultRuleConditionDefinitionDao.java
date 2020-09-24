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
package de.hybris.platform.ruleengineservices.rule.dao.impl;

import static com.google.common.collect.Lists.newArrayList;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;
import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionRuleTypeMappingModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleConditionDefinitionDao;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleConditionDefinitionDao extends AbstractItemDao implements RuleConditionDefinitionDao
{

	protected static final String GET_ALL_CONDITION_DEFINITIONS = "select {" + RuleConditionDefinitionModel.PK + "} from {"
			+ RuleConditionDefinitionModel._TYPECODE + "}";
	protected static final String GET_CONDITION_DEFINITIONS_BY_RULE_TYPE = "select {" + RuleConditionDefinitionModel.PK
			+ "} from {" + RuleConditionDefinitionModel._TYPECODE + " as rcd JOIN "
			+ RuleConditionDefinitionRuleTypeMappingModel._TYPECODE + " as rcdm ON {rcd.PK} = {rcdm.definition}} WHERE {rcdm."
			+ RuleConditionDefinitionRuleTypeMappingModel.RULETYPE + "} in (?ruleTypes)";

	private TypeService typeService;

	@Override
	public List<RuleConditionDefinitionModel> findAllRuleConditionDefinitions()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_CONDITION_DEFINITIONS);
		final SearchResult<RuleConditionDefinitionModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<RuleConditionDefinitionModel> findRuleConditionDefinitionsByRuleType(final Class<?> ruleType)
	{
		final ComposedTypeModel ruleTypeModel = typeService.getComposedTypeForClass(ruleType);
		final List<ComposedTypeModel> ruleTypes = newArrayList();
		ruleTypes.add(ruleTypeModel);
		final Collection<ComposedTypeModel> superTypes = ruleTypeModel.getAllSuperTypes();
		ruleTypes.addAll(superTypes);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_CONDITION_DEFINITIONS_BY_RULE_TYPE);
		query.addQueryParameter("ruleTypes", ruleTypes);

		final SearchResult<RuleConditionDefinitionModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
