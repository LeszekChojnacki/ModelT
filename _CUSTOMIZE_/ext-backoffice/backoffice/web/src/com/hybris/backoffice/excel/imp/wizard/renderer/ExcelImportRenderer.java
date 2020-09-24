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
package com.hybris.backoffice.excel.imp.wizard.renderer;

import de.hybris.platform.util.Config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zkmax.zul.Dropupload;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;

import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.imp.ExcelValidator;
import com.hybris.backoffice.excel.imp.wizard.ExcelImportWizardForm;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


/**
 * Renders wizard's content for importing excel files.
 * @deprecated since 6.7 use separate file upload editors
 */
@Deprecated
public class ExcelImportRenderer extends DefaultCustomViewRenderer
{

	private ExcelValidator excelValidator;

	protected static final String PREFIX_SCLASS_EXCEL_IMPORT_WIZARD = "yw-excel-import-wizard";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_COMPONENT = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD + "-component";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_FILEUPLOAD = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD + "-fileupload";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_DROP_UPLOAD = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD + "-drop-upload";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_DROP_ANCHOR = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD + "-drop-anchor";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_ATTACHMENTS = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD + "-attachments";
	protected static final String YE_DELETE_BTN = "ye-delete-btn";
	protected static final String SCLASS_EXCEL_IMPORT_WIZARD_ATTACHMENTS_BTN = PREFIX_SCLASS_EXCEL_IMPORT_WIZARD
			+ "-attachments-btn";

	protected static final String LABEL_EXCEL_IMPORT_ATTACHMENTS = "excel.import.wizard.attachments";
	protected static final String LABEL_EXCEL_IMPORT_DROPCONTENT = "excel.import.wizard.dropcontent";
	protected static final String LABEL_EXCEL_IMPORT_UPLOAD = "excel.import.wizard.upload";

	protected static final String ICON_SCLASS = "z-icon-paperclip";

	protected static final String BACKOFFICE_FILE_UPLOAD_MAX_SIZE = "backoffice.fileUpload.maxSize";
	protected static final int DEFAULT_MAX_FILE_SIZE = 10000;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final Component fileUpload = createFileUpload();
		final Component dropAnchor = createAnchor();
		final Component dropUpload = createDropUpload(dropAnchor);
		final Groupbox attachmentsPlaceholder = createAttachmentsPlaceholder(wim);

		fileUpload.addEventListener(Events.ON_UPLOAD, event -> dragUploadFiles((UploadEvent) event, wim, attachmentsPlaceholder));
		dropUpload.addEventListener(Events.ON_UPLOAD, event -> dragUploadFiles((UploadEvent) event, wim, attachmentsPlaceholder));

		parent.appendChild(fileUpload);
		parent.appendChild(dropUpload);
		parent.appendChild(dropAnchor);
		parent.appendChild(attachmentsPlaceholder);
		attachmentsPlaceholder.addForward(Editor.ON_VALUE_CHANGED, parent, Editor.ON_VALUE_CHANGED);
	}

	protected Component createAnchor()
	{
		final Div dropAnchor = new Div();
		dropAnchor.setSclass(SCLASS_EXCEL_IMPORT_WIZARD_DROP_ANCHOR);

		final Label label = new Label(Labels.getLabel(LABEL_EXCEL_IMPORT_DROPCONTENT));
		label.setParent(dropAnchor);

		return dropAnchor;
	}

	protected Component createFileUpload()
	{
		final Button fileUpload = new Button();
		fileUpload.setLabel(Labels.getLabel(LABEL_EXCEL_IMPORT_UPLOAD));
		fileUpload.setUpload(getUpload());
		fileUpload.setSclass(SCLASS_EXCEL_IMPORT_WIZARD_FILEUPLOAD + " " + SCLASS_EXCEL_IMPORT_WIZARD_COMPONENT);
		return fileUpload;
	}

	protected Component createDropUpload(final Component dropAnchor)
	{
		final Dropupload dropUpload = new Dropupload();
		dropUpload.setAnchor(dropAnchor);
		dropUpload.setContent(Labels.getLabel(LABEL_EXCEL_IMPORT_DROPCONTENT));
		dropUpload.setSclass(SCLASS_EXCEL_IMPORT_WIZARD_DROP_UPLOAD);
		dropUpload.setMaxsize(getMaxFileSize());
		dropUpload.setNative(true);
		return dropUpload;
	}

	protected Groupbox createAttachmentsPlaceholder(final WidgetInstanceManager wim)
	{
		final Set<FileUploadResult> fileUploadResults = getCurrentExcelModel(wim).getFileUploadResult();

		final Groupbox groupbox = new Groupbox();
		groupbox.setTitle(Labels.getLabel(LABEL_EXCEL_IMPORT_ATTACHMENTS));
		groupbox.setOpen(true);
		groupbox.setSclass(SCLASS_EXCEL_IMPORT_WIZARD_COMPONENT);
		groupbox.setVisible(!fileUploadResults.isEmpty());

		renderAttachments(groupbox, wim, fileUploadResults);
		return groupbox;
	}

	protected void renderAttachments(final Groupbox placeholder, final WidgetInstanceManager wim,
			final Set<FileUploadResult> fileUploadResults)
	{
		placeholder.getChildren().clear();
		fileUploadResults.forEach(upload -> renderAttachment(placeholder, wim, upload));
	}

	protected void renderAttachment(final Groupbox placeholder, final WidgetInstanceManager wim,
			final FileUploadResult fileUploadResult)
	{
		final Div container = new Div();
		container.setSclass(SCLASS_EXCEL_IMPORT_WIZARD_ATTACHMENTS);

		final Button removeAttachmentButton = new Button();
		removeAttachmentButton.setSclass(String.format("%s %s", SCLASS_EXCEL_IMPORT_WIZARD_ATTACHMENTS_BTN, YE_DELETE_BTN));
		removeAttachmentButton.setParent(container);

		final Span attachment = new Span();

		final A a = new A();
		a.setIconSclass(ICON_SCLASS);
		a.setLabel(fileUploadResult.getName());
		a.setParent(attachment);

		attachment.setParent(container);
		container.setParent(placeholder);

		removeAttachmentButton.addEventListener(Events.ON_CLICK, e -> {
			placeholder.removeChild(container);

			getCurrentExcelModel(wim).getFileUploadResult().remove(fileUploadResult);
			attachmentsListChanged(placeholder, wim);
		});
	}

	protected void attachmentsListChanged(final Groupbox placeholder, final WidgetInstanceManager wim)
	{
		placeholder.setVisible(!getCurrentExcelModel(wim).getFileUploadResult().isEmpty());
		Events.sendEvent(Editor.ON_VALUE_CHANGED, placeholder, null);
	}

	protected void dragUploadFiles(final UploadEvent event, final WidgetInstanceManager wim, final Groupbox attachmentsPlaceholder)
	{
		final Set<FileUploadResult> currentUploadResults = getCurrentExcelModel(wim).getFileUploadResult();

		Stream.of(event.getMedias()).forEach(media -> {
			final FileUploadResult fileUploadResult = new FileUploadResult();
			fileUploadResult.setData(media.getByteData());
			fileUploadResult.setFormat(media.getFormat());
			fileUploadResult.setContentType(media.getContentType());
			fileUploadResult.setName(media.getName());
			addFileResult(currentUploadResults, fileUploadResult, attachmentsPlaceholder, wim);
		});
	}

	protected void addFileResult(final Set<FileUploadResult> fileUploadResults, final FileUploadResult fileUploadResult,
			final Groupbox attachmentsPlaceholder, final WidgetInstanceManager wim)
	{
		if (excelValidator.isCorrectFormat(fileUploadResult))
		{
			removeAttachmentOfType(fileUploadResults, fileUploadResult.getFormat());
			fileUploadResults.add(fileUploadResult);
			attachmentsListChanged(attachmentsPlaceholder, wim);
			renderAttachments(attachmentsPlaceholder, wim, fileUploadResults);
		}
	}

	protected void removeAttachmentOfType(final Set<FileUploadResult> fileUploadResults, final String fileFormat)
	{
		final Optional<FileUploadResult> foundFileOfType = fileUploadResults.stream()
				.filter(upload -> fileFormat.equals(upload.getFormat())).findFirst();
		foundFileOfType.ifPresent(fileUploadResults::remove);
	}

	protected ExcelImportWizardForm getCurrentExcelModel(final WidgetInstanceManager wim)
	{
		return wim.getModel().getValue(ExcelConstants.EXCEL_FORM_PROPERTY, ExcelImportWizardForm.class);
	}

	protected String getUpload()
	{
		return String.format("true,maxsize=%d,accept=%s",
				getMaxFileSize(), getExcelValidator().getFormats().stream().map(e -> "." + e).reduce((e, f) -> e + "|" + f).get());
	}

	public int getMaxFileSize()
	{
		return Config.getInt(BACKOFFICE_FILE_UPLOAD_MAX_SIZE, DEFAULT_MAX_FILE_SIZE);
	}

	public ExcelValidator getExcelValidator()
	{
		return excelValidator;
	}

	@Required
	public void setExcelValidator(final ExcelValidator excelValidator)
	{
		this.excelValidator = excelValidator;
	}



}
