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
package de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.deleteoverridereference;

import de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor.deletereference.DeleteReferenceAction;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;

import com.hybris.cockpitng.actions.ActionContext;


public class DeleteOverrideReferenceAction extends DeleteReferenceAction
{
	@Override
	public boolean canPerform(final ActionContext<AbstractEditorData> ctx)
	{
		final AbstractEditorData data = ctx.getData();
		if (data == null)
		{
			return false;
		}

		return data.isFromSearchConfiguration() && data.isOverride();
	}
}
