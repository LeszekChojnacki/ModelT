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
package de.hybris.platform.personalizationservicesbackoffice.editor.facade;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import de.hybris.platform.personalizationservicesbackoffice.editor.DefaultRecalculateActionEditor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;
import de.hybris.platform.personalizationservices.RecalculateAction;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 *  Default implementation of {@link ReferenceEditorSearchFacade} used by {@link DefaultRecalculateActionEditor}.
 */
public class DefaultRecalculateActionEditorSearchFacade implements ReferenceEditorSearchFacade<String>
{
    @Override
    public Pageable<String> search(SearchQueryData searchQueryData)
    {
        final List<String> recalculateActionNames = getFilteredEnumValues(searchQueryData.getSearchQueryText());
        return new PageableList<>(recalculateActionNames, searchQueryData.getPageSize());
    }

    protected List<String> getFilteredEnumValues(final String textQuery)
    {
        final List<String> collect = Stream.of(RecalculateAction.values())
                .map(RecalculateAction::name)
                .collect(Collectors.toList());
        return filterEnumValues(collect, textQuery);
    }

    protected List<String> filterEnumValues(final List<String> values, final String textQuery)
    {
        final List<String> result = Lists.newArrayList(values);
        return result.stream().filter( v -> {
            final String txtQuery = (textQuery == null ? StringUtils.EMPTY : textQuery);
            return v.toLowerCase(Locale.ROOT).contains(txtQuery.toLowerCase(Locale.ROOT));
        }).collect(Collectors.toList());
    }
}
