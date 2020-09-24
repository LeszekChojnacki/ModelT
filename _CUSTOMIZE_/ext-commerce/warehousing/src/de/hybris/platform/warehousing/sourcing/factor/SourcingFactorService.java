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
package de.hybris.platform.warehousing.sourcing.factor;

import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingFactor;
import de.hybris.platform.warehousing.data.sourcing.SourcingFactorIdentifiersEnum;

import java.util.Set;


/**
 * Service used to build/retrieve defined list of sourcing factors <br/>
 * A factor must have a weight associated. <br/>
 * <br/>
 * Example: <br/>
 * <br/>
 * <table border="1">
 * <col width="50%"/> <col width="50%"/> <thead>
 * <tr>
 * <th>Sourcing Factor</th>
 * <th>Weight</th>
 * </tr>
 * <thead> <tbody>
 * <tr>
 * <td align="center">DISTANCE</td>
 * <td align="center">50</td>
 * </tr>
 * <tr>
 * <td align="center">ALLOCATION</td>
 * <td align="center">30</td>
 * </tr>
 * <tr>
 * <td align="center">PRIORITY</td>
 * <td align="center">20</td>
 * </tr>
 * </tbody>
 * </table>
 */
public interface SourcingFactorService
{
	/**
	 * Create or retrieve the requested sourcing factor according to the sourcingFactorId and the baseStore given in parameter.
	 *
	 * @param sourcingFactorId
	 * 		the identifier of the sourcing factor {@link SourcingFactorIdentifiersEnum}
	 * @param baseStore
	 * 		the baseStore
	 * @return the {@link SourcingFactor}
	 */
	SourcingFactor getSourcingFactor(SourcingFactorIdentifiersEnum sourcingFactorId, BaseStoreModel baseStore);

	/**
	 * Get the list of all the sourcing factors for the given {@link BaseStoreModel}.
	 *
	 * @param baseStore
	 * 		the baseStore
	 * @return the list of {@link SourcingFactor}
	 */
	Set<SourcingFactor> getAllSourcingFactorsForBaseStore(BaseStoreModel baseStore);
}
