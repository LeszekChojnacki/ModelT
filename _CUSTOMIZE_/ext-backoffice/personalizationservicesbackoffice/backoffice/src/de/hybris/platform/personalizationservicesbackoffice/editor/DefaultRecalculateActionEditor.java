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
package de.hybris.platform.personalizationservicesbackoffice.editor;

import com.hybris.cockpitng.editor.defaultmultireferenceeditor.DefaultMultiReferenceEditor;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class DefaultRecalculateActionEditor extends DefaultMultiReferenceEditor<String>
{
    private static final Pattern REGEX_EDITOR_PATTERN = Pattern.compile("^(Collection|List|Set)\\((java.lang.String)\\)$");

    @Override
    protected void initializeSelectedItemsCollection(final String collectionType)
    {
        super.initializeSelectedItemsCollection(StringUtils.upperCase(collectionType));
    }

    @Override
    protected Pattern getRegexEditorPattern()
    {
        return REGEX_EDITOR_PATTERN;
    }

    @Override
    public boolean allowNestedObjectCreation()
    {
        return false;
    }
}
