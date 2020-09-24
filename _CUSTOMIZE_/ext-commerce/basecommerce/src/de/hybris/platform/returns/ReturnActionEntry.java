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
package de.hybris.platform.returns;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.returns.model.ReturnEntryModel;


/**
 * Represents an action entry in a ReturnRequest Action Request or ReturnRequest Action Response. A single action entry
 * refers to a ReturnEntry and has an actionQuantity value. actionQuantity value must be greater than zero and less than
 * or equal to ReturnEntry.getExpectedQuantity(). if actionQuantity value is equal to the ReturnEntry quantity, the
 * ReturnEntry is subject to complete action operation, , otherwise it is subject to partial action.
 *
 * In case of ReturnRequest Action Request the actionQuantity value means how many items should be taking care of from
 * the actual ReturnEntry. In case of ReturnRequest Action Response the actionQuantity value means how many items were
 * successfully taking care of from the actual ReturnEntry.
 */
public class ReturnActionEntry
{
	private final ReturnEntryModel returnEntry;
	private final long actionQuantity;
	private String notes;
	private HybrisEnumValue actionReason;

	/**
	 * Creates an entry that represents action of the whole Return Entry
	 *
	 * @param returnEntry
	 */
	public ReturnActionEntry(final ReturnEntryModel returnEntry)
	{
		this.returnEntry = returnEntry;
		this.actionQuantity = returnEntry.getExpectedQuantity().longValue();
	}

	/**
	 * Creates an entry that represents action of a part of the Return Entry (i.e. reducing Return Entry quantity).
	 * Reducing Return Entry quantity to zero is the same as cancelling it completely.
	 *
	 * @param returnEntry
	 *           - return entry
	 * @param actionQuantity
	 *           - how much of the entry's quantity should be taking care of
	 */
	public ReturnActionEntry(final ReturnEntryModel returnEntry, final long actionQuantity)
	{
		this(returnEntry, actionQuantity, null);
	}

	/**
	 * Creates an entry that represents action of a part of the Return Entry (i.e. reducing Return Entry quantity).
	 * Reducing Return Entry quantity to zero is the same as canceling it completely.
	 *
	 * @param returnEntry
	 *           - return entry
	 * @param actionQuantity
	 *           - how much of the entry's quantity should be taking care of
	 * @param notes
	 *           - additional notes (I.E from CSAdmin)
	 */
	public ReturnActionEntry(final ReturnEntryModel returnEntry, final long actionQuantity, final String notes)
	{
		this(returnEntry, actionQuantity, notes, CancelReason.NA);
	}

	/**
	 * Creates an entry that represents action of a part of the Return Entry completely.
	 *
	 * @param returnEntry
	 *           - return entry
	 * @param actionReason
	 *           - reason of this return entry action
	 * @param notes
	 *           - additional notes (I.E from CSAdmin)
	 */
	public ReturnActionEntry(final ReturnEntryModel returnEntry, final String notes, final HybrisEnumValue actionReason)
	{
		this(returnEntry, returnEntry.getExpectedQuantity().longValue(), notes, actionReason);
	}

	/**
	 * Creates an entry that represents action of a part of the Return Entry partially.
	 *
	 * @param returnEntry
	 *           - return entry
	 * @param actionQuantity
	 *           - how much of the entry's quantity should be taking care of
	 * @param actionReason
	 *           - reason of this return entry action
	 * @param notes
	 *           - additional notes (I.E from CSAdmin)
	 */
	public ReturnActionEntry(final ReturnEntryModel returnEntry, final long actionQuantity, final String notes,
			final HybrisEnumValue actionReason)
	{
		this.returnEntry = returnEntry;
		if (actionQuantity < 0)
		{
			throw new IllegalArgumentException("ReturnActionEntry's actionQuantity value must be greater than zero");
		}
		if (actionQuantity > returnEntry.getExpectedQuantity().longValue())
		{
			throw new IllegalArgumentException(
					"ReturnActionEntry's actionQuantity value cannot be greater than actual ReturnEntry expected quantity");
		}
		this.actionQuantity = actionQuantity;
		this.notes = notes;
		this.actionReason = actionReason;
	}

	public ReturnEntryModel getReturnEntry()
	{
		return returnEntry;
	}

	public long getActionQuantity()
	{
		return actionQuantity;
	}

	public String getNotes()
	{
		return notes;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}

	public HybrisEnumValue getActionReason()
	{
		return actionReason;
	}

	public void setActionReason(HybrisEnumValue actionReason)
	{
		this.actionReason = actionReason;
	}

}
