/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
/*
N * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.renderers;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.comments.model.CommentAttachmentModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.ticket.comparator.TicketEventsComparator;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventCsTicketStateEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketAttachmentsService;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticket.service.TicketService;
import de.hybris.platform.ticket.service.UnsupportedAttachmentException;
import de.hybris.platform.ticket.utils.AttachmentMediaUrlHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Script;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.editorarea.renderer.EditorAreaRendererUtils;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.AbstractEditorAreaPanelRenderer;

/**
 * Special renderer for Ticket -> General tab -> Correspondence section. looks like chat
 */
public class CorrespondenceRenderer extends AbstractEditorAreaPanelRenderer<CsTicketModel>
{
	private static final Logger LOG = Logger.getLogger(CorrespondenceRenderer.class);

	protected static final String CUSTOMER_SERVICE_STYLE_BOX = "yw-editorarea-editor-correspondence-customer-service";
	protected static final String CUSTOMER_STYLE_BOX = "yw-editorarea-editor-correspondence-customer";
	protected static final String SEND_BUTTON_STYLE = "yw-editorarea-correspondence-button-send y-btn-primary";
	protected static final String SEND_TEXTBOX_STYLE = "yw-editorarea-correspondence-textbox-send";

	protected static final String PRIVATE_MSG_TEXTBOX_STYLE = "ye-customersupport-textbox-private";
	protected static final String PUBLIC_MSG_TEXTBOX_STYLE = "ye-customersupport-textbox-public";
	protected static final String STATUS_MSG_TEXTBOX_STYLE = "ye-customersupport-textbox-status";
	protected static final String CUSTOMER_MSG_TEXTBOX_STYLE = "ye-customersupport-textbox";
	protected static final String MSG_TEXTBOX_STYLE_CLOSE = "ye-status-open-closed";
	protected static final String MSG_TEXTBOX_STYLE_REOPEN = "ye-status-closed-open";

	private static final String REFRESH_BUTTON_LISTENER_ID = "REFRESH_BUTTON_LISTENER_ID";

	private TicketService ticketService;
	private TicketBusinessService ticketBusinessService;
	private ModelService modelService;
	private CsTicketModel csTicketModel;
	private final Textbox replyTextbox = new Textbox();
	private TicketAttachmentsService ticketAttachmentsService;
	private WidgetInstanceManager widgetInstanceManager;
	private NotificationService notificationService;

	private final Set<MediaModel> attachments = new HashSet<>();

	private String allowedUploadedFormats;

	@Override
	public void render(final Component component, final AbstractPanel abstractPanel, final CsTicketModel csTicketModel,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{

		this.csTicketModel = csTicketModel;
		this.widgetInstanceManager = widgetInstanceManager;

		// listen for clicking on Refresh
		EditorAreaRendererUtils.setAfterCancelListener(widgetInstanceManager.getModel(), REFRESH_BUTTON_LISTENER_ID,
				event -> widgetInstanceManager.getWidgetslot().updateView(), false);

		// remove Save button
		removeSaveButton(component);

		final List<CsTicketEventModel> events = new ArrayList<>(ticketService.getEventsForTicket(csTicketModel));
		Collections.sort(events, new TicketEventsComparator());

		// add area for sending a message
		addSendArea(component);

		// adding message history
		final Vlayout allMessageBox = new Vlayout();
		for (final CsTicketEventModel csTicketEventModel : events)
		{
			final Set<CsTicketChangeEventEntryModel> entries = csTicketEventModel.getEntries();

			// make change entries block
			if (CollectionUtils.isNotEmpty(entries))
			{
				allMessageBox.appendChild(createEntryChangeBox(csTicketEventModel));
			}

			// make comment block
			if (StringUtils.isNotEmpty(csTicketEventModel.getText()))
			{
				allMessageBox.appendChild(createCommentBox(csTicketEventModel));
			}
		}
		component.appendChild(allMessageBox);
	}

	protected Component createFooter(final CsTicketEventModel ticket)
	{
		final Hlayout footer = new Hlayout();
		// append event time
		final SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a",
				getCockpitLocaleService().getCurrentLocale());
		final Label timeLabel = new Label(format.format(ticket.getCreationtime()));
		footer.appendChild(timeLabel);

		// add author
		if (ticket.getAuthor() != null && ticket.getAuthor() instanceof EmployeeModel)
		{
			final UserModel author = ticket.getAuthor();

			final Label authorLabel = new Label(author.getName() + " [" + author.getUid() + "]");
			footer.appendChild(authorLabel);
			footer.setSclass(CUSTOMER_SERVICE_STYLE_BOX);
		}
		else
		{
			final Label authorLabel = new Label(csTicketModel.getCustomer().getName());
			footer.appendChild(authorLabel);
			footer.setSclass(CUSTOMER_STYLE_BOX);
		}
		return footer;
	}

	protected Component createCommentBox(final CsTicketEventModel csTicketEventModel) //NOSONAR
	{
		final Vlayout oneMessageBox = new Vlayout();
		// add message
		if (StringUtils.isNotBlank(csTicketEventModel.getText()))
		{
			final Label messageLabel = new Label(csTicketEventModel.getText());
			final Div messageDiv = new Div();
			messageDiv.setSclass("z-textbox-message");

			//Make it blue for private messages.
			if (csTicketEventModel instanceof CsCustomerEventModel)
			{
				final CsInterventionType interventionType = ((CsCustomerEventModel) csTicketEventModel).getInterventionType();
				if (interventionType != null && interventionType.equals(CsInterventionType.PRIVATE))
				{
					UITools.modifySClass(oneMessageBox, PRIVATE_MSG_TEXTBOX_STYLE, true);
					messageLabel.setValue(interventionType + "\n\n" + messageLabel.getValue());
				}
				else if (csTicketEventModel.getAuthor() != null && csTicketEventModel.getAuthor() instanceof EmployeeModel)
				{
					UITools.modifySClass(oneMessageBox, PUBLIC_MSG_TEXTBOX_STYLE, true);
				}
				else
				{
					UITools.modifySClass(oneMessageBox, CUSTOMER_MSG_TEXTBOX_STYLE, true);
				}
			}

			messageLabel.setMultiline(true);

			if (!csTicketEventModel.getEntries().isEmpty())
			{
				if (hasMatchingStatusEvent(csTicketEventModel.getEntries(), CsTicketState.CLOSED, CsTicketState.OPEN))
				{
					UITools.modifySClass(oneMessageBox, MSG_TEXTBOX_STYLE_REOPEN, true);
				}
				else if (hasMatchingStatusEvent(csTicketEventModel.getEntries(), CsTicketState.OPEN, CsTicketState.CLOSED))
				{
					UITools.modifySClass(oneMessageBox, MSG_TEXTBOX_STYLE_CLOSE, true);
				}
			}

			messageDiv.appendChild(messageLabel);
			oneMessageBox.appendChild(messageDiv);
		}

		if (!CollectionUtils.isEmpty(csTicketEventModel.getAttachments()))
		{
			final Hlayout attachmentsList = new Hlayout();
			attachmentsList.setSclass("yw-editorarea-editor-correspondence-attachments");
			for (final CommentAttachmentModel attachmentModel : csTicketEventModel.getAttachments())
			{
				final Span attachmentSpan = new Span();
				attachmentSpan.setSclass("yw-editorarea-editor-correspondence-attachment");
				attachmentSpan.appendChild(createAttachmentLink((MediaModel) attachmentModel.getItem()));
				attachmentsList.appendChild(attachmentSpan);
			}
			oneMessageBox.appendChild(attachmentsList);
		}

		oneMessageBox.appendChild(createFooter(csTicketEventModel));
		return oneMessageBox;
	}

	protected Component createEntryChangeBox(final CsTicketEventModel csTicketEventModel)
	{
		final Vlayout oneMessageBox = new Vlayout();
		final StringBuilder text = new StringBuilder();
		for (final CsTicketChangeEventEntryModel e : csTicketEventModel.getEntries())
		{
			if (e instanceof CsTicketChangeEventCsTicketStateEntryModel)
			{
				text.append(e.getAlteredAttribute().getName() + ": "
						+ Labels.getLabel("customersupport_backoffice_tickets_inline_state_" + e.getOldStringValue().toLowerCase())
						+ " → "
						+ Labels.getLabel("customersupport_backoffice_tickets_inline_state_" + e.getNewStringValue().toLowerCase())
						+ "\n");
			}
			else
			{
				text.append(
						e.getAlteredAttribute().getName() + ": " + e.getOldStringValue() + " → " + e.getNewStringValue() + "\n");
			}
		}
		final Label changesLabel = new Label(text.toString());
		final Div changesDiv = new Div();
		UITools.modifySClass(oneMessageBox, STATUS_MSG_TEXTBOX_STYLE, true);
		changesDiv.appendChild(changesLabel);

		oneMessageBox.appendChild(changesDiv);
		oneMessageBox.appendChild(createFooter(csTicketEventModel));
		return oneMessageBox;
	}

	protected boolean hasMatchingStatusEvent(final Set<CsTicketChangeEventEntryModel> ticketEventEntries,
			final CsTicketState oldState, final CsTicketState newState)
	{
		for (final CsTicketChangeEventEntryModel e : ticketEventEntries)
		{
			if (e instanceof CsTicketChangeEventCsTicketStateEntryModel
					&& (e.getOldStringValue().equalsIgnoreCase(oldState.getCode())
							&& e.getNewStringValue().equalsIgnoreCase(newState.getCode())))
			{
				return true;
			}
		}
		return false;
	}

	protected void removeSaveButton(final Component component)
	{
		final Script jQscript = new Script();
		jQscript.setContent(
				"function hideShowNav() { if ($('.yw-editorarea-correspondence-textbox-send').length) { $('div.yw-editorarea-navi-container').hide(); $('div.ye-save-container').hide(); } "
						+ "else { $('div.ye-save-container').show();} };"
						+ "hideShowNav(); $('.yw-editor-area-main-content').bind('DOMSubtreeModified',function(){ hideShowNav() });");
		jQscript.setDefer(true);
		jQscript.setParent(component);
	}

	protected void addSendArea(final Component parent)
	{
		final Vlayout vlayout = new Vlayout();
		final Hlayout attachmentsList = new Hlayout();
		attachmentsList.setSclass("yw-editorarea-editor-correspondence-attachments");

		final Button sendButton = new Button(Labels.getLabel("customersupport_backoffice_tickets_correspondence_send"));

		replyTextbox.setSclass(SEND_TEXTBOX_STYLE);
		replyTextbox.setMultiline(true);
		sendButton.setSclass(SEND_BUTTON_STYLE);

		final Button attachButton = new Button();
		attachButton.setUpload("true,maxsize=10240");
		attachButton.setLabel(Labels.getLabel("customersupport_backoffice_tickets_correspondence_attach"));
		attachButton.addEventListener(Events.ON_UPLOAD, event -> { //NOSONAR

			final MediaModel mediaModel;
			try
			{
				mediaModel = createMediaModel(((UploadEvent) event).getMedia(), csTicketModel.getCustomer());
			}
			catch (final IOException | UnsupportedAttachmentException e)
			{
				LOG.error(e.getMessage(), e);
				getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(widgetInstanceManager),
						NotificationEventTypes.EVENT_TYPE_GENERAL, NotificationEvent.Level.FAILURE, e);
				return;
			}
			final Span attachmentSpan = new Span();
			attachmentSpan.setSclass("yw-editorarea-editor-correspondence-attachment");
			attachmentSpan.appendChild(createAttachmentLink(mediaModel));

			final A rm = new A("x");
			rm.setSclass("yw-editorarea-editor-correspondence-attachment-remove");

			getAttachments().add(mediaModel);

			rm.addEventListener(Events.ON_CLICK, removeEvent -> {
				getAttachments().remove(mediaModel);
				getModelService().remove(mediaModel);
				attachmentSpan.detach();
				if (CollectionUtils.isEmpty(getAttachments()))
				{
					attachmentsList.getChildren().clear();
				}
			});

			attachmentSpan.appendChild(rm);
			attachmentsList.appendChild(attachmentSpan);
		});

		//Reply to Customer / CS Support
		final Hlayout replyToHLayout = new Hlayout();


		final Label contactTypeLabel = new Label(Labels.getLabel("customersupport_backoffice_tickets_correspondence.contacttype"));
		final Combobox contactTypeCombo = configureContactTypeCombo();
		contactTypeCombo.setReadonly(true);
		replyToHLayout.appendChild(contactTypeLabel);
		replyToHLayout.appendChild(contactTypeCombo);

		final Div replyToDiv = new Div();
		final Label replyToCustomerLabel = new Label(Labels.getLabel("customersupport_backoffice_tickets_correspondence.replyto"));
		replyToDiv.appendChild(replyToCustomerLabel);

		final Radiogroup replyToTypeRadioGroup = configureReplyToRadioGroup();
		replyToDiv.appendChild(replyToTypeRadioGroup);
		replyToHLayout.appendChild(replyToDiv);
		replyToDiv.setSclass("yw-editorarea-editor-correspondence-replyto");

		vlayout.appendChild(replyToHLayout);

		//Add event listeners for radio and combo boxes.
		replyToTypeRadioGroup.addEventListener(Events.ON_CHECK,
				addReplyToRadioEventListener(replyToTypeRadioGroup, contactTypeCombo));
		contactTypeCombo.addEventListener(Events.ON_SELECT, addReplyToComboEventListener(replyToTypeRadioGroup, contactTypeCombo));

		vlayout.appendChild(replyTextbox);

		final Hlayout buttonsHlayout = new Hlayout();
		buttonsHlayout.appendChild(attachmentsList);
		buttonsHlayout.appendChild(attachButton);
		buttonsHlayout.appendChild(sendButton);
		vlayout.appendChild(buttonsHlayout);

		vlayout.setParent(parent);
		vlayout.setSclass("yw-reply-to-customer-container");

		sendButton.addEventListener(Events.ON_CLICK, handleButtonClick(contactTypeCombo));
	}

	/**
	 * Handles the button click events for the reply messages.
	 *
	 * @param contactTypeCombo
	 * @return EventListener
	 */
	protected EventListener handleButtonClick(final Combobox contactTypeCombo)
	{
		return event -> { //NOSONAR

			LOG.debug(replyTextbox.getValue());
			if (StringUtils.isBlank(replyTextbox.getValue()))
			{
				getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(widgetInstanceManager),
						NotificationEventTypes.EVENT_TYPE_GENERAL, NotificationEvent.Level.FAILURE,
						Labels.getLabel("customersupport_backoffice_tickets_correspondence_empty_message"));
				return;

			}
			final CsInterventionType contactType = contactTypeCombo.getSelectedItem().getValue();
			final CsCustomerEventModel csCustomerEventModel = ticketBusinessService.addNoteToTicket(csTicketModel, contactType,
					CsEventReason.UPDATE, replyTextbox.getValue(), getAttachments());
			replyTextbox.setValue(StringUtils.EMPTY);

			widgetInstanceManager.getWidgetslot().updateView();

			if (csCustomerEventModel != null)
			{
				if (CsInterventionType.PRIVATE.equals(contactType))
				{
					getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(widgetInstanceManager), CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
							NotificationEvent.Level.SUCCESS,
							Labels.getLabel("customersupport_backoffice_tickets_correspondence_private_messge_success"));
				}
				else
				{
					getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(widgetInstanceManager), CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
							NotificationEvent.Level.SUCCESS,
							Labels.getLabel("customersupport_backoffice_tickets_correspondence_customer_message_success") + " "
									+ csTicketModel.getCustomer().getDisplayName());
				}
			}
			else
			{
				getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(widgetInstanceManager), CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE, Labels.getLabel("customersupport_backoffice_tickets_correspondence_fail"));
			}
		};
	}

	/**
	 * Adds CSS class to the replyTextbox to highlight the color based on selection of the radio / combo item
	 *
	 * @param selectedInterventionType
	 */
	protected void highlightReplyTextBox(final CsInterventionType selectedInterventionType)
	{
		if (CsInterventionType.PRIVATE.equals(selectedInterventionType))
		{
			UITools.modifySClass(replyTextbox, PRIVATE_MSG_TEXTBOX_STYLE, true);
		}
		else
		{
			UITools.modifySClass(replyTextbox, PRIVATE_MSG_TEXTBOX_STYLE, false);
		}
	}

	/**
	 * @param contactTypeCombo
	 * @param replyToTypeRadioGroup
	 */
	protected EventListener addReplyToComboEventListener(final Radiogroup replyToTypeRadioGroup, final Combobox contactTypeCombo)
	{
		return new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final CsInterventionType selectedInterventionComboValue = contactTypeCombo.getSelectedItem().getValue();
				highlightReplyTextBox(selectedInterventionComboValue);
				if (CsInterventionType.PRIVATE.equals(selectedInterventionComboValue)
						&& !replyToTypeRadioGroup.getSelectedItem().getValue().equals(selectedInterventionComboValue.getCode()))
				{
					replyToTypeRadioGroup.setSelectedIndex(1);
				}
				else
				{
					replyToTypeRadioGroup.setSelectedIndex(0);
				}
			}
		};
	}

	/**
	 * @param contactTypeCombo
	 * @param replyToTypeRadioGroup
	 */
	protected EventListener addReplyToRadioEventListener(final Radiogroup replyToTypeRadioGroup, final Combobox contactTypeCombo)
	{
		return new EventListener()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final String selectedRadioValue = replyToTypeRadioGroup.getSelectedItem().getValue();
				highlightReplyTextBox(CsInterventionType.valueOf(selectedRadioValue));
				for (final Comboitem currentComboItem : contactTypeCombo.getItems())
				{
					if (currentComboItem.getValue().toString().equalsIgnoreCase(selectedRadioValue))
					{
						contactTypeCombo.setSelectedItem(currentComboItem);
						break;
					}
				}

			}
		};
	}

	protected MediaModel createMediaModel(final Media media, final UserModel customer) throws IOException
	{
		byte[] byteData;
		if (media.isBinary())
		{
			if (media.inMemory())
			{
				byteData = media.getByteData();
			}
			else
			{
				byteData = StreamUtils.getBytes(media.getStreamData());
			}
		}
		else
		{
			byteData = media.getStringData().getBytes();
		}
		return getTicketAttachmentsService().createAttachment(media.getName(), media.getContentType(), byteData, customer);
	}

	protected A createAttachmentLink(final MediaModel mediaModel)
	{
		final A attachmentLink = new A(mediaModel.getRealFileName());
		attachmentLink.setHref(AttachmentMediaUrlHelper.urlHelper(mediaModel.getURL()));
		attachmentLink.setTarget("_new");
		return attachmentLink;
	}

	protected Combobox configureContactTypeCombo()
	{
		final Combobox contactTypeCombo = new Combobox();
		final List<CsInterventionType> interventionTypes = ticketService.getInterventionTypes();
		for (final CsInterventionType csInterventionType : interventionTypes)
		{
			final Comboitem comboItem = new Comboitem();
			comboItem.setLabel(getLabelService().getObjectLabel(csInterventionType));
			comboItem.setValue(csInterventionType);
			contactTypeCombo.appendChild(comboItem);
			if (CsInterventionType.TICKETMESSAGE.equals(csInterventionType))
			{
				contactTypeCombo.setSelectedItem(comboItem);
			}
		}
		return contactTypeCombo;
	}

	protected Radiogroup configureReplyToRadioGroup()
	{
		final Radiogroup replyToTypeRadioGroup = new Radiogroup();

		replyToTypeRadioGroup.appendItem(Labels.getLabel("customersupport_backoffice_tickets_correspondence_replyto_customer"),
				CsInterventionType.TICKETMESSAGE.getCode());
		replyToTypeRadioGroup.appendItem(
				Labels.getLabel("customersupport_backoffice_tickets_correspondence_replyto_customersupport"),
				CsInterventionType.PRIVATE.getCode());

		replyToTypeRadioGroup.setSelectedIndex(0);
		return replyToTypeRadioGroup;
	}

	protected TicketBusinessService getTicketBusinessService()
	{
		return ticketBusinessService;
	}

	@Required
	public void setTicketBusinessService(final TicketBusinessService ticketBusinessService)
	{
		this.ticketBusinessService = ticketBusinessService;
	}

	protected TicketService getTicketService()
	{
		return ticketService;
	}

	@Required
	public void setTicketService(final TicketService ticketService)
	{
		this.ticketService = ticketService;
	}

	protected Set<MediaModel> getAttachments()
	{
		return attachments;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected TicketAttachmentsService getTicketAttachmentsService()
	{
		return ticketAttachmentsService;
	}

	@Required
	public void setTicketAttachmentsService(final TicketAttachmentsService ticketAttachmentsService)
	{
		this.ticketAttachmentsService = ticketAttachmentsService;
	}

	protected String getAllowedUploadedFormats()
	{
		return allowedUploadedFormats;
	}

	@Required
	public void setAllowedUploadedFormats(final String allowedUploadedFormats)
	{
		this.allowedUploadedFormats = allowedUploadedFormats;
	}
}
