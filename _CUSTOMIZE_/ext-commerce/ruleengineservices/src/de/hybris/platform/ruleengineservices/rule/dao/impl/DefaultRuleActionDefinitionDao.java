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

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionModel;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionRuleTypeMappingModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleActionDefinitionDao;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleActionDefinitionDao extends AbstractItemDao implements RuleActionDefinitionDao
{

	protected static final String GET_ALL_ACTION_DEFINITIONS = "select {" + RuleActionDefinitionModel.PK + "} from {"
			+ RuleActionDefinitionModel._TYPECODE + "}";
	protected static final String GET_ACTION_DEFINITIONS_BY_RULE_TYPE = "select {" + RuleActionDefinitionModel.PK + "} from {"
			+ RuleActionDefinitionModel._TYPECODE + " as rad JOIN " + RuleActionDefinitionRuleTypeMappingModel._TYPECODE
			+ " as radm ON {rad.PK} = {radm.definition}} WHERE {radm." + RuleActionDefinitionRuleTypeMappingModel.RULETYPE
			+ "} in (?ruleTypes)";

	private TypeService typeService;

	@Override
	public List<RuleActionDefinitionModel> findAllRuleActionDefinitions()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_ACTION_DEFINITIONS);
		final SearchResult<RuleActionDefinitionModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<RuleActionDefinitionModel> findRuleActionDefinitionsByRuleType(final Class<?> ruleType)
	{
		final ComposedTypeModel ruleTypeModel = typeService.getComposedTypeForClass(ruleType);
		final List<ComposedTypeModel> ruleTypes = new ArrayList<ComposedTypeModel>();
		ruleTypes.add(ruleTypeModel);
		final Collection<ComposedTypeModel> superTypes = ruleTypeModel.getAllSuperTypes();
		ruleTypes.addAll(superTypes);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ACTION_DEFINITIONS_BY_RULE_TYPE);
		query.addQueryParameter("ruleTypes", ruleTypes);

		final SearchResult<RuleActionDefinitionModel> search = getFlexibleSearchService().search(query);
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
