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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.servicelayer.exceptions.BusinessException;
import de.hybris.platform.voucher.VoucherModelService;
import de.hybris.platform.voucher.VoucherService;
import de.hybris.platform.voucher.backoffice.cockpitng.editor.defaultinseteditor.DefaultInsetEditor;
import de.hybris.platform.voucher.model.VoucherModel;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;

import com.google.common.base.Strings;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Inset for applying vouchers to the {@link de.hybris.platform.core.model.order.AbstractOrderModel}
 */
public class VoucherApplierInset implements CockpitEditorRenderer<Object>
{
	protected static final String VOUCHER_CODE_PLACEHOLDER_LABEL = "voucherApplierInset.voucherCode.placeholder";
	protected static final String REDEEM_BUTTON_MSG = "hmc.btn.redeem.voucher";
	protected static final String RELEASE_BUTTON_MSG = "hmc.btn.release.voucher";
	protected static final String ERROR_WRONG_PARENT_TYPE_MSG = "voucherApplierInset.wrongParentType";
	protected static final String INVALID_VOUCHER_CODE_MSG = "hmc.error.voucher.invalid.vouchercode";
	protected static final String EMPTY_VOUCHER_CODE_MSG = "hmc.error.voucher.vouchercode.empty";
	protected static final String VOUCHER_VIOLATION_HEADER_MSG = "hmc.voucher.violation.header";
	protected static final String ERROR_INVALID_VOUCHER_CODE_MSG = "hmc.error.voucher.invalid.vouchercode";
	protected static final String ERROR_VOUCHER_ALREADY_APPLIED_MSG = "hmc.error.voucher.already.applied";
	protected static final String ERROR_VOUCHER_ALREADY_USED_MSG = "hmc.error.voucher.vouchercode.already.used";
	protected static final String ERROR_VOUCHER_TOTALPRICE_EXCEEDED_MSG = "hmc.error.voucher.totalprice.exceeded";
	protected static final String ERROR_UNKNOWN_MSG = "hmc.error.voucher.unknown";

	public static final String INSET_SCLASS = "voucher-applier";
	public static final String TEXTBOX_SCLASS = "inset-textbox";
	public static final String TEXTBOX_WRAPPER_SCLASS = "z-textbox-wrapper";
	public static final String BUTTON_SCLASS = "inset-button";
	public static final String REDEEM_BUTTON_SCLASS = "inset-button-redeem";
	public static final String RELEASE_BUTTON_SCLASS = "inset-button-release";
	public static final String BUTTON_WRAPPER_SCLASS = "z-button-wrapper";

	protected static final String PARENT_OBJECT_PARAM = "parentObject";
	protected static final String CURRENT_OBJECT_PARAM = "currentObject";

	private VoucherService voucherService;
	private VoucherModelService voucherModelService;
	private ObjectFacade objectFacade;

	private static final String ERROR_WHILE_APPLYING_VOUCHER = "Error while applying voucher: ";
	private static final String ERROR_VOUCHER_CANNOT_BE_REDEEMED = "Voucher cannot be redeemed: ";

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
		if (!(parentObject instanceof AbstractOrderModel))
		{
			throw new IllegalStateException(Labels.getLabel(ERROR_WRONG_PARENT_TYPE_MSG));
		}

		final AbstractOrderModel abstractOrder = (AbstractOrderModel) parentObject;

		final Div textboxWrapper = new Div();
		textboxWrapper.setSclass(TEXTBOX_WRAPPER_SCLASS);
		textboxWrapper.setParent(insetContainer);

		final Textbox editorView = new Textbox();
		editorView.setSclass(TEXTBOX_SCLASS);
		editorView.setPlaceholder(Labels.getLabel(VOUCHER_CODE_PLACEHOLDER_LABEL));
		editorView.setParent(textboxWrapper);

		final Div redeemButtonWrapper = new Div();
		redeemButtonWrapper.setSclass(BUTTON_WRAPPER_SCLASS);
		redeemButtonWrapper.setParent(insetContainer);

		final Button redeemButton = new Button(Labels.getLabel(REDEEM_BUTTON_MSG));
		redeemButton.setSclass(BUTTON_SCLASS + " " + REDEEM_BUTTON_SCLASS);
		redeemButton.setParent(redeemButtonWrapper);
		redeemButton.setDisabled(true);
		redeemButton.addEventListener(Events.ON_CLICK, new RedeemVoucherEventListener(editorView, abstractOrder, wim));

		final Div releaseButtonWrapper = new Div();
		releaseButtonWrapper.setSclass(BUTTON_WRAPPER_SCLASS);
		releaseButtonWrapper.setParent(insetContainer);

		final Button releaseButton = new Button(Labels.getLabel(RELEASE_BUTTON_MSG));
		releaseButton.setSclass(BUTTON_SCLASS + " " + RELEASE_BUTTON_SCLASS);
		releaseButton.setParent(releaseButtonWrapper);
		releaseButton.setDisabled(true);
		releaseButton.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception //NOPMD
			{
				editorView.clearErrorMessage();
				final String voucherCode = editorView.getValue();

				try
				{
					releaseVoucher(voucherCode, abstractOrder);
				}
				catch (final SimpleVoucherException ex)
				{
					throw new WrongValueException(editorView, ex.getMessage(), ex);
				}

				wim.getModel().setValue(CURRENT_OBJECT_PARAM, getObjectFacade().reload(abstractOrder));
			}
		});

		editorView.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>()
		{
			@Override
			public void onEvent(final InputEvent event) throws Exception //NOPMD
			{
				final String value = event.getValue();
				if (Strings.isNullOrEmpty(value))
				{
					redeemButton.setDisabled(true);
					releaseButton.setDisabled(true);
				}
				else
				{
					redeemButton.setDisabled(false);
					releaseButton.setDisabled(false);
				}
			}
		});
	}

	protected void applyVoucher(final String voucherCode, final AbstractOrderModel abstractOrder)
			throws VoucherException, VoucherViolationException, SimpleVoucherException
	{
		validateVoucherCode(voucherCode);

		synchronized (abstractOrder) //NOSONAR
		{
			final VoucherModel voucher = getVoucherService().getVoucher(voucherCode);
			if (!getVoucherModelService().isApplicable(voucher, abstractOrder))
			{
				throw new VoucherViolationException(ERROR_VOUCHER_CANNOT_BE_REDEEMED + voucherCode);
			}
			if (!getVoucherModelService().isReservable(voucher, voucherCode, abstractOrder))
			{
				throw new VoucherException(ERROR_VOUCHER_CANNOT_BE_REDEEMED + voucherCode);
			}

			if (abstractOrder instanceof OrderModel)
			{
				if (getVoucherModelService().redeem(voucher, voucherCode, (OrderModel) abstractOrder) == null)
				{
					throw new VoucherException(ERROR_WHILE_APPLYING_VOUCHER + voucherCode);
				}
			}
			else if (abstractOrder instanceof CartModel)
			{
				try
				{
					if (!getVoucherModelService().redeem(voucher, voucherCode, (CartModel) abstractOrder))
					{
						throw new VoucherException(ERROR_WHILE_APPLYING_VOUCHER + voucherCode);
					}
				}
				catch (final JaloPriceFactoryException ex)
				{
					throw new VoucherException(ERROR_WHILE_APPLYING_VOUCHER + voucherCode, ex);
				}
			}
			else
			{
				throw new UnsupportedOperationException("Unable to release voucher from " + abstractOrder.getItemtype() + "!");
			}
			//Important! Checking cart, if total amount <0, release this voucher
			checkOrderAfterRedeem(voucher, voucherCode, abstractOrder);
			return;

		}
	}

	protected void releaseVoucher(final String voucherCode, final AbstractOrderModel abstractOrder)
			throws VoucherException, SimpleVoucherException
	{
		validateVoucherCode(voucherCode);
		final VoucherModel voucher = getVoucherService().getVoucher(voucherCode);
		try
		{
			if (abstractOrder instanceof OrderModel)
			{
				getVoucherModelService().release(voucher, voucherCode, (OrderModel) abstractOrder);
			}
			else if (abstractOrder instanceof CartModel)
			{
				getVoucherModelService().release(voucher, voucherCode, (CartModel) abstractOrder);
			}
			else
			{
				throw new UnsupportedOperationException("Unable to release voucher from " + abstractOrder.getItemtype() + "!");
			}
		}
		catch (final JaloPriceFactoryException | ConsistencyCheckException ex)
		{
			throw new VoucherException("Couldn't release voucher: " + voucherCode, ex);
		}
	}

	protected void validateVoucherCode(final String voucherCode) throws SimpleVoucherException
	{
		if (StringUtils.isBlank(voucherCode))
		{
			throw new SimpleVoucherException(Labels.getLabel(EMPTY_VOUCHER_CODE_MSG));
		}

		final VoucherModel voucher = getVoucherService().getVoucher(voucherCode);
		if (voucher == null)
		{
			throw new SimpleVoucherException(Labels.getLabel(INVALID_VOUCHER_CODE_MSG));
		}
	}

	protected void checkOrderAfterRedeem(final VoucherModel voucher, final String voucherCode,
			final AbstractOrderModel abstractOrder) throws VoucherException, SimpleVoucherException
	{
		//Total amount in order updated with delay... Calculating value of voucher regarding to order
		final double cartTotal = abstractOrder.getTotalPrice().doubleValue();
		final double voucherValue = voucher.getValue().doubleValue();
		final double voucherCalcValue = (voucher.getAbsolute().equals(Boolean.TRUE)) ? voucherValue
				: (cartTotal * voucherValue) / 100;

		if (abstractOrder.getTotalPrice().doubleValue() - voucherCalcValue < 0)
		{
			releaseVoucher(voucherCode, abstractOrder);
			//Throw exception with specific information
			throw new SimpleVoucherException(Labels.getLabel(ERROR_VOUCHER_TOTALPRICE_EXCEEDED_MSG));
		}
	}

	@Required
	public void setVoucherService(final VoucherService voucherService)
	{
		this.voucherService = voucherService;
	}

	protected VoucherService getVoucherService()
	{
		return voucherService;
	}

	@Required
	public void setVoucherModelService(final VoucherModelService voucherModelService)
	{
		this.voucherModelService = voucherModelService;
	}

	protected VoucherModelService getVoucherModelService()
	{
		return voucherModelService;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	protected ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	protected static class VoucherException extends BusinessException
	{
		public VoucherException(final String message)
		{
			super(message);
		}

		public VoucherException(final String message, final Throwable cause)
		{
			super(message, cause);
		}
	}

	protected static class SimpleVoucherException extends BusinessException
	{
		public SimpleVoucherException(final String message)
		{
			super(message);
		}
	}

	protected static class VoucherViolationException extends BusinessException
	{
		public VoucherViolationException(final String message)
		{
			super(message);
		}
	}

	protected class RedeemVoucherEventListener implements EventListener<Event>
	{
		private final Textbox editorView;
		private final AbstractOrderModel abstractOrder;
		private final WidgetInstanceManager wim;

		public RedeemVoucherEventListener(final Textbox editorView, final AbstractOrderModel abstractOrder,
				final WidgetInstanceManager wim)
		{
			this.editorView = editorView;
			this.abstractOrder = abstractOrder;
			this.wim = wim;
		}

		@Override
		public void onEvent(final Event event) throws Exception //NOPMD
		{
			editorView.clearErrorMessage();
			final String voucherCode = editorView.getValue();

			try
			{
				applyVoucher(voucherCode, abstractOrder);
			}
			catch (final SimpleVoucherException ex)
			{
				throw new WrongValueException(editorView, ex.getMessage(), ex);
			}
			catch (final VoucherViolationException ex)
			{
				throw new WrongValueException(editorView, buildViolationMessage(voucherCode), ex);
			}
			catch (final VoucherException ex)
			{
				throw new WrongValueException(editorView, getErrorMessageLabel(voucherCode), ex);
			}

			wim.getModel().setValue(CURRENT_OBJECT_PARAM, getObjectFacade().reload(abstractOrder));
		}

		protected String buildViolationMessage(final String voucherCode)
		{
			final VoucherModel voucher = getVoucherService().getVoucher(voucherCode);
			final List<String> violationMessages = getVoucherModelService().getViolationMessages(voucher, abstractOrder);
			final StringBuilder message = new StringBuilder();
			message.append(Labels.getLabel(VOUCHER_VIOLATION_HEADER_MSG));
			final Iterator it = violationMessages.iterator();
			while (it.hasNext())
			{
				message.append("\n- ").append(it.next());
			}
			return message.toString();
		}

		protected String getErrorMessageLabel(final String voucherCode)
		{
			final String msgLabel = getErrorMessageLabelKey(voucherCode);
			return Labels.getLabel(msgLabel);
		}

		protected String getErrorMessageLabelKey(final String voucherCode)
		{
			final VoucherModel voucher = getVoucherService().getVoucher(voucherCode);
			String msgLabel;
			if (!getVoucherModelService().checkVoucherCode(voucher, voucherCode))
			{
				msgLabel = ERROR_INVALID_VOUCHER_CODE_MSG;
			}
			else if (abstractOrder.getDiscounts().contains(voucher))
			{
				msgLabel = ERROR_VOUCHER_ALREADY_APPLIED_MSG;
			}
			else if (!getVoucherModelService().isReservable(voucher, voucherCode, abstractOrder.getUser()))
			{
				msgLabel = ERROR_VOUCHER_ALREADY_USED_MSG;
			}
			else
			{
				msgLabel = ERROR_UNKNOWN_MSG;
			}
			return msgLabel;
		}
	}
}
