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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.voucher.VoucherModelService;
import de.hybris.platform.voucher.model.VoucherModel;

import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;


/**
 * Inset for generating voucher code
 */
public class VoucherCodeGeneratorInset extends AbstractSingleButtonInset<String>
{
	private static final Logger LOG = LoggerFactory.getLogger(VoucherCodeGeneratorInset.class);

	public static final String VOUCHER_CODE_GENERATOR_INSET = "voucher-code-generator";

	protected static final String PARENT_OBJECT_PARAM = "parentObject";

	private VoucherModelService voucherModelService;
	private ModelService modelService;

	@Override
	protected EventListener<Event> getInsetListener(final Component parent, final EditorContext<String> context,
			final EditorListener<String> listener)
	{
		return event -> {
			final Object parentObject = context.getParameter(PARENT_OBJECT_PARAM);
			if (parentObject instanceof VoucherModel)
			{
				try
				{
					final String newVoucherCode = getVoucherModelService().generateVoucherCode((VoucherModel) parentObject);
					if (newVoucherCode != null && !newVoucherCode.equals(findAncestorEditor(parent).getValue()))
					{
						listener.onValueChanged(newVoucherCode);
					}
				}
				catch (final NoSuchAlgorithmException e)
				{
					LOG.error(e.getMessage(), e);
				}
			}
		};
	}

	@Override
	protected boolean isEnabled(final EditorContext<String> context)
	{
		final Object parentObject = context.getParameter(PARENT_OBJECT_PARAM);
		return (parentObject instanceof ItemModel && !getModelService().isNew(parentObject));
	}

	@Override
	protected String getSclass()
	{
		return VOUCHER_CODE_GENERATOR_INSET;
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
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}
}
