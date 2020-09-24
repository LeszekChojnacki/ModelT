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
package com.hybris.backoffice.solrsearch.dataaccess;/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;

public class SolrSearchStrategyTest {


    private SolrSearchStrategy solrSearchStrategy;
    @Mock
    private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;

    final String testTypeCode = "typeCode";
    final String testFieldName = "testFieldName";
    final String testType = "java.lang.String";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        solrSearchStrategy = new SolrSearchStrategy();
        solrSearchStrategy.setBackofficeFacetSearchConfigService(backofficeFacetSearchConfigService);
        Map<String, String> typeMappings = new HashMap<>();

        typeMappings.put("text", "java.lang.String");
        typeMappings.put("sortabletext", "java.lang.String");
        typeMappings.put("string", "java.lang.String");

        solrSearchStrategy.setTypeMappings(typeMappings);
    }

    @Test
    public void checkGetFieldType() {

        FacetSearchConfig facetSearchConfig = createFacetSearchConfigData();

        try {
            Mockito.when(backofficeFacetSearchConfigService.getFacetSearchConfig(testTypeCode)).thenReturn(facetSearchConfig);
        } catch (FacetConfigServiceException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assertions.assertThat(solrSearchStrategy.getFieldType(testTypeCode, testFieldName)).isEqualTo(testType);
        Assertions.assertThat(solrSearchStrategy.isLocalized(testTypeCode, testFieldName)).isEqualTo(true);
    }

    private FacetSearchConfig createFacetSearchConfigData() {

        final FacetSearchConfig facetConfig = new FacetSearchConfig();

        final IndexConfig indexConfig = mock(IndexConfig.class);
        IndexedType indexedType = Mockito.mock(IndexedType.class);
        ComposedTypeModel composedTypeModel = mock(ComposedTypeModel.class);

        when(indexedType.getComposedType()).thenReturn(composedTypeModel);
        when(indexedType.getComposedType().getCode()).thenReturn(testTypeCode);

        final Map<String, IndexedProperty> indexedPropertyMap = mock(Map.class);
        when(indexedPropertyMap.get(anyString())).thenAnswer(invocationOnMock -> {
            final String name = (String) invocationOnMock.getArguments()[0];
            final IndexedProperty indexedProperty = new IndexedProperty();
            indexedProperty.setName(name);
            indexedProperty.setBackofficeDisplayName(name);
            indexedProperty.setLocalized(true);
            indexedProperty.setType("string"); //key in the map "typeMappings"
            return indexedProperty;
        });
        when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);

        Map<String, IndexedType> indexedTypeMap = new HashMap<>();
        indexedTypeMap.put(testTypeCode, indexedType);
        when(indexConfig.getIndexedTypes()).thenReturn(indexedTypeMap);

        facetConfig.setName("facetConfigName");
        facetConfig.setIndexConfig(indexConfig);

        return facetConfig;

    }

}
