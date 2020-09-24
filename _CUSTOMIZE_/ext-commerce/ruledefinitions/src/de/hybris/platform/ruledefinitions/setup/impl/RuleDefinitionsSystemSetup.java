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
package de.hybris.platform.ruledefinitions.setup.impl;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruledefinitions.constants.RuledefinitionsConstants;
import de.hybris.platform.ruleengine.setup.AbstractRuleEngineSystemSetup;


@SystemSetup(extension = RuledefinitionsConstants.EXTENSIONNAME)
public class RuleDefinitionsSystemSetup extends AbstractRuleEngineSystemSetup
{

	@SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.ALL)
	public void createEssentialData(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		importImpexFile("/ruledefinitions/import/essentialdata-definitions.impex", true, false);
	}

}
