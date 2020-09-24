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
package de.hybris.platform.adaptivesearch.setup;

import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ConfigurationUtils;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

@SystemSetup(extension = AdaptivesearchConstants.EXTENSIONNAME)
public class AdaptiveSearchSystemSetup
{

    private FlexibleSearchService flexibleSearchService;
    private ModelService modelService;

    @SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.UPDATE)
    public void updateSortsMergeMode()
    {
        final AsSortsMergeMode defaultSortsMergeMode = ConfigurationUtils.getDefaultSortsMergeMode();

        //@formatter:off
        final String query =
                "SELECT {"+AbstractAsConfigurableSearchConfigurationModel.PK+"} "
                        + " FROM {"+AbstractAsConfigurableSearchConfigurationModel._TYPECODE+"} "
                        + " WHERE {"+AbstractAsConfigurableSearchConfigurationModel.SORTSMERGEMODE+"} IS NULL";
        //@formatter:on

        final SearchResult<AbstractAsConfigurableSearchConfigurationModel> searchResult = flexibleSearchService.search(query);
        final List<AbstractAsConfigurableSearchConfigurationModel> searchConfigurations = searchResult.getResult();

        if(CollectionUtils.isNotEmpty(searchConfigurations))
        {
            searchConfigurations.forEach(searchConfiguration -> searchConfiguration.setSortsMergeMode(defaultSortsMergeMode));
            modelService.saveAll(searchConfigurations);
        }
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Required
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Required
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
