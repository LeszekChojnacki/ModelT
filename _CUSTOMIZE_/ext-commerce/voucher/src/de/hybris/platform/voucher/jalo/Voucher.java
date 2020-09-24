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
import de.hybris.platform.core.GenericConditionList;
import de.hybris.platform.core.GenericQuery;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.price.Discount;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.util.Base64;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.voucher.constants.VoucherConstants;
import de.hybris.platform.voucher.jalo.util.VoucherEntry;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;
import de.hybris.platform.voucher.jalo.util.VoucherValue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;


/**
 * The vouchers are redeemed on the total value of an order.
 * <p />
 * The calculation of the discount is done on the total value of the applicable product's prices,
 * <p />
 * inclusive of VAT.
 * <p />
 * Non-applicable products in the order are not subject to the discount rules.
 * <p />
 * To discover to which products in the order the voucher is applicable one could assign
 * <p />
 * various restrictions to the voucher.
 *
 */
public abstract class Voucher extends GeneratedVoucher //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(Voucher.class);

	protected static final int CODELENGTH = 12;
	protected static final int LENGTH_CODE = 3;
	protected static final String DIVIDER = "-";
	private static final String ALGORITHM = "AES";
	private static final String KEY = "key";
	private static final String ALPHABET = "alphabet";
	private static final String RANDOM_ALGORITHM = "SHA1PRNG";


	//was 'static', see blocker COM-1237
	//private transient Cipher theCipher; //NOSONAR

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		allAttributes.setAttributeMode(CODE, AttributeMode.INITIAL);

		// by now this item hasn't got its proper jalo class so we have to cast
		// to Discount instead :(
		final Discount voucher = (Discount) super.createItem(ctx, type, allAttributes);

		createAndStoreKey(ctx, voucher);
		//JIRA: COM-1893
		return changeTypeAfterCreation(voucher, type);
	}

	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// remove all invalidations for this voucher
		removeInvalidations(ctx);
		// now remove voucher itself
		super.remove(ctx);
	}

	/**
	 * Upon voucher removal this method deletes all {@link VoucherInvalidation} items which belong to this voucher.
	 *
	 * @param ctx
	 */
	protected void removeInvalidations(final SessionContext ctx)
	{
		for (final VoucherInvalidation vi : getInvalidations(ctx))
		{
			try
			{
				vi.remove(ctx);
			}
			catch (final Exception e)
			{
				LOG.warn("Voucher invalidation failed for: " + vi.getCode(), e);
			}
		}
	}

	protected void createAndStoreKey(final SessionContext ctx, final Discount item)
	{
		try
		{
			final KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
			final SecretKey skey = kgen.generateKey();
			item.setProperty(ctx, KEY, Base64.encodeBytes(skey.getEncoded(), Base64.DONT_BREAK_LINES));

			// Compute a secret alphabet. This actually is another encryption in itself
			// (Caesar Cipher). It is used to change the output and to hide the fact
			// that the voucher number is numberic.
			final String origChars = "123456789ABCDEFGHKLMNPRSTWXYZ";
			String chars = "";
			while (chars.length() < 16)
			{
				final int pos = (int) (SecureRandom.getInstance(RANDOM_ALGORITHM).nextDouble() * origChars.length()); //NOSONAR
				final char nextChar = origChars.charAt(pos);
				if (chars.indexOf(nextChar) == -1)
				{
					chars += nextChar; //NOSONAR
				}
			}
			item.setProperty(ctx, ALPHABET, chars);
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new JaloSystemException(e, "!!", 0);
		}
	}

	public VoucherInvalidation createVoucherInvalidation(final String aVoucherCode, final Order anOrder)
	{
		final Map params = new HashMap();
		params.put(VoucherInvalidation.VOUCHER, this);
		params.put(VoucherInvalidation.CODE, aVoucherCode);
		params.put(VoucherInvalidation.USER, anOrder.getUser());
		params.put(VoucherInvalidation.ORDER, anOrder);
		params.put(VoucherInvalidation.STATUS, VoucherInvalidation.STATUS_CREATED);
		return VoucherManager.getInstance(getSession()).createVoucherInvalidation(params);
	}

	/**
	 * Returns <tt>true</tt> if the specified voucher code is valid for this voucher.
	 *
	 * @param aVoucherCode
	 *           the voucher code to check validity of.
	 * @return <tt>true</tt> if the specified voucher code is valid for this voucher, <tt>false</tt> else.
	 */
	public abstract boolean checkVoucherCode(String aVoucherCode);

	/**
	 * Returns all positions or parts of positions of the specified abstract order that are eligible for this voucher.
	 *
	 * @param anOrder
	 *           the abstract order to get eligible positions of.
	 * @return a <tt>VoucherEntrySet</tt> containing a <tt>VoucherEntry</tt> object for every position that is fully or
	 *         partly eligible for this voucher.
	 */
	public VoucherEntrySet getApplicableEntries(final AbstractOrder anOrder)
	{
		final VoucherEntrySet entries = new VoucherEntrySet(anOrder.getAllEntries());  //NOSONAR
		for (final Iterator iterator = getRestrictions().iterator(); !entries.isEmpty() && iterator.hasNext();)
		{
			final Restriction nextRestriction = (Restriction) iterator.next();
			entries.retainAll(nextRestriction.getApplicableEntries(anOrder));
		}
		return entries;
	}

	/**
	 * Returns a <tt>VoucherValue</tt> object representing the partial value of the total of the specified abstract order
	 * that is eligible for this voucher. Typically this would correspond to the sum of all totals of the applicable
	 * entries.
	 *
	 * @param anOrder
	 *           the abstract order to get applicable value of.
	 * @return a <tt>VoucherValue</tt> representing the partial value of the total of the specified abstract order that
	 *         is eligible for this voucher.
	 */
	protected VoucherValue getApplicableValue(final AbstractOrder anOrder)
	{
		double applicableValue = 0.0;
		for (final Iterator iterator = getApplicableEntries(anOrder).iterator(); iterator.hasNext();)
		{
			final VoucherEntry voucherEntry = (VoucherEntry) iterator.next();
			final AbstractOrderEntry orderEntry = voucherEntry.getOrderEntry();
			final long voucherEntryQuantity = voucherEntry.getQuantity();
			final long orderEntryQuantity = orderEntry.getQuantity().longValue();
			if (voucherEntryQuantity == orderEntryQuantity)
			{
				applicableValue += orderEntry.getTotalPrice().doubleValue();
			}
			else
			{
				// we need to calculate a base price since the only reliable
				// price is the total price !!!
				final double calculatedBasePrice = orderEntry.getTotalPrice().doubleValue() / orderEntryQuantity;
				applicableValue += Math.min(voucherEntryQuantity, orderEntryQuantity) * calculatedBasePrice;
			}
		}
		return new VoucherValue(applicableValue, anOrder.getCurrency());
	}

	/**
	 * Returns a <tt>VoucherValue</tt> object representing the discount value of this voucher. If the voucher is
	 * applicable to the specified abstract order this value is calculated in consideration of the applicable value
	 * returned by <tt>getApplicableValue(AbstractOrder)</tt>.
	 *
	 * @param anOrder
	 *           the abstract order to get discount value of.
	 * @return a <tt>VoucherValue</tt> representing the discount value of this voucher.
	 */
	public VoucherValue getAppliedValue(final AbstractOrder anOrder)
	{
		if (isApplicable(anOrder))
		{
			return getVoucherValue(anOrder);
		}
		else
		{
			return new VoucherValue(0.0, anOrder.getCurrency());
		}
	}

	protected ComposedType getComposedType(final Class aClass)
	{
		try
		{
			final ComposedType type = getSession().getTypeManager().getComposedType(aClass);  //NOSONAR
			if (type == null)
			{
				throw new JaloSystemException(null, "got type null for " + aClass, 0);
			}
			return type;
		}
		catch (final JaloItemNotFoundException e)
		{
			throw new JaloSystemException(e, "required type missing", 0);
		}
	}

	@Override
	public DiscountValue getDiscountValue(final AbstractOrder anOrder)
	{
		final VoucherValue value = getAppliedValue(anOrder);
		return new DiscountValue(getCode(), value.getValue(), true, value.getValue(), value.getCurrencyIsoCode());
	}

	/**
	 * Returns a voucher invalidation object if the specified voucher code was used in the specified order or
	 * <tt>null</tt> else.
	 *
	 * @param aVoucherCode
	 *           the voucher code to check.
	 * @param anOrder
	 *           the order to check.
	 * @return a VoucherInvalidation object representing the usage of a voucher code in an order.
	 */
	protected VoucherInvalidation getInvalidation(final String aVoucherCode, final Order anOrder)
	{
		final GenericQuery query = new GenericQuery(VoucherConstants.TC.VOUCHERINVALIDATION,
				GenericConditionList.createConditionList(GenericCondition.equals(VoucherInvalidation.CODE, aVoucherCode),
						GenericCondition.equals(VoucherInvalidation.ORDER, anOrder)));
		final List<VoucherInvalidation> invalidations = getSession().search(query, getSession().createSearchContext()).getResult();
		return invalidations.isEmpty() ? null : invalidations.get(0);
	}

	/**
	 * Returns all voucher invalidations representing the usage of the specified voucher code. If voucher code represents
	 * a serial voucher the returned collection should not contain more than one item since serial voucher codes can only
	 * be used once.
	 *
	 * @param aVoucherCode
	 *           the voucher code to check
	 * @return a <tt>Collection</tt> containing none, one or more <tt>VoucherInvalidation</tt> objects.
	 */
	protected Collection<VoucherInvalidation> getInvalidations(final String aVoucherCode)
	{
		return getInvalidationsInternal(GenericCondition.equals(VoucherInvalidation.CODE, aVoucherCode));
	}

	/**
	 * Returns all voucher invalidations representing the usage of the specified voucher code by the specified user. If
	 * voucher code represents a serial voucher the returned collection should not contain more than one item since
	 * serial voucher codes can only be used once.
	 *
	 * @param aVoucherCode
	 *           the voucher code to check
	 * @param anUser
	 *           the user to check
	 * @return a <tt>Collection</tt> containing none, one or more <tt>VoucherInvalidation</tt> objects.
	 */
	protected Collection<VoucherInvalidation> getInvalidations(final String aVoucherCode, final User anUser)
	{
		return getInvalidationsInternal(
				GenericCondition.createConditionList(GenericCondition.equals(VoucherInvalidation.USER, anUser),
						GenericCondition.equals(VoucherInvalidation.CODE, aVoucherCode)));
	}

	private Collection<VoucherInvalidation> getInvalidationsInternal(final GenericCondition condition)
	{
		return getSession()
				.search(new GenericQuery(VoucherConstants.TC.VOUCHERINVALIDATION, condition), getSession().createSearchContext())
				.getResult();
	}

	/**
	 * Convenience method. Returns a string representation of the value of this voucher, e.g. 5$ or 10%.
	 *
	 * @param ctx
	 *           the context of the actual session
	 * @return a <tt>String</tt> representing the alue of this voucher.
	 */
	@Override
	public String getValueString(final SessionContext ctx)
	{
		return getValue()
				+ (isAbsolute().booleanValue() ? " " + (getCurrency() != null ? getCurrency().getIsoCode(ctx) : "") : "%");  //NOSONAR
	}

	/**
	 * Returns all restrictions that are not fulfilled by the specified abstract order.
	 *
	 * @param anOrder
	 *           the abstract order to return violated restrictions for.
	 * @return a <tt>List</tt> object containing all <tt>Restriction</tt> objects associated with this voucher that the
	 *         specified abstract order does not fulfill.
	 */
	public List<Restriction> getViolatedRestrictions(final AbstractOrder anOrder)
	{
		final List<Restriction> restrictions = new ArrayList();
		for (final Restriction nextRestriction : getRestrictions())
		{
			if (!nextRestriction.isFulfilled(anOrder))
			{
				restrictions.add(nextRestriction);
			}
		}
		return restrictions.isEmpty() ? Collections.emptyList() : restrictions;
	}

	/**
	 * Returns all restrictions that are not fulfilled by the specified product.
	 *
	 * @param aProduct
	 *           the product to return violated restrictions for.
	 * @return a <tt>List</tt> object containing all <tt>Restriction</tt> objects associated with this voucher that the
	 *         specified product does not fulfill.
	 */
	public List<Restriction> getViolatedRestrictions(final Product aProduct)  //NOSONAR
	{
		final List<Restriction> restrictions = new ArrayList();
		for (final Restriction nextRestriction : getRestrictions())
		{
			if (!nextRestriction.isFulfilled(aProduct))
			{
				restrictions.add(nextRestriction);
			}
		}
		return restrictions.isEmpty() ? Collections.emptyList() : restrictions;
	}

	public List<String> getViolationMessages(final AbstractOrder anOrder)
	{
		return getViolationMessages(getViolatedRestrictions(anOrder));
	}

	public List<String> getViolationMessages(final Product aProduct)  //NOSONAR
	{
		return getViolationMessages(getViolatedRestrictions(aProduct));
	}
	@SuppressWarnings("squid:S2325")
	private List<String> getViolationMessages(final Collection<Restriction> restrictions)
	{
		final List<String> messages = new ArrayList();
		for (final Restriction nextRestriction : restrictions)
		{
			messages.add(nextRestriction.getViolationMessage());
		}
		return messages.isEmpty() ? Collections.emptyList() : messages;
	}

	public VoucherValue getVoucherValue(final AbstractOrder anOrder) //NOSONAR
	{
		//COM-7
		final Iterator<Discount> discounts = anOrder.getDiscounts().iterator();
		boolean found = false;
		boolean applyFreeShipping = false;
		while (discounts.hasNext() && !found)
		{
			final Discount discount = discounts.next();
			if (discount instanceof Voucher)
			{
				final Voucher voucher = (Voucher) discount;
				if (voucher.isApplicable(anOrder) && voucher.isFreeShippingAsPrimitive())
				{
					if (voucher.equals(this)) //NOSONAR
					{
						applyFreeShipping = true;
					}
					found = true;
				}
			}
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Processed voucher: [" + this.getCode() + "] " + this.getName());
			LOG.debug("Free shipping is: " + this.isFreeShippingAsPrimitive());
			LOG.debug("Free shipping will apply: " + applyFreeShipping);
		}

		final VoucherValue applicableValue = getApplicableValue(anOrder);
		double resultValue;
		Currency resultCurrency;
		if (isAbsolute().booleanValue())
		{
			resultValue = Math.min(applicableValue.getCurrency().convertAndRound(getCurrency(), applicableValue.getValue()),  //NOSONAR
					getValue().doubleValue());
			resultCurrency = getCurrency();
		}
		else
		{
			resultValue = applicableValue.getValue() * getValue().doubleValue() / 100;
			resultCurrency = applicableValue.getCurrency();
		}
		if (isFreeShippingAsPrimitive() && applyFreeShipping)
		{
			resultValue += anOrder.getCurrency().convertAndRound(resultCurrency, anOrder.getDeliveryCosts());  //NOSONAR
		}
		return new VoucherValue(resultValue, resultCurrency);
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order is eligible for this voucher. More formally, returns
	 * <tt>true</tt> if the specified abstract order fulfills all restrictions associated with this voucher.
	 *
	 * @param anOrder
	 *           the abstract order to check whether it is eligible for this voucher.
	 * @return <tt>true</tt> if the specified abstract order is eligible for this voucher, <tt>false</tt> else.
	 */
	public boolean isApplicable(final AbstractOrder anOrder)
	{
		for (final Iterator iterator = getRestrictions().iterator(); iterator.hasNext();)
		{
			final Restriction nextRestriction = (Restriction) iterator.next();
			if (!nextRestriction.isFulfilled(anOrder))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <tt>true</tt> if the specified product is eligible for this voucher. More formally, returns <tt>true</tt>
	 * if the specified product fulfills all restrictions associated with this voucher.
	 *
	 * @param aProduct
	 *           the product to check whether it is eligible for this voucher.
	 * @return <tt>true</tt> if the specified product is eligible for this voucher, <tt>false</tt> else.
	 */
	public boolean isApplicable(final Product aProduct)  //NOSONAR
	{
		for (final Iterator iterator = getRestrictions().iterator(); iterator.hasNext();)
		{
			final Restriction nextRestriction = (Restriction) iterator.next();
			if (!nextRestriction.isFulfilled(aProduct))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isApplied(final String aVoucherCode, final AbstractOrder abstractOrder)
	{
		boolean ret = false;
		final VoucherManager vm = VoucherManager.getInstance();
		final Voucher v = vm.getVoucher(aVoucherCode);
		if (v != null && abstractOrder.getDiscounts().contains(v))
		{
			if (abstractOrder instanceof Cart)
			{
				ret = vm.getAppliedVoucherCodes((Cart) abstractOrder).contains(aVoucherCode);
			}
			else
			{
				final VoucherInvalidation inv = getInvalidation(aVoucherCode, (Order) abstractOrder);
				ret = inv != null && VoucherInvalidation.STATUS_CONFIRMED.equals(inv.getStatus());
			}
		}

		return ret;
	}

	public abstract boolean isReservable(String aVoucherCode, User user);

	public boolean isReservable(final String aVoucherCode, final AbstractOrder abstractOrder)
	{
		return isReservable(aVoucherCode, abstractOrder.getUser()) && !isApplied(aVoucherCode, abstractOrder);
	}

	public boolean redeem(final String aVoucherCode, final Cart aCart) throws JaloPriceFactoryException
	{
		if (checkVoucherCode(aVoucherCode) && isReservable(aVoucherCode, aCart))
		{
			aCart.addDiscount(this);  //NOSONAR
			aCart.recalculate();  //NOSONAR
			final Collection<String> appliedVoucherCodes = new LinkedHashSet<String>(
					VoucherManager.getInstance().getAppliedVoucherCodes(aCart));
			appliedVoucherCodes.add(aVoucherCode);
			VoucherManager.getInstance().setAppliedVoucherCodes(aCart, appliedVoucherCodes);
			return true;
		}
		return false;
	}

	public VoucherInvalidation redeem(final String aVoucherCode, final Order anOrder)
	{
		if (!checkVoucherCode(aVoucherCode))
		{
			return null;
		}
		VoucherInvalidation invalidation = getInvalidation(aVoucherCode, anOrder);
		if (invalidation == null)
		{
			invalidation = reserve(aVoucherCode, anOrder);
		}
		if (invalidation == null || VoucherInvalidation.STATUS_CONFIRMED.equals(invalidation.getStatus()))
		{
			return null;
		}
		invalidation.setStatus(VoucherInvalidation.STATUS_CONFIRMED);
		return invalidation;
	}

	public void release(final String aVoucherCode, final Order anOrder) throws ConsistencyCheckException
	{
		if (checkVoucherCode(aVoucherCode))
		{
			anOrder.removeDiscount(this);  //NOSONAR
			final VoucherInvalidation invalidation = getInvalidation(aVoucherCode, anOrder);
			if (invalidation != null)
			{
				invalidation.remove();
			}
		}
	}

	public void release(final String aVoucherCode, final Cart aCart) throws JaloPriceFactoryException
	{
		if (checkVoucherCode(aVoucherCode))
		{
			aCart.removeDiscount(this);  //NOSONAR
			aCart.recalculate();  //NOSONAR
			final Collection<String> appliedVoucherCodes = new LinkedHashSet<String>(
					VoucherManager.getInstance().getAppliedVoucherCodes(aCart));
			appliedVoucherCodes.remove(aVoucherCode);
			VoucherManager.getInstance().setAppliedVoucherCodes(aCart, appliedVoucherCodes);
		}
	}

	public VoucherInvalidation reserve(final String aVoucherCode, final Order anOrder)
	{
		if (checkVoucherCode(aVoucherCode) && isReservable(aVoucherCode, anOrder))
		{
			anOrder.addDiscount(this);  //NOSONAR
			return createVoucherInvalidation(aVoucherCode, anOrder);
		}
		return null;
	}

	// ********************************************************************************
	// Voucher code generation and handling
	// ********************************************************************************
	protected static String insertDividers(final String voucherCode)
	{
		final StringBuilder result = new StringBuilder(voucherCode);
		for (int index = LENGTH_CODE; index < result.length(); index += 4 + DIVIDER.length())
		{
			result.insert(index, DIVIDER);
		}
		return result.toString();
	}

	protected static String removeDividers(final String voucherCode)
	{
		final StringBuilder result = new StringBuilder();
		for (final StringTokenizer tokenizer = new StringTokenizer(voucherCode, DIVIDER); tokenizer.hasMoreTokens();)
		{
			result.append(tokenizer.nextToken());
		}
		return result.toString();
	}

	/*
	 * (this is not to become a public API comment) The voucher code generation works as follows: - build a six character
	 * voucher number out of the given voucherNumber. This works by using the lower three byte of the voucherNumber,
	 * generating two characters for each byte using the secret alphabet and rotating this alphabet for each byte and
	 * again for each half-byte. This is to prevent the voucher number from looking too numeric. Without rotating a
	 * problem could arise if for instance only 1000 codes would be created for a voucher. They would all start with
	 * 000000. Using the rotation they look like 048BAE. - Concatenate the action code and the generated voucher number
	 * and encrypt it. - use the first 9 bytes of the encrypted text to generate (XOR) 3 bytes of signature. Represent
	 * those 3 bytes as a 6 character signature using the same rotation as above. - Concatenate the action code, the
	 * voucher number and the signature to form a 15 character voucher code.
	 */
	public String generateVoucherCode() throws NoSuchAlgorithmException
	{
		final int voucherNumber = getNextVoucherNumber(getSession().getSessionContext());
		if (voucherNumber < 0 || voucherNumber >= 1 << 24)
		{
			throw new IllegalArgumentException("Given voucherNumber is not in accepted range!");
		}
		final String clearText = getCode() + threeByteHex(voucherNumber);
		final String sig = threeByteSig(clearText);
		return insertDividers(clearText.concat(sig));
	}

	private String getAlphabet(final SessionContext ctx)
	{
		return (String) getProperty(ctx, ALPHABET);
	}

	private synchronized Cipher getCipher() throws NoSuchAlgorithmException
	{
		// blocker COM-1237

		Cipher cipher = null;
		try
		{
			final SecretKeySpec skeySpec = new SecretKeySpec(getKey(getSession().getSessionContext()), ALGORITHM);
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		}
		catch (final NoSuchPaddingException | InvalidKeyException e)
		{
			throw new RuntimeException(e); //NOPMD //NOSONAR
		}

		return cipher;
	}

	private int getIntFromHexByte(final String value, final int offset) throws InvalidVoucherKeyException
	{
		return (getIntFromHexNibble(value.charAt(0), offset) << 4) + getIntFromHexNibble(value.charAt(1), offset + 4);
	}

	private int getIntFromHexNibble(final char value, final int offset) throws InvalidVoucherKeyException
	{
		int pos = getAlphabet(getSession().getSessionContext()).indexOf(value);
		if (pos == -1)
		{
			throw new InvalidVoucherKeyException();
		}
		else
		{
			pos -= offset;
			while (pos < 0)
			{
				pos += 16;
			}
			return pos % 16;
		}
	}

	private byte[] getKey(final SessionContext ctx)
	{
		return Base64.decode((String) getProperty(ctx, KEY));
	}

	protected abstract int getNextVoucherNumber(SessionContext ctx);

	protected int getVoucherNumber(final String voucherCode) throws InvalidVoucherKeyException
	{
		final int voucherNumberPos = voucherCode.length() - CODELENGTH;
		final String voucherNumberHex = voucherCode.substring(voucherNumberPos, voucherNumberPos + CODELENGTH / 2);
		final int firstByte = getIntFromHexByte(voucherNumberHex.substring(0, 2), 0);
		final int secondByte = getIntFromHexByte(voucherNumberHex.substring(2, 4), 1);
		final int thirdByte = getIntFromHexByte(voucherNumberHex.substring(4, 6), 7);
		return (firstByte << 16) | (secondByte << 8) | thirdByte;
	}

	private String nibbleHex(final int value, final int offset)
	{
		final String chars = getAlphabet(getSession().getSessionContext());
		final int pos = (value + offset) % 16;
		return chars.substring(pos, pos + 1);
	}

	/**
	 * Returns a two character representation of the last byte of the given value. The offset is used to rotate the
	 * alphabet.
	 */
	private String oneByteHex(final int value, final int offset)
	{
		return nibbleHex(value >> 4, offset).concat(nibbleHex(value & 15, offset + 4));
	}

	/**
	 * This method returns a six character representation of the lower three bytes of the given value.
	 */
	private String threeByteHex(final int number)
	{
		return oneByteHex((number >> 16) & 255, 0).concat(oneByteHex((number >> 8) & 255, 1)).concat(oneByteHex((number) & 255, 7));
	}

	/*
	 * This method takes the sigText, encrypts it and computes a three-byte-signature of it. The result is a six
	 * character string (2 characters per byte).
	 */
	protected String threeByteSig(final String sigText) throws NoSuchAlgorithmException
	{
		try
		{
			final String sigClearText = sigText.length() < 9 ? sigText.concat(sigText) : sigText;
			final byte[] sigData = getCipher().doFinal(sigClearText.getBytes());
			final int sigByte0 = ((sigData[0] & 255) ^ (sigData[3] & 255) ^ (sigData[8] & 255)); //NOSONAR
			final int sigByte1 = ((sigData[1] & 255) ^ (sigData[4] & 255) ^ (sigData[6] & 255)); //NOSONAR
			final int sigByte2 = ((sigData[2] & 255) ^ (sigData[5] & 255) ^ (sigData[7] & 255)); //NOSONAR
			return threeByteHex((sigByte0 << 16) | (sigByte1 << 8) | sigByte2);
		}
		catch (final BadPaddingException | IllegalBlockSizeException e)
		{
			throw new RuntimeException(e); //NOPMD //NOSONAR
		}
	}

	@SuppressWarnings("serial")
	protected static class InvalidVoucherKeyException extends Exception
	{
		// DOCTODO Document reason, why this block is empty
	}
}
