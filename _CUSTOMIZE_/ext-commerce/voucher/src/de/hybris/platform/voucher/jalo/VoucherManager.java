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

import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericQuery;
import de.hybris.platform.core.GenericSearchField;
import de.hybris.platform.core.GenericSearchOrderBy;
import de.hybris.platform.core.GenericSelectField;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.extension.ExtensionNotFoundException;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.OrderManager;
import de.hybris.platform.jalo.order.price.Discount;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.JspContext;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.voucher.constants.VoucherConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * The manager for voucher items.
 */
public class VoucherManager extends GeneratedVoucherManager
{
	private static final Logger LOG = Logger.getLogger(VoucherManager.class);

	/**
	 * Creates essential data for the voucher extension.
	 */
	@Override
	public void createEssentialData(final Map params, final JspContext jspc)
	{
		try
		{
			// setting default values for voucher name
			for (final ComposedType ct : getComposedType(VoucherConstants.TC.VOUCHER).getAllSubTypes())
			{
				setDefaultValues(ct, Voucher.NAME);
			}
			// setting default values for restriction description and violation message
			for (final ComposedType type : getComposedType(VoucherConstants.TC.RESTRICTION).getAllSubTypes())
			{
				setDefaultValues(type, Restriction.DESCRIPTION);
				setDefaultValues(type, Restriction.VIOLATIONMESSAGE);
			}
			//#5344 setting default values explicit for subtypes of productrestricions again - above it will be overwritten
			// HACK: XXX: if somebody knows a better way tell me
			for (final ComposedType type : getComposedType(VoucherConstants.TC.PRODUCTRESTRICTION).getAllSubTypes())
			{
				setDefaultValues(type, Restriction.DESCRIPTION);
				setDefaultValues(type, Restriction.VIOLATIONMESSAGE);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error while creating essential data vor voucher extension", e);
		}
	}

	/**
	 * Transfers vouchers form cart to newly created order. Please make sure to call this always after
	 * {@link OrderManager#createOrder(AbstractOrder)}.
	 *
	 * @param order
	 *           the newly created order
	 * @param cart
	 *           the cart which this order has been created from
	 */
	public void afterOrderCreation(final Order order, final Cart cart)
	{
		removeCopiedVouchers(order, cart);
		afterOrderCreation(getSession().getSessionContext(), order, cart);
	}

	/**
	 * Transfers vouchers form cart to newly created order. Please make sure to call this always after
	 * {@link OrderManager#createOrder(AbstractOrder)}.
	 *
	 * @param ctx
	 *           the session context
	 * @param order
	 *           the newly created order
	 * @param cart
	 *           the cart which this order has been created from
	 */
	public void afterOrderCreation(final SessionContext ctx, final Order order, final Cart cart) //NOSONAR
	{
		final Collection<String> appliedCodes = new HashSet<>(getAppliedVoucherCodes(ctx, cart));
		if (!appliedCodes.isEmpty())
		{
			// remove all codes which are applied well (calling our method getAppliedVoucherCodes(ctx,Order) which checks invalidations only
			appliedCodes.removeAll(this.getAppliedVoucherCodes(ctx, order));
			// collect invalid vouchers
			final List<Voucher> invalid = new ArrayList<>();
			for (final String codeToApply : appliedCodes)
			{
				VoucherInvalidation inv = null;
				try
				{
					inv = redeemVoucher(codeToApply, order);
				}
				catch (final Exception e)
				{
					LOG.error("redeemVoucher failed", e);
				}
				// throw out all voucher which could not be applied
				if (inv == null)
				{
					final Voucher v = getVoucher(codeToApply);
					if (v != null) //NOSONAR
					{
						invalid.add(v);
					}
				}
			}
			if (!invalid.isEmpty())
			{
				final List<Discount> discounts = new ArrayList<>(order.getDiscounts());
				final List<DiscountValue> discountValues = new ArrayList<>(order.getGlobalDiscountValues());  //NOSONAR
				for (final Voucher v : invalid)
				{
					// remove from discounts list
					discounts.remove(v);
					// remove actual discount value
					final String vCode = v.getCode();
					for (int i = 0; i < discountValues.size(); i++) //NOSONAR
					{
						final DiscountValue dv = discountValues.get(i);
						if (vCode.equals(dv.getCode()))
						{
							discountValues.remove(i);
							break;
						}
					}
				}
				order.setDiscounts(discounts);
				order.setGlobalDiscountValues(discountValues); //NOSONAR
				try
				{
					order.calculateTotals(false);//NOSONAR
				}
				catch (final JaloPriceFactoryException e)
				{
					throw new JaloSystemException(e);
				}
			}
		}
		// clear code property again -> now we've got VoucherInvalidationItems instead
		super.setAppliedVoucherCodes(ctx, order, null);
	}

	private void setDefaultValues(final ComposedType type, final String attributeDescriptorQualifier)
	{
		final SessionContext ctx = getSession().createSessionContext();
		ctx.setLanguage(null);
		final AttributeDescriptor attributeDescriptor = type.getAttributeDescriptor(attributeDescriptorQualifier);
		final StringBuilder key = new StringBuilder();
		key.append("type.");
		key.append(type.getCode().toLowerCase());
		key.append(".");
		key.append(attributeDescriptorQualifier.toLowerCase());
		key.append(".");
		key.append("defaultvalue");
		attributeDescriptor.setDefaultValue(ctx, Localization.getLocalizedMap(key.toString()));
	}

	/**
	 *
	 * @return a Collection with all found {@link Voucher}s
	 */
	public Collection getAllVouchers()
	{
		return getSession().search(new GenericQuery(VoucherConstants.TC.VOUCHER)).getResult();
	}

	public Collection<String> getAppliedVoucherCodes(final Cart item)
	{
		final Collection<String> ret = super.getAppliedVoucherCodes(item);
		return ret != null ? Collections.unmodifiableCollection(ret) : Collections.emptyList();
	}

	public Collection<String> getAppliedVoucherCodes(final Order order)
	{
		return getAppliedVoucherCodes(getSession().getSessionContext(), order);
	}

	@SuppressWarnings("unused")
	public Collection<String> getAppliedVoucherCodes(final SessionContext ctx, final Order order) //NOSONAR
	{
		final GenericQuery q = new GenericQuery(VoucherConstants.TC.VOUCHERINVALIDATION,
				GenericCondition.and(GenericCondition.equals(VoucherInvalidation.ORDER, order),
						GenericCondition.equals(VoucherInvalidation.STATUS, VoucherInvalidation.STATUS_CONFIRMED)));
		q.addSelectField(new GenericSelectField(VoucherInvalidation.CODE, String.class));
		q.addOrderBy(new GenericSearchOrderBy(new GenericSearchField(VoucherInvalidation.CREATION_TIME), true));
		q.addOrderBy(new GenericSearchOrderBy(new GenericSearchField(VoucherInvalidation.PK), true));
		return new LinkedHashSet<>(getSession().search(q).getResult());
	}

	public Collection<String> getAppliedVoucherCodes(final SessionContext ctx, final Cart item)
	{
		final Collection<String> ret = super.getAppliedVoucherCodes(ctx, item);
		return ret != null ? Collections.unmodifiableCollection(ret) : Collections.emptyList();
	}

	/**
	 *
	 * @param anOrder
	 *           the given order
	 * @return a Collection with all applied {@link Voucher}s for this order
	 */
	public Collection getAppliedVouchers(final AbstractOrder anOrder)
	{
		final Collection result = new HashSet();
		for (final Iterator iterator = anOrder.getDiscounts().iterator(); iterator.hasNext();)
		{
			final Discount nextDiscount = (Discount) iterator.next();
			if (nextDiscount instanceof Voucher)
			{
				result.add(nextDiscount);
			}
		}
		return result;
	}

	/**
	 * Gets the Instance of the VoucherManager
	 *
	 * @param jaloSession
	 *           the Jalo Session
	 * @return the Instance of the VoucherManager
	 */
	public static VoucherManager getInstance(final JaloSession jaloSession)
	{
		try
		{
			return (VoucherManager) jaloSession.getExtensionManager().getExtension(VoucherConstants.EXTENSIONNAME);
		}
		catch (final ExtensionNotFoundException e) //NOSONAR
		{
			return null;
		}
	}

	/**
	 * @return instance of this manager
	 */
	public static VoucherManager getInstance()
	{
		return getInstance(JaloSession.getCurrentSession());
	}

	/**
	 * @param voucherCode
	 *           the code
	 * @return the first {@link Voucher} for the given code. This includes {@link PromotionVoucher} and
	 *         {@link SerialVoucher}.
	 */
	public Voucher getVoucher(final String voucherCode)
	{
		Collection<Voucher> result = getPromotionVouchers(voucherCode);
		if (result.isEmpty())
		{
			result = getSerialVouchers(voucherCode);
		}

		return result.isEmpty() ? null : result.iterator().next();
	}

	/**
	 *
	 * @param voucherCode
	 *           the code
	 * @return a Collection of {@link PromotionVoucher}s
	 */
	public Collection getPromotionVouchers(final String voucherCode)
	{
		return getSession().search(new GenericQuery(VoucherConstants.TC.PROMOTIONVOUCHER,
				GenericCondition.equals(PromotionVoucher.VOUCHERCODE, voucherCode))).getResult();
	}

	/**
	 *
	 * @param aVoucherCode
	 *           the code
	 * @return a Collection of {@link SerialVoucher}s
	 */
	public Collection getSerialVouchers(final String aVoucherCode)
	{
		final String code = SerialVoucher.extractCode(aVoucherCode);
		if (code == null)
		{
			return Collections.emptyList();
		}
		final Collection<SerialVoucher> ret = new LinkedHashSet<>();
		for (final SerialVoucher voucher : (List<SerialVoucher>) getSession()
				.search(new GenericQuery(VoucherConstants.TC.SERIALVOUCHER, GenericCondition.equals(Voucher.CODE, code))).getResult())
		{
			if (voucher.checkVoucherCode(aVoucherCode))
			{
				ret.add(voucher);
			}
		}
		return Collections.unmodifiableCollection(ret);
	}

	private ComposedType getComposedType(final String code)
	{
		try
		{
			final ComposedType type = getSession().getTypeManager().getComposedType(code); //NOSONAR
			if (type == null)
			{
				throw new JaloSystemException(null, "got type null for " + code, 0);
			}
			return type;
		}
		catch (final JaloItemNotFoundException e)
		{
			throw new JaloSystemException(e, "required type missing", 0);
		}
	}

	/**
	 * For the given {@link Cart} the given {@link Voucher} will be redeem.
	 *
	 * <p>
	 * <b>WARNING!</b> <br>
	 * If some methods for checking voucher availability are called before this method, all these methods have to be in
	 * one synchronize block! Suggested synchronization object is cart.
	 * </p>
	 *
	 * @param aVoucherCode
	 *           code of the Voucher
	 * @param aCart
	 *           the cart
	 * @return true if the redemption was successful
	 * @throws JaloPriceFactoryException
	 */
	public boolean redeemVoucher(final String aVoucherCode, final Cart aCart) throws JaloPriceFactoryException
	{
		final Voucher voucher = getVoucher(aVoucherCode);
		if (voucher != null)
		{
			return voucher.redeem(aVoucherCode, aCart);
		}
		return false;
	}


	public VoucherInvalidation redeemVoucher(final String aVoucherCode, final Order anOrder)
	{
		final Voucher voucher = getVoucher(aVoucherCode);
		if (voucher != null)
		{
			return voucher.redeem(aVoucherCode, anOrder);
		}
		return null;
	}

	public void releaseVoucher(final String aVoucherCode, final Cart aCart) throws JaloPriceFactoryException
	{
		final Voucher voucher = getVoucher(aVoucherCode);
		if (voucher != null)
		{
			voucher.release(aVoucherCode, aCart);
		}
	}

	public void releaseVoucher(final String aVoucherCode, final Order anOrder) throws ConsistencyCheckException
	{
		final Voucher voucher = getVoucher(aVoucherCode);
		if (voucher != null)
		{
			voucher.release(aVoucherCode, anOrder);
		}
	}

	public VoucherInvalidation reserveVoucher(final String aVoucherCode, final Order anOrder)
	{
		final Voucher voucher = getVoucher(aVoucherCode);
		if (voucher != null)
		{
			return voucher.reserve(aVoucherCode, anOrder);
		}
		return null;
	}

	private void removeCopiedVouchers(final Order order, final Cart cart)
	{
		final VoucherManager vm = VoucherManager.getInstance();
		final Collection<String> appliedVoucherCodes = vm.getAppliedVoucherCodes(cart); //NOSONAR
		if (CollectionUtils.isNotEmpty(appliedVoucherCodes))
		{
			final List<Discount> discounts = order.getDiscounts();
			final List<Discount> newOnes = new ArrayList<>();
			for (final Discount d : discounts)
			{
				if (!(d instanceof Voucher))
				{
					newOnes.add(d);
				}
			}
			order.setDiscounts(newOnes);
			setAppliedVoucherCodes(order, null);
		}
	}
}
