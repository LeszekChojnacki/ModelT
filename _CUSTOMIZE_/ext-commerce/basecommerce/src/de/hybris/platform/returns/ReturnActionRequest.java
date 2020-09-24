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

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.PK;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Represents Return action requests. Instances of this class can represent:
 * <ul>
 * <li>Requests for approving/cancelling/receiving whole return request (all return entries of a ReturnRequest are
 * approved/cancelled/received)</li>
 * <li>Requests for approving/cancelling/receiving only some of the return entries of a ReturnRequest</li> A
 * ReturnRequest entry may be approved/cancelled/received completely (return entry is approved/cancelled/received) or
 * partially (i.e. only return entry quantity is reduced).
 *
 * It is important to note that the requests represented by this class may be processed completely, declined or
 * processed only partially by the ReturnService.
 * </ul>
 */
public class ReturnActionRequest
{
	private final ReturnRequestModel returnRequest;
	private final List<ReturnActionEntry> entriesToTakeAction;
	private final boolean partialAction;
	private final boolean partialEntryAction;
	private String requestToken;
	private String notes;
	private HybrisEnumValue actionReason;

	/**
	 * Creates {@link ReturnActionRequest} for ReturnRequest action Complete
	 *
	 * @param returnRequest
	 *           a ReturnRequest that should be completely taking care of
	 */
	public ReturnActionRequest(final ReturnRequestModel returnRequest)
	{
		this(returnRequest, CancelReason.NA);
	}

	/**
	 * Creates {@link ReturnActionRequest} for ReturnRequest action Complete
	 *
	 * @param actionReason
	 *           reason for complete action
	 * @param returnRequest
	 *           a ReturnRequest that should be completely taking care of
	 */
	public ReturnActionRequest(final ReturnRequestModel returnRequest, final HybrisEnumValue actionReason)
	{
		this(returnRequest, actionReason, null);
	}

	/**
	 * Creates {@link ReturnActionRequest} for ReturnRequest action Complete
	 *
	 * @param actionReason
	 *           reason for complete action
	 * @param returnRequest
	 *           a ReturnRequest that should be completely taking care of
	 * @param notes
	 *           - additional notes (i.e. from CSAdmin)
	 */
	public ReturnActionRequest(final ReturnRequestModel returnRequest, final HybrisEnumValue actionReason, final String notes)
	{
		this.returnRequest = returnRequest;
		final List<ReturnActionEntry> tmpList = new ArrayList<>();
		for (final ReturnEntryModel rem : returnRequest.getReturnEntries())
		{
			tmpList.add(new ReturnActionEntry(rem, rem.getExpectedQuantity().longValue()));
		}
		Collections.sort(tmpList, (final ReturnActionEntry return1, final ReturnActionEntry return2) -> return1.getReturnEntry()
				.getPk().compareTo(return2.getReturnEntry().getPk()));
		this.entriesToTakeAction = Collections.unmodifiableList(tmpList);
		this.partialAction = false;
		this.partialEntryAction = false;
		this.actionReason = actionReason;
		this.notes = notes;
	}

	/**
	 * Creates ReturnActionRequest for ReturnRequest action Partial.
	 *
	 * @param returnRequest
	 *           a ReturnRquest that should be partially taking care of
	 * @param returnActionEntries
	 *           specifies how should return entries be taking care of. Each ReturnActionEntry's Quantity value specifies
	 *           how many items should be taking care of from the corresponding ReturnEntry. If Quantity value equals the
	 *           ReturnEntry.getQuantity() value, the whole ReturnEntry is be taking care of.
	 */
	public ReturnActionRequest(final ReturnRequestModel returnRequest, final List<ReturnActionEntry> returnActionEntries)
	{
		this(returnRequest, returnActionEntries, null);
	}

	/**
	 * Creates ReturnActionRequest for ReturnRequest action Partial.
	 *
	 * @param returnRequest
	 *           a ReturnRquest that should be partially taking care of
	 * @param returnActionEntries
	 *           specifies how should return entries be taking care of. Each ReturnActionEntry's Quantity value specifies
	 *           how many items should be taking care of from the corresponding ReturnEntry. If Quantity value equals the
	 *           ReturnEntry.getQuantity() value, the whole ReturnEntry is be taking care of.
	 * @param notes
	 *           - additional notes from the CSAdmin on the whole order cancellation
	 */
	public ReturnActionRequest(final ReturnRequestModel returnRequest, final List<ReturnActionEntry> returnActionEntries,
			final String notes)
	{
		checkArgument(isNotEmpty(returnActionEntries), "returnActionEntries is null or empty");

		this.returnRequest = returnRequest;
		this.notes = notes;

		//This holds pairs: entryNumber(OrderEntry), OrderCancelEntry(OrderEntry)
		final Map<PK, ReturnActionEntry> actionEntriesMap = new HashMap<>();

		boolean partialEntryActionDetected = false;
		for (final ReturnActionEntry rae : returnActionEntries)
		{
			if (!returnRequest.equals(rae.getReturnEntry().getReturnRequest()))
			{
				throw new IllegalArgumentException("Attempt to add Return Entry that belongs to another return");
			}

			if (actionEntriesMap.containsKey(rae.getReturnEntry().getPk()))
			{
				throw new IllegalArgumentException("Attempt to add Return Entry twice");
			}
			else
			{
				actionEntriesMap.put(rae.getReturnEntry().getPk(), rae);
			}

			if (rae.getActionQuantity() < rae.getReturnEntry().getExpectedQuantity().longValue())
			{
				partialEntryActionDetected = true;
			}
		}

		final List<ReturnActionEntry> tmpList = new ArrayList<>(actionEntriesMap.values());
		Collections.sort(tmpList, (final ReturnActionEntry rae1, final ReturnActionEntry rae2) -> rae1.getReturnEntry().getPk()
				.compareTo(rae2.getReturnEntry().getPk()));

		this.entriesToTakeAction = Collections.unmodifiableList(tmpList);

		this.partialEntryAction = partialEntryActionDetected;

		//Set value for "partialCancel" flag
		if (partialEntryActionDetected)
		{
			this.partialAction = true;
		}
		else
		{
			//Detect if this is partial or complete action.
			//It is complete action, when all ReturnEntries of a ReturnRequest have corresponding ReturnActionEntry.
			boolean allReturnEntriesDone = true;
			for (final ReturnEntryModel rem : returnRequest.getReturnEntries())
			{
				if (!actionEntriesMap.containsKey(rem.getPk()))
				{
					allReturnEntriesDone = false;
				}
			}
			this.partialAction = !allReturnEntriesDone;
		}
	}

	public String getRequestToken()
	{
		return requestToken;
	}

	public void setRequestToken(final String requestToken)
	{
		this.requestToken = requestToken;
	}

	public HybrisEnumValue getActionReason()
	{
		return actionReason;
	}

	public void setActionReason(final HybrisEnumValue actionReason)
	{
		this.actionReason = actionReason;
	}

	public ReturnRequestModel getReturnRequest()
	{
		return returnRequest;
	}

	public List<ReturnActionEntry> getEntriesToTakeAction()
	{
		return entriesToTakeAction;
	}

	public String getNotes()
	{
		return notes;
	}

	public void setNotes(final String notes)
	{
		this.notes = notes;
	}

	public boolean isPartialAction()
	{
		return partialAction;
	}

	public boolean isPartialEntryAction()
	{
		return partialEntryAction;
	}
}
