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
package de.hybris.platform.datahubbackoffice.presentation.widgets.quickupload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvents;
import com.hybris.cockpitng.testing.annotation.ExtensibleWidget;
import com.hybris.cockpitng.testing.annotation.NullSafeWidget;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

@DeclaredInput(value = QuickUploadController.DATAHUB_INFO_MODEL_PARAMETER, socketType = DataHubServer.class)
@DeclaredViewEvents({
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_CLEAR_BTN, eventName = Events.ON_CLICK),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_LOAD_BTN, eventName = Events.ON_CHECK),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_COMPOSE_BTN, eventName = Events.ON_CHECK),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_PUBLISH_BTN, eventName = Events.ON_CHECK),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_UPLOAD_BTN, eventName = Events.ON_UPLOAD),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_TEXTAREA, eventName = Events.ON_CHANGING),
@DeclaredViewEvent(componentID = QuickUploadController.COMPONENT_PROCESS, eventName = Events.ON_CLICK)})
@ExtensibleWidget(level = ExtensibleWidget.ALL)
@NullSafeWidget(false)
public class QuickUploadControllerUnitTest extends AbstractWidgetUnitTest<QuickUploadController>
{
	private static final String STRATEGY_KEY = "PROCESS";
	private static final String PROCESS = "process";

	@InjectMocks
	private QuickUploadController controller = new QuickUploadController();
	@Mock
	private Radiogroup radiogroup;
	private ProcessingStrategy strategy;

	@Mock
	protected Div composeContent;
	@Mock
	protected Div publishContent;
	@Mock
	protected Div loadSelectors;
	@Mock
	protected NotificationService notificationService;
	@Mock
	protected Div main;
	@Captor
	private ArgumentCaptor<Map<String, Object>> ctxCaptor;

	@Before
	public void setUp()
	{
		strategy = strategyForCode(STRATEGY_KEY);
		givenProcessingStrategiesApplied(strategy);
		controller.setNotificationService(notificationService);
	}

	@Test
	public void testProcessNotSelected()
	{
		givenProcessNotSelected();

		executeViewEvent(PROCESS, Events.ON_CLICK);

		verifyZeroInteractions(strategy);
	}

	@Test
	public void testUnknownProcessSelected()
	{
		givenProcessSelected("UNKNOWN");

		executeViewEvent(PROCESS, Events.ON_CLICK);

		verify(strategy, never()).process(any(QuickUploadController.class), anyString(), any());
	}

	@Test
	public void testKnownProcessSelected()
	{
		final ProcessingStrategy one = strategyForCode("one");
		final ProcessingStrategy two = strategyForCode("two");
		givenProcessingStrategiesApplied(one, two);
		givenProcessSelected("two");

		executeViewEvent(PROCESS, Events.ON_CLICK);

		verify(one, never()).process(any(QuickUploadController.class), anyString(), any());
		verify(two).process(same(controller), eq("two"), any());
	}

	@Test
	public void testContextCreationForTheSelectedProcess()
	{
		givenProcessSelected(STRATEGY_KEY);

		executeViewEvent(PROCESS, Events.ON_CLICK);

		verify(strategy).process(any(QuickUploadController.class), anyString(), ctxCaptor.capture());
		final Map<String, Object> ctx = ctxCaptor.getValue();
		assertThat(ctx).containsKeys("selectedFeed", "selectedType", "selectedPoolForCompose", "selectedPoolForPublish",
				"selectedTargetSystems", "datahubInfo");
	}

	private void givenProcessSelected(final String key)
	{
		final Radio option = mock(Radio.class);
		doReturn(key).when(option).getValue();
		doReturn(option).when(radiogroup).getSelectedItem();
	}

	private void givenProcessNotSelected()
	{
		doReturn(null).when(radiogroup).getSelectedItem();
	}

	private void givenProcessingStrategiesApplied(final ProcessingStrategy... strategies)
	{
		controller.setProcessingStrategies(Arrays.asList(strategies));
	}

	private ProcessingStrategy strategyForCode(final String key)
	{
		final ProcessingStrategy s = mock(ProcessingStrategy.class);
		doReturn(true).when(s).supports(key);
		return s;
	}

	@Override
	protected QuickUploadController getWidgetController()
	{
		return controller;
	}
}