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

import de.hybris.bootstrap.annotations.UnitTest;

import com.hybris.cockpitng.testing.AbstractCockpitEditorRendererUnitTest;
import com.hybris.cockpitng.testing.annotation.ExtensibleWidget;


/**
 * Unit test for {@link de.hybris.platform.voucher.backoffice.cockpitng.editor.insets.VoucherCodeGeneratorInset}
 */
@UnitTest
@ExtensibleWidget(level = ExtensibleWidget.ALL)
public class VoucherCodeGeneratorInsetTest extends AbstractCockpitEditorRendererUnitTest<String, VoucherCodeGeneratorInset>
{
	private final VoucherCodeGeneratorInset inset = new VoucherCodeGeneratorInset();

	@Override
	public VoucherCodeGeneratorInset getEditorInstance()
	{
		return inset;
	}
}
