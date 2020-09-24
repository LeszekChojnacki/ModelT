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

package de.hybris.platform.configurablebundlecockpits.productcockpit.model.referenceeditor.simple.impl;

import de.hybris.platform.cockpit.model.editor.AdditionalReferenceEditorListener;
import de.hybris.platform.cockpit.model.editor.EditorListener;
import de.hybris.platform.cockpit.model.editor.impl.AbstractUIEditor;
import de.hybris.platform.cockpit.model.meta.ObjectType;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.model.referenceeditor.simple.SimpleReferenceSelector;
import de.hybris.platform.cockpit.model.referenceeditor.simple.SimpleSelectorModel;
import de.hybris.platform.cockpit.model.referenceeditor.simple.impl.DefaultSimpleReferenceSelectorController;
import de.hybris.platform.cockpit.model.referenceeditor.simple.impl.DefaultSimpleReferenceUIEditor;
import de.hybris.platform.cockpit.services.label.LabelService;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.session.impl.CreateContext;
import de.hybris.platform.cockpit.util.UITools;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Label;

public class MinimalReferenceUIEditor extends DefaultSimpleReferenceUIEditor
{

	/**
	 * 
	 */
	public MinimalReferenceUIEditor()
	{
		super();

	}

	/**
	 * @param rootType
	 */
	public MinimalReferenceUIEditor(final ObjectType rootType)
	{
		super(rootType);

	}

	@Override
	// NOSONAR
	public HtmlBasedComponent createViewComponent(final Object initialValue, final Map<String, ?> parameters,
			final EditorListener listener)
	{
		parseInitialParameters(parameters);
		if (isEditable())
		{
			simpleSelector = new SimpleReferenceSelector();
			simpleSelector.setAutocompletionAllowed(false); // no autocomplete
			simpleSelector.setDisabled(!isEditable());

			final Object createContext = parameters.get("createContext");
			if (createContext instanceof CreateContext)
			{
				simpleSelector.setCreateContext((CreateContext) createContext);
			}
			simpleSelector.setAllowcreate(isAllowCreate());

			final Integer maxAC = findMaxAutocompleteSearchResults(parameters);
			if (maxAC != null && maxAC.intValue() > 0)
			{
				model.setMaxAutoCompleteResultSize(maxAC.intValue());
			}

			model.setParameters(parameters);

			if (initialValue != null)
			{
				if (initialValue instanceof TypedObject)
				{
					model.setValue(initialValue);
				}
				else
				{
					throw new IllegalArgumentException("Initial value '" + initialValue + "' not a typed object.");
				}
			}
			else
			{
				model.setValue(initialValue);
			}
			if (UISessionUtils.getCurrentSession().isUsingTestIDs())
			{
				String id = "SimpleReferenceSelector_";
				String attQual = (String) parameters.get(AbstractUIEditor.ATTRIBUTE_QUALIFIER_PARAM);
				if (attQual != null)
				{
					attQual = attQual.replaceAll("\\W", "");
					id = id + attQual;
				}
				UITools.applyTestID(simpleSelector, id);
			}

			/*---------create test controller for Reference Selector---------- */
			if (selectorController != null)
			{
				selectorController.unregisterListeners();
			}

			final Object additionalListenerParam = parameters.get(AdditionalReferenceEditorListener.class.getName());
			AdditionalReferenceEditorListener additionalListener = null;
			if (additionalListenerParam instanceof AdditionalReferenceEditorListener)
			{
				additionalListener = (AdditionalReferenceEditorListener) additionalListenerParam;
			}

			selectorController = new DefaultSimpleReferenceSelectorController(model, simpleSelector, listener, additionalListener);
			selectorController.initialize();
			/*---------------------------------------------------------------- */
			simpleSelector.setModel(model);

			final CancelButtonContainer cancelButtonContainer = new CancelButtonContainer(listener, new CancelListener()
			{
				@Override
				public void cancelPressed()
				{
					simpleSelector.setFocus(false);
				}
			});
			cancelButtonContainer.setSclass("simpleReferenceEditorContainer");

			simpleSelector.addEventSelectorListener(SimpleReferenceSelector.EDIT_FINISH_EVENT, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD: ZK specific
				{
					cancelButtonContainer.showButton(false);
				}
			});

			simpleSelector.addEventSelectorListener(SimpleReferenceSelector.EDIT_START_EVENT, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD: ZK specific
				{
					cancelButtonContainer.showButton(true);
				}
			});

			simpleSelector.addEventSelectorListener(Events.ON_BLUR, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD: ZK specific
				{
					if (!simpleSelector.getModel().getMode().equals(SimpleSelectorModel.Mode.VIEW_MODE))
					{
						if (event.getTarget() instanceof Bandbox)
						{
							final Bandbox editorView = (Bandbox) event.getTarget();
							final String currentTextValue = editorView.getText();

							if (StringUtils.isBlank(currentTextValue) && getValue() != null)
							{
								simpleSelector.saveCurrentValue(null);
							}
						}
						listener.actionPerformed(EditorListener.CANCEL_CLICKED);
						cancelButtonContainer.showButton(false);

					}
				}
			});

			simpleSelector.addEventSelectorListener(Events.ON_OK, new EventListener()
			{
				@Override
				public void onEvent(final Event event) throws Exception //NOPMD: ZK specific
				{

					if (event.getTarget() instanceof Bandbox)
					{
						final Bandbox editorView = (Bandbox) event.getTarget();
						final String currentTextValue = editorView.getText();

						if (StringUtils.isEmpty(currentTextValue))
						{
							simpleSelector.saveCurrentValue(null);
						}
						else
						{
							simpleSelector.fireSaveActualSelected();
						}
						listener.actionPerformed(EditorListener.ENTER_PRESSED);
						cancelButtonContainer.showButton(false);
					}

				}
			});

			if (!isAllowActivate(parameters))
			{
				UITools.modifySClass(simpleSelector, "disallowActivate", true);
			}
			cancelButtonContainer.setContent(simpleSelector);
			return cancelButtonContainer;
		}
		else
		{
			final LabelService labelService = UISessionUtils.getCurrentSession().getLabelService();
			final Label label = initialValue != null ? new Label(labelService.getObjectTextLabel((TypedObject) initialValue))
					: new Label();
			return label;
		}
	}

	protected boolean isAllowActivate(final Map<String, ? extends Object> parameters)
	{
		boolean isActivate = true;
		final String paramIsActivate = (String) parameters.get("allowActivate");
		if (paramIsActivate != null)
		{
			isActivate = Boolean.parseBoolean(paramIsActivate);
		}
		return isActivate;
	}


}
