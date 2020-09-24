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
package de.hybris.platform.voucher.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.util.localization.Localization;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;


/**
 * A <i>promotion voucher </i> can be redeemed once or more times up to a user-defined limit.
 * <p />
 * It provides just one arbitrary, user-defined voucher code (ID) to enable promotions
 * <p />
 * with easily rememberable code tokens, such as "VINEGLAS".
 * <p />
 * Moreover promotion vouchers share a lot of functionality with the serial vouchers
 * <p />
 * and therefore have attributes like the voucher value,
 * <p />
 * the discount type, the free shipping option and the restrictions.
 *
 */
public class PromotionVoucher extends GeneratedPromotionVoucher //NOSONAR
{

	/*
	 * create the item you can delete this method if you don't want to intercept the creation of this item
	 */
	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		// ## business code placed here will be executed before the item is created
		final String voucherCode = (String) allAttributes.get(PromotionVoucher.VOUCHERCODE);
		if (StringUtils.isNotBlank(voucherCode))
		{
			final Voucher voucher = VoucherManager.getInstance(getSession()).getVoucher(voucherCode);
			if (voucher != null)
			{
				throw new JaloInvalidParameterException(MessageFormat
						.format(Localization.getLocalizedString("type.promotionvoucher.error.vouchercode.not.unique"), new Object[]
				{ voucherCode, voucher.getName() }), 0);
			}
		}
		return super.createItem(ctx, type, allAttributes);
	}

	@Override
	public boolean checkVoucherCode(final String aVoucherCode)
	{
		return aVoucherCode.equals(getVoucherCode());
	}

	@Override
	protected int getNextVoucherNumber(final SessionContext ctx)
	{
		return 1;
	}

	@Override
	public boolean isReservable(final String aVoucherCode, final User user)
	{
		return getInvalidations(aVoucherCode, user).size() < getRedemptionQuantityLimitPerUserAsPrimitive()
				&& getInvalidations(aVoucherCode).size() < getRedemptionQuantityLimitAsPrimitive();
	}

	@Override
	public void setVoucherCode(final SessionContext ctx, final String param)
	{
		if (StringUtils.isNotBlank(param))
		{
			final Voucher voucher = VoucherManager.getInstance(getSession()).getVoucher(param);
			if (voucher != null && voucher != this) //NOSONAR
			{
				throw new JaloInvalidParameterException(MessageFormat
						.format(Localization.getLocalizedString("type.promotionvoucher.error.vouchercode.not.unique"), new Object[]
				{ param, voucher.getName() }), 0);
			}
		}
		super.setVoucherCode(ctx, param);
	}
}
