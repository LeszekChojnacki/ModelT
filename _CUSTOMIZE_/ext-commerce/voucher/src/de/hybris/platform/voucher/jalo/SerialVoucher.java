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

import de.hybris.platform.jalo.ConsistencyCheckAction;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.voucher.constants.VoucherConstants;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * A <i>serial voucher </i> provides a set of unique (system-generated) voucher codes (ID) that could be delivered to
 * customers, employees or something to enable them to redeem the voucher in an order.
 * <p />
 * Every voucher code may be redeemed once and once-only in a single order. All voucher codes of the same batch share
 * the same common base criteria such as the voucher value, the discount type, the free shipping option and the
 * restrictions.
 *
 */
public class SerialVoucher extends GeneratedSerialVoucher //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(SerialVoucher.class.getName());
	private static final String LASTVOUCHERNUMBER = "lastVoucherNumber";

	/**
	 * Returns <tt>true</tt> if the specified voucher code is valid for this voucher.
	 *
	 * @param aVoucherCode
	 *           the voucher code to check validity of.
	 * @return <tt>true</tt> if the specified voucher code is valid for this voucher, <tt>false</tt> else.
	 */
	@Override
	public boolean checkVoucherCode(final String aVoucherCode)
	{
		//check if the format of the voucher is correct.
		//we only accept the format XXX-XXXX-XXXX-XXXX
		final String pattern = ".{3}-.{4}-.{4}-.{4}";
		final boolean valid = Pattern.matches(pattern, aVoucherCode);
		if (!valid)
		{
			LOG.warn("Pattern of voucher code not valid: [" + aVoucherCode + "]");
			return false;
		}


		final String voucherCode = removeDividers(aVoucherCode);
		final String code = SerialVoucher.extractCode(voucherCode);
		if (getCode().equals(code))
		{
			final String clearText = voucherCode.substring(0, voucherCode.length() - CODELENGTH / 2);
			final String sig = voucherCode.substring(voucherCode.length() - CODELENGTH / 2);
			try
			{
				if (threeByteSig(clearText).equals(sig))
				{
					final int lastVoucherNumber = getLastVoucherNumber(getSession().getSessionContext());
					try //NOSONAR
					{
						final int voucherNumber = getVoucherNumber(voucherCode);
						return 0 <= voucherNumber && voucherNumber <= lastVoucherNumber;
					}
					catch (final InvalidVoucherKeyException e) //NOSONAR
					{
						return false;
					}
				}
			}
			catch (final NoSuchAlgorithmException e)
			{
				throw new JaloSystemException(e, "!!", 0);
			}
		}
		return false;
	}

	public static String extractCode(final String aVoucherCode)
	{
		final String voucherCode = removeDividers(aVoucherCode);
		if (voucherCode.length() > LENGTH_CODE)
		{
			return voucherCode.substring(0, LENGTH_CODE);
		}
		else
		{
			return null;
		}
	}

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		allAttributes.addConsistencyCheck(new ConsistencyCheckAction()
		{
			@Override
			public void execute(final ItemAttributeMap m) throws ConsistencyCheckException
			{
				final Set<String> missing = new HashSet<>();
				if (!checkMandatoryAttribute(CODE, m, missing))
				{
					throw new JaloInvalidParameterException("missing " + missing + " to create a new " + type.getCode(), 0);
				}
				else if (((String) m.get(CODE)).length() != LENGTH_CODE)
				{
					throw new JaloInvalidParameterException(
							Localization.getLocalizedString("type.serialvoucher.error.create.invalid.code.length"), 0);
				}
			}
		});

		return super.createItem(ctx, type, allAttributes);
	}

	private int getLastVoucherNumber(final SessionContext ctx)
	{
		final Object lastVoucherNumber = getProperty(ctx, LASTVOUCHERNUMBER);
		if (lastVoucherNumber != null)
		{
			return ((Integer) lastVoucherNumber).intValue();
		}
		return -1;
	}

	@Override
	protected int getNextVoucherNumber(final SessionContext ctx)
	{
		final int result = getLastVoucherNumber(ctx) + 1;
		setProperty(ctx, LASTVOUCHERNUMBER, Integer.valueOf(result));
		return result;
	}

	@Override
	public boolean isReservable(final String aVoucherCode, final User user)
	{
		// COM-11 serial vouchers must not be shared between multiple users
		return getInvalidations(aVoucherCode).isEmpty();
	}

	/**
	 * Sets the generated codes of this voucher.
	 *
	 * @param param
	 *           the generated codes of this voucher.
	 */
	@Override
	public void setCodes(final java.util.Collection param)
	{
		setProperty(VoucherConstants.CODES, param);
	}
}
