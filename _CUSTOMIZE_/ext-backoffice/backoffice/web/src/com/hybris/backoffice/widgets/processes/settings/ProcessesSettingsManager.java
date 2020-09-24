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
package com.hybris.backoffice.widgets.processes.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.collections4.CollectionUtils;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


/**
 * Processes settings manager - manages specified setting so they can be modified session not affecting widget's
 * settings.
 */
public class ProcessesSettingsManager
{
	public static final String SETTINGS_PATH_PREFIX = "ProcessesSettingsManager_";
	protected static final String DEFAULT_TIME_RANGE = "1h";
	protected static final String LABEL_STATE_ENABLED = "processes.header.state.enabled";
	protected static final String LABEL_STATE_DISABLED = "processes.header.state.disabled";
	private final WidgetInstanceManager wim;
	private final List<BiConsumer<String, Object>> listeners = new ArrayList<>();
	private TimeRangeFactory timeRangeFactory;

	public ProcessesSettingsManager(final WidgetInstanceManager wim)
	{
		this.wim = wim;
	}

	/**
	 * Returns value of managed setting.
	 *
	 * @param settingName
	 *           setting name
	 * @param tClass
	 *           class of the setting
	 * @param <T>
	 *           expected type of setting value
	 * @return setting value
	 */
	public <T> T getSettingValue(final String settingName, final Class<T> tClass)
	{
		return wim.getModel().getValue(getSettingPath(settingName), tClass);
	}

	/**
	 * Adds lister to managed settings.
	 *
	 * @param listener
	 *           consumer which consumes setting name and update setting value.
	 */
	public void addSettingChangedListener(final BiConsumer<String, Object> listener)
	{
		listeners.add(listener);
	}

	/**
	 * Adds boolean setting to manager.
	 *
	 * @param settingName
	 *           setting name
	 * @param checkbox
	 *           boolean checkbox
	 * @param stateLabel
	 *           label which is placeholder for checkbox state e.g. enabled/disbaled
	 */
	public void addBooleanSetting(final String settingName, final Checkbox checkbox, final Label stateLabel)
	{
		checkbox.addEventListener(Events.ON_CHECK, (CheckEvent event) -> {
			final Boolean updatedValue = Boolean.valueOf(event.isChecked());
			wim.getModel().setValue(getSettingPath(settingName), updatedValue);
			updateBooleanLabel(stateLabel, event.isChecked());
			notifySettingChanged(settingName, updatedValue);
		});
		initBooleanSettingValue(settingName, checkbox, stateLabel);
	}

	protected void initBooleanSettingValue(final String settingName, final Checkbox checkbox, final Label stateLabel)
	{
		final Boolean settingValueInModel = wim.getModel().getValue(getSettingPath(settingName), Boolean.class);
		final boolean settingValue = settingValueInModel != null ? settingValueInModel.booleanValue()
				: wim.getWidgetSettings().getBoolean(settingName);

		checkbox.setChecked(settingValue);
		updateBooleanLabel(stateLabel, settingValue);
		if (settingValueInModel == null)
		{
			wim.getModel().setValue(getSettingPath(settingName), Boolean.valueOf(settingValue));
		}
	}

	protected void updateBooleanLabel(final Label stateLabel, final boolean isChecked)
	{
		final String labelKey = isChecked ? LABEL_STATE_ENABLED : LABEL_STATE_DISABLED;
		stateLabel.setValue(wim.getLabel(labelKey));
	}

	/**
	 * Adds time range setting. It populates list of available time ranges with given widget setting using
	 * {@link TimeRangeFactory}
	 *
	 * @param settingName
	 *           name of a setting with time ranges
	 * @param rangeList
	 *           list where time ranges should be displayed
	 * @param statusLabel
	 *           placeholder which represent chosen range
	 */
	public void addTimeRangeSetting(final String settingName, final Listbox rangeList, final Label statusLabel)
	{
		final ListModelList<TimeRange> model = new ListModelList<>(createTimeRanges(settingName));
		rangeList.setModel(model);
		rangeList.addEventListener(Events.ON_SELECT, (SelectEvent<Listitem, TimeRange> e) -> {
			final Iterator<TimeRange> iterator = e.getSelectedObjects().iterator();
			if (iterator.hasNext())
			{
				final TimeRange range = iterator.next();
				updateChosenRange(settingName, statusLabel, range);
				notifySettingChanged(settingName, range);
			}
		});
		initRangeSettingValue(settingName, statusLabel, model);
	}

	protected void initRangeSettingValue(final String settingName, final Label statusLabel, final ListModelList<TimeRange> model)
	{
		final TimeRange value = wim.getModel().getValue(getSettingPath(settingName), TimeRange.class);
		if (value != null && model.indexOf(value) >= 0)
		{
			model.setSelection(Lists.newArrayList(value));
			updateChosenRange(settingName, statusLabel, value);
		}
		else
		{
			final TimeRange firstAvailAble = model.get(0);
			updateChosenRange(settingName, statusLabel, firstAvailAble);
			model.setSelection(Lists.newArrayList(firstAvailAble));
		}
	}

	protected void updateChosenRange(final String settingName, final Label rangeStateLabel, final TimeRange timeRange)
	{
		wim.getModel().setValue(getSettingPath(settingName), timeRange);
		rangeStateLabel.setValue(timeRange.getLabel());
	}

	protected List<TimeRange> createTimeRanges(final String settingName)
	{
		final List<TimeRange> timeRanges = new ArrayList<>();

		final String rangesSetting = wim.getWidgetSettings().getString(settingName);
		final List<TimeRange> rangesFromSettings = getTimeRangeFactory().createTimeRanges(rangesSetting);

		if (CollectionUtils.isNotEmpty(rangesFromSettings))
		{
			timeRanges.addAll(rangesFromSettings);
		}
		else
		{
			final TimeRange defaultTimeRange = getTimeRangeFactory().createTimeRange(DEFAULT_TIME_RANGE);
			if (defaultTimeRange != null)
			{
				timeRanges.add(defaultTimeRange);
			}
		}

		return timeRanges;
	}

	protected void notifySettingChanged(final String settingName, final Object updatedValue)
	{
		listeners.forEach(listener -> listener.accept(settingName, updatedValue));
	}

	protected String getSettingPath(final String settingName)
	{
		return SETTINGS_PATH_PREFIX.concat(settingName);
	}

	public TimeRangeFactory getTimeRangeFactory()
	{
		if (timeRangeFactory == null)
		{
			timeRangeFactory = BackofficeSpringUtil.getBean("timeRangeFactory", TimeRangeFactory.class);
		}
		return timeRangeFactory;
	}

	public WidgetInstanceManager getWim()
	{
		return wim;
	}
}
