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
package com.hybris.backoffice.cockpitng.dataaccess.facades.search;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.SearchQueryConditionList;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;


/**
 * Search provider which returns the subtypes of given class.
 *
 * <pre>
 * To make it work the referenceSearchCondition with {@value #CODE} has to be used, e.g.:
 * {@code
 *
 * 	<wz:editor-parameter>
 * 	    <wz:name>referenceSearchCondition_code</wz:name>
 * 	    <wz:value>User</wz:value>
 *	</wz:editor-parameter>
 *
 * }
 * It returns empty Pageable and warning when code is not passed.
 * </pre>
 */
public class SubtypesSearchFacade implements ReferenceEditorSearchFacade<ComposedTypeModel>
{

	protected static final String CODE = "code";
	private static final Logger LOG = LoggerFactory.getLogger(SubtypesSearchFacade.class);
	private TypeService typeService;

	@Override
	public Pageable<ComposedTypeModel> search(final SearchQueryData searchQueryData)
	{
		final Optional<String> codeCondition = searchQueryData.getConditions().stream()//
				.filter(SearchQueryConditionList.class::isInstance)//
				.map(SearchQueryConditionList.class::cast)//
				.map(SearchQueryConditionList::getConditions)//
				.flatMap(List::stream)//
				.filter(this::hasCodeAttribute)//
				.findFirst()//
				.map(SearchQueryCondition::getValue)//
				.filter(String.class::isInstance)//
				.map(String.class::cast);

		if (!codeCondition.isPresent())
		{
			LOG.warn("Typecode for given facade was not found, returning an empty Pageable");
			return new PageableList<>(Lists.newArrayList(), searchQueryData.getPageSize());
		}


		return new PageableList<>(
				filterTypesByNames(new ArrayList<>(typeService.getComposedTypeForCode(codeCondition.get()).getAllSubTypes()),
						searchQueryData.getSearchQueryText()),
				searchQueryData.getPageSize());
	}

	protected List<ComposedTypeModel> filterTypesByNames(final List<ComposedTypeModel> composedTypes, final String searchText)
	{
		if (StringUtils.isNotEmpty(searchText))
		{
			return composedTypes.stream().filter(type -> StringUtils.containsIgnoreCase(type.getName(), searchText))
					.collect(Collectors.toList());
		}

		return composedTypes;
	}

	protected boolean hasCodeAttribute(final SearchQueryCondition condition)
	{
		return StringUtils.equals(condition.getDescriptor().getAttributeName(), CODE);
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
