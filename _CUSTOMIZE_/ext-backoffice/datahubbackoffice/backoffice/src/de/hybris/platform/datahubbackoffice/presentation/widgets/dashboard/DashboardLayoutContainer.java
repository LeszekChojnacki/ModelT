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
package de.hybris.platform.datahubbackoffice.presentation.widgets.dashboard;

import static de.hybris.platform.datahubbackoffice.dataaccess.pool.PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE;

import de.hybris.platform.datahubbackoffice.datahub.dashboard.DashboardRowRenderer;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Timer;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.pool.PoolData;

public class DashboardLayoutContainer extends DefaultWidgetController
{
	protected static final String SOCKET_IN_ACTIVE = "active";
	private static final String WIDGET_SETTING_DASHBOARD_RENDERERS = "renderers_%s";
	protected static final String TABBOX_WIDGET_ID = "tabbox";
	private static final String REFERENCE_TYPE = "Reference(%s)";

	private static final String MODEL_VALUE_POOL = "currentPool";

	@Wire
	private Tabbox tabbox;
	@Wire
	private Div statusCnt;
	@Wire
	private Label statusLabel;
	@Wire
	private Timer timer;


	@SocketEvent(socketId = SOCKET_IN_ACTIVE)
	public void activate(final Boolean enabled)
	{
		final boolean activated = BooleanUtils.toBoolean(enabled) && isDashboardTabSelected();
		timer.setRunning(activated);
		if (activated)
		{
			renderInternal();
		}
	}

	@ViewEvent(componentID = TABBOX_WIDGET_ID, eventName = Events.ON_SELECT)
	public void dashboardTabSelected()
	{
		renderInternal();
	}

	protected void renderInternal()
	{
		resetTimerForSelectedTab();
		refreshSelectedTabPanel();
	}

	protected void resetTimerForSelectedTab()
	{
		timer.getEventListeners(Events.ON_TIMER)
				.forEach(eventListener -> timer.removeEventListener(Events.ON_TIMER, eventListener));
		timer.addEventListener(Events.ON_TIMER, event -> refreshSelectedTabPanel());
	}

	protected void refreshSelectedTabPanel()
	{
		clearSelectedPanel();
		renderCurrentRows();
	}

	protected void clearSelectedPanel()
	{
		if (isDashboardTabSelected() && CollectionUtils.isNotEmpty(tabbox.getSelectedPanel().getChildren()))
		{
			tabbox.getSelectedPanel().getChildren().clear();
		}
	}

	protected void renderCurrentRows()
	{
		getDashboardRowRenderers(getSelectedTabId())
				.forEach(each -> each.renderDashboardRow(tabbox.getSelectedPanel(), getWidgetInstanceManager()));
	}

	protected List<DashboardRowRenderer> getDashboardRowRenderers(final String id)
	{
		final String configuredRenderers = getWidgetSettings().getString(String.format(WIDGET_SETTING_DASHBOARD_RENDERERS, id));
		final List<DashboardRowRenderer> ret = Lists.newArrayList();
		if (StringUtils.isNotBlank(configuredRenderers))
		{
			for (final String renderer : configuredRenderers.split(","))
			{
				ret.add(BackofficeSpringUtil.getBean(renderer));
			}
		}
		return ret;
	}

	@Override
	public void initialize(final Component comp)
	{
		updateSelectedPoolStatusLabel(null);
		statusCnt.appendChild(createSelectPoolEditor(DATAHUB_POOL_TYPECODE, MODEL_VALUE_POOL));
	}

	protected String getSelectedTabId()
	{
		return isDashboardTabSelected() ? tabbox.getSelectedTab().getId() : StringUtils.EMPTY;
	}

	protected boolean isDashboardTabSelected()
	{
		return tabbox.getSelectedPanel() != null;
	}

	protected EventListener<Event> createPoolSelectionListener()
	{
		return event -> {
			final PoolData selectedPool = (PoolData) event.getData();
			updateSelectedPoolStatusLabel(selectedPool);
			renderInternal();
		};
	}

	protected void updateSelectedPoolStatusLabel(final PoolData selectedPool)
	{
		final String poolName = selectedPool != null ? selectedPool.getPoolName() : StringUtils.EMPTY;
		statusLabel.setValue(poolName);

	}

	protected Editor createSelectPoolEditor(final String type, final String property)
	{
		final Editor editor = new Editor();
		editor.setProperty(property);
		editor.setEditorLabel(type);
		editor.setType(String.format(REFERENCE_TYPE, type));
		editor.setWidgetInstanceManager(getWidgetInstanceManager());
		editor.setNestedObjectCreationDisabled(true);
		editor.addParameter("referenceAdvancedSearchEnabled", Boolean.FALSE);
		editor.addEventListener(Editor.ON_VALUE_CHANGED, createPoolSelectionListener());
		editor.initialize();
		return editor;
	}
}
