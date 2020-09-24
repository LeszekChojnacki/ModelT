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
package de.hybris.platform.voucher.backoffice.cockpitng.editor.insets;

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.voucher.VoucherModelService;
import de.hybris.platform.voucher.backoffice.cockpitng.editor.defaultinseteditor.DefaultInsetEditor;
import de.hybris.platform.voucher.model.SerialVoucherModel;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Intbox;

import com.google.common.base.Strings;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Inset for generating serial voucher codes
 */
public class SerialVoucherCodesGeneratorInset implements CockpitEditorRenderer<Object>
{
	protected static final String ERROR_WRONG_PARENT_TYPE_MSG = "serialVoucherCodesGenerator.wrongParentType";
	protected static final String EDITOR_PLACEHOLDER = "hmc.text.serialvoucher.setquantity.label";
	protected static final String GENERATE_BUTTON_LABEL = "hmc.btn.generate.voucher.codes";
	protected static final String UNEXPECTED_ERROR_MSG = "serialVoucherCodesGenerator.unexpectedError";

	public static final String INSET_SCLASS = "serial-voucher-codes-generator";
	public static final String BUTTON_SCLASS = "inset-button";
	public static final String TEXTBOX_SCLASS = "inset-textbox";

	protected static final String PARENT_OBJECT_PARAM = "parentObject";
	protected static final String CURRENT_OBJECT_PARAM = "currentObject";

	private ModelService modelService;
	private VoucherModelService voucherModelService;
	private MediaService mediaService;

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		if (parent == null || context == null || listener == null)
		{
			return;
		}

		final Div insetContainer = new Div();
		insetContainer.setSclass(DefaultInsetEditor.GENERAL_INSET_SCLASS + " " + INSET_SCLASS);
		insetContainer.setParent(parent);

		final WidgetInstanceManager wim = (WidgetInstanceManager) context.getParameter("wim");

		final Object parentObject = context.getParameter(PARENT_OBJECT_PARAM);
		if (!(parentObject instanceof SerialVoucherModel))
		{
			throw new IllegalStateException(Labels.getLabel(ERROR_WRONG_PARENT_TYPE_MSG));
		}

		final SerialVoucherModel serialVoucher = (SerialVoucherModel) parentObject;

		final Intbox editorView = new Intbox();
		editorView.setPlaceholder(Labels.getLabel(EDITOR_PLACEHOLDER));
		editorView.setSclass(TEXTBOX_SCLASS);
		editorView.setParent(insetContainer);
		editorView.setConstraint("no negative, no zero");
		editorView.setDisabled(isDisabled(serialVoucher));

		final Button button = new Button(Labels.getLabel(GENERATE_BUTTON_LABEL));
		button.setSclass(BUTTON_SCLASS);
		button.setParent(insetContainer);
		button.setDisabled(true);
		button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD
			{
				final Integer quantity = editorView.getValue();
				if (quantity != null)
				{
					try
					{
						generateVoucherCodes(serialVoucher, quantity);
					}
					catch (final IOException | JaloBusinessException ex)
					{
						throw new IllegalStateException(Labels.getLabel(UNEXPECTED_ERROR_MSG), ex);
					}
					wim.getModel().setValue(CURRENT_OBJECT_PARAM, serialVoucher);
				}
			}
		});

		editorView.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>()
		{
			@Override
			public void onEvent(final InputEvent event) throws Exception //NOPMD
			{
				final String value = event.getValue();
				button.setDisabled(Strings.isNullOrEmpty(value));
			}
		});
	}

	protected void generateVoucherCodes(final SerialVoucherModel serialVoucher, final Integer quantity)
			throws IOException, JaloBusinessException
	{
		final List<String> generatedCodes = new ArrayList<>();

		for (int i = 0; i < quantity.intValue(); i++)
		{
			try
			{
				generatedCodes.add(getVoucherModelService().generateVoucherCode(serialVoucher));
			}
			catch (final NoSuchAlgorithmException ex)
			{
				throw new IllegalStateException(UNEXPECTED_ERROR_MSG, ex);
			}
		}

		final MediaModel voucherCodesMedia = createMedia(generatedCodes, serialVoucher.getCode(), quantity.intValue());
		final Collection voucherCodes = serialVoucher.getCodes() != null ? new ArrayList(serialVoucher.getCodes())
				: new ArrayList();
		voucherCodes.add(voucherCodesMedia);
		serialVoucher.setCodes(voucherCodes);
	}

	protected MediaModel createMedia(final List<String> codes, final String actionCode, final int quantity)
	{
		final String mediaCode = quantity + " " + actionCode + " vouchercode" + (quantity == 1 ? "" : "s") + " ("
				+ new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS").format(new Date()) + ")";

		final CatalogUnawareMediaModel media = new CatalogUnawareMediaModel();
		media.setCode(mediaCode);
		getModelService().save(media);

		try
		{
			final InputStream is = IOUtils.toInputStream(StringUtils.join(codes, "\n"), "UTF-8");
			getMediaService().setStreamForMedia(media, is, mediaCode + ".csv", "text/csv");
		}
		catch (final IOException ex)
		{
			throw new IllegalStateException(UNEXPECTED_ERROR_MSG, ex);
		}

		return media;
	}

	protected boolean isDisabled(final SerialVoucherModel serialVoucher)
	{
		return getModelService().isNew(serialVoucher);
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected VoucherModelService getVoucherModelService()
	{
		return voucherModelService;
	}

	@Required
	public void setVoucherModelService(final VoucherModelService voucherModelService)
	{
		this.voucherModelService = voucherModelService;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

}
