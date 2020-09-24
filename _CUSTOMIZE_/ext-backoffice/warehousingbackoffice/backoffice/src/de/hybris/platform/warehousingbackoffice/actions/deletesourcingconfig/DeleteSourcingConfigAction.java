/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */

package de.hybris.platform.warehousingbackoffice.actions.deletesourcingconfig;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.delete.DeleteAction;
import de.hybris.platform.warehousing.model.SourcingConfigModel;
import org.apache.commons.collections4.CollectionUtils;


/**
 * Delete Action for AtpFormula, which restricts deletion of only those formula that are not assigned to any BaseStore
 */
public class DeleteSourcingConfigAction extends DeleteAction
{
	@Override
	public boolean canPerform(final ActionContext<Object> ctx)
	{
		boolean allowed = false;

		if (ctx.getData() != null
				&& ctx.getData() instanceof SourcingConfigModel
				&& CollectionUtils.isEmpty(((SourcingConfigModel) ctx.getData()).getBaseStores()))
		{
			allowed = true;
		}
		return allowed;
	}

}
