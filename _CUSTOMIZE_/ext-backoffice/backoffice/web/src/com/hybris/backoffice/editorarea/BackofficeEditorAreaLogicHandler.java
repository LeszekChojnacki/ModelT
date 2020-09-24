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
package com.hybris.backoffice.editorarea;

import de.hybris.platform.core.model.ItemModel;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.core.Executable;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.MessageboxUtils;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaLogicHandler;


/**
 * Logic handler which checks if sync is running for an item being saved.
 */
public class BackofficeEditorAreaLogicHandler extends DefaultEditorAreaLogicHandler
{
	protected static final String SETTING_DISABLE_SAVE_ON_SYNC = "disableSaveOnSync";
	protected static final String LABEL_SYNC_IN_PROGRESS_MSG_SAVE = "confirmation.sync.inprogress.msg.save";
	protected static final String LABEL_SYNC_IN_PROGRESS_MSG_NO_SAVE = "confirmation.sync.inprogress.msg.nosave";
	protected static final String LABEL_SYNC_IN_PROGRESS_TITLE = "confirmation.sync.inprogress.title";
	protected static final String LABEL_BTN_CANCEL = "confirmation.sync.inprogress.btn.cancel";
	protected static final String LABEL_BTN_IGNORE_AND_SAVE = "confirmation.sync.inprogress.btn.ignore";

	private SynchronizationFacade synchronizationFacade;

	@Override
	public void executeSaveWithConfirmation(final WidgetInstanceManager wim, final Executable save, final Object currentObject)
	{
		if (isSyncInProgress(currentObject))
		{
			showSyncInProgressConfirmation(wim, () -> super.executeSaveWithConfirmation(wim, save, currentObject));
		}
		else
		{
			super.executeSaveWithConfirmation(wim, save, currentObject);
		}
	}

	protected boolean isSyncInProgress(final Object currentObject)
	{
		return currentObject instanceof ItemModel && synchronizationFacade.isSyncInProgress((ItemModel) currentObject);
	}

	protected void showSyncInProgressConfirmation(final WidgetInstanceManager wim, final Executable onIgnoreAndSave)
	{
		final boolean saveDisabled = wim.getWidgetSettings().getBoolean(SETTING_DISABLE_SAVE_ON_SYNC);
		final Messagebox.Button[] buttons = saveDisabled ?
				MessageboxUtils.order(Messagebox.Button.CANCEL)
				: MessageboxUtils.order(Messagebox.Button.CANCEL, Messagebox.Button.IGNORE);

		final String[] btnLabels = { Labels.getLabel(LABEL_BTN_CANCEL), Labels.getLabel(LABEL_BTN_IGNORE_AND_SAVE) };

		final String title = Labels.getLabel(LABEL_SYNC_IN_PROGRESS_TITLE);
		final String message = saveDisabled ?
				Labels.getLabel(LABEL_SYNC_IN_PROGRESS_MSG_NO_SAVE) :
				Labels.getLabel(LABEL_SYNC_IN_PROGRESS_MSG_SAVE);

		showMessageBox(title, message, buttons, btnLabels, (final Messagebox.ClickEvent event) ->
		{
			if (Messagebox.Button.IGNORE.equals(event.getButton()))
			{
				onIgnoreAndSave.execute();
			}
		});
	}

	protected void showMessageBox(final String title, final String message, final Messagebox.Button[] buttons,
			final String[] btnLabels, final EventListener<Messagebox.ClickEvent> clickEventListener)
	{
		Messagebox.show(message, title, buttons, btnLabels, Messagebox.EXCLAMATION, Messagebox.Button.CANCEL, clickEventListener);
	}


	@Required
	public void setSynchronizationFacade(
			final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}
}
