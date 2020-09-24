/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.datahub.dashboard;

import static org.mockito.Mockito.when;

import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.datahub.client.StatusCountClient;
import com.hybris.datahub.dto.count.CanonicalItemStatusCountData;
import com.hybris.datahub.dto.count.CanonicalPublicationStatusCountData;
import com.hybris.datahub.dto.count.RawItemStatusCountData;
import com.hybris.datahub.dto.pool.PoolData;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDashboardRowRendererUnitTest
{
	private static final String MODEL_VALUE_POOL = "currentPool";
	@Mock
	protected StatusCountClient statusCountClient;
	@Mock
	protected CanonicalItemStatusCountData canonicalStatusCountData;
	@Mock
	protected RawItemStatusCountData rawStatusCountData;
	@Mock
	protected CanonicalPublicationStatusCountData canonicalPublicationStatusCountData;
	@Mock
	protected WidgetInstanceManager context;
	@Mock
	private WidgetModel dashboardControllerModel;

	protected final Component parent = new Div();

	@Before
	public void setUp()
	{
		when(context.getModel()).thenReturn(dashboardControllerModel);
	}

	public void whenPoolWithNameSelected(final String poolName)
	{
		when(dashboardControllerModel.getValue(MODEL_VALUE_POOL, PoolData.class)).thenReturn(new PoolData().withName(poolName));
	}

	public void whenNoPoolSelected()
	{
		when(dashboardControllerModel.getValue(MODEL_VALUE_POOL, PoolData.class)).thenReturn(null);
	}
}
