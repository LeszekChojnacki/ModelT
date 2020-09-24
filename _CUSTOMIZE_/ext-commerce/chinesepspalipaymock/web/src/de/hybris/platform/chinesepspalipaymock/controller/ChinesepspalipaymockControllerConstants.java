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
package de.hybris.platform.chinesepspalipaymock.controller;

import java.util.HashMap;
import java.util.Map;


/**
 * Chinesepspalipaymock constants
 */
public interface ChinesepspalipaymockControllerConstants
{
	interface Page
	{
		String AlipayMockPage = "/responsive/pages/alipay/mock/mockWeb";
	}

	class Errors
	{
		
		Map refundErrors = new HashMap() //NOSONAR
		{
			{ //NOSONAR
				put("ILLEGAL_SIGN", "ILLEGAL_SIGN");
				put("ILLEGAL_DYN_MD5_KEY", "ILLEGAL_DYN_MD5_KEY");
				put("ILLEGAL_ENCRYPT", "ILLEGAL_ENCRYPT");
				put("ILLEGAL_ARGUMENT", "ILLEGAL_ARGUMENT");
				put("ILLEGAL_SERVICE", "ILLEGAL_SERVICE");
				put("ILLEGAL_USER", "ILLEGAL_USER");
				put("ILLEGAL_PARTNER", "ILLEGAL_PARTNER");
				put("ILLEGAL_EXTERFACE", "ILLEGAL_EXTERFACE");
				put("LLEGAL_PARTNER_EXTERFACE", "LLEGAL_PARTNER_EXTERFACE");
				put("ILLEGAL_SECURITY_PROFILE", "ILLEGAL_SECURITY_PROFILE");
				put("ILLEGAL_AGENT", "ILLEGAL_AGENT");
				put("ILLEGAL_SIGN_TYPE", "ILLEGAL_SIGN_TYPE");
				put("ILLEGAL_CHARSET", "ILLEGAL_CHARSET");
				put("ILLEGAL_CLIENT_IP", "ILLEGAL_CLIENT_IP");
				put("HAS_NO_PRIVILEGE", "HAS_NO_PRIVILEGE");
				put("ILLEGAL_DIGEST_TYPE", "ILLEGAL_DIGEST_TYPE");
				put("ILLEGAL_DIGEST", "ILLEGAL_DIGEST");
				put("ILLEGAL_FILE_FORMAT", "ILLEGAL_FILE_FORMAT");
				put("ILLEGAL_ENCODING", "ILLEGAL_ENCODING");
				put("ILLEGAL_REQUEST_REFERER", "ILLEGAL_REQUEST_REFERER");
				put("ILLEGAL_ANTI_PHISHING_KEY", "ILLEGAL_ANTI_PHISHING_KEY");
				put("ANTI_PHISHING_KEY_TIMEOUT", "ANTI_PHISHING_KEY_TIMEOUT");
				put("ILLEGAL_EXTER_INVOKE_IP", "ILLEGAL_EXTER_INVOKE_IP");
				put("BATCH_NUM_EXCEED_LIMIT", "BATCH_NUM_EXCEED_LIMIT");
				put("REFUND_DATE_ERROR", "REFUND_DATE_ERROR");
				put("BATCH_NUM_ERROR", "BATCH_NUM_ERROR");
				put("DUBL_ROYALTY_IN_DETAIL", "DUBL_ROYALTY_IN_DETAIL");
				put("BATCH_NUM_NOT_EQUAL_TOTAL", "BATCH_NUM_NOT_EQUAL_TOTAL");
				put("SINGLE_DETAIL_DATA_EXCEED_LIMIT", "SINGLE_DETAIL_DATA_EXCEED_LIMIT");
				put("DUBL_TRADE_NO_IN_SAME_BATCH", "DUBL_TRADE_NO_IN_SAME_BATCH");
				put("DUPLICATE_BATCH_NO", "DUPLICATE_BATCH_NO");
				put("TRADE_STATUS_ERROR", "TRADE_STATUS_ERROR");
				put("BATCH_NO_FORMAT_ERROR", "BATCH_NO_FORMAT_ERROR");
				put("PARTNER_NOT_SIGN_PROTOCOL", "PARTNER_NOT_SIGN_PROTOCOL");
				put("NOT_THIS_PARTNERS_TRADE", "NOT_THIS_PARTNERS_TRADE");
				put("DETAIL_DATA_FORMAT_ERROR", "DETAIL_DATA_FORMAT_ERROR");
				put("SELLER_NOT_SIGN_PROTOCOL", "SELLER_NOT_SIGN_PROTOCOL");
				put("INVALID_CHARACTER_SET", "INVALID_CHARACTER_SET");
				put("ACCOUNT_NOT_EXISTS", "ACCOUNT_NOT_EXISTS");
				put("EMAIL_USERID_NOT_MATCH", "EMAIL_USERID_NOT_MATCH");
				put("REFUND_ROYALTY_FEE_ERROR", "REFUND_ROYALTY_FEE_ERROR");
				put("ROYALTYER_NOT_SIGN_PROTOCOL", "RESULT_AMOUNT_NOT_VALID");
				put("REASON_REFUND_ROYALTY_ERROR", "REASON_REFUND_ROYALTY_ERROR");
				put("TRADE_NOT_EXISTS", "TRADE_NOT_EXISTS");
				put("WHOLE_DETAIL_FORBID_REFUND", "WHOLE_DETAIL_FORBID_REFUND");
				put("TRADE_HAS_CLOSED", "TRADE_HAS_CLOSED");
				put("TRADE_HAS_FINISHED", "TRADE_HAS_FINISHED");
				put("NO_REFUND_CHARGE_PRIVILEDGE", "NO_REFUND_CHARGE_PRIVILEDGE");
				put("RESULT_BATCH_NO_FORMAT_ERROR", "RESULT_BATCH_NO_FORMAT_ERROR");
				put("BATCH_MEMO_LENGTH_EXCEED_LIMIT", "BATCH_MEMO_LENGTH_EXCEED_LIMIT");
				put("REFUND_CHARGE_FEE_GREATER_THAN_LIMIT", "REFUND_CHARGE_FEE_GREATER_THAN_LIMIT");
				put("REFUND_TRADE_FEE_ERROR", "REFUND_TRADE_FEE_ERROR");
				put("SELLER_STATUS_NOT_ALLOW", "SELLER_STATUS_NOT_ALLOW");
				put("SINGLE_DETAIL_DATA_ENCODING_NOT_SUPPORT", "SINGLE_DETAIL_DATA_ENCODING_NOT_SUPPORT");
				put("TXN_RESULT_ACCOUNT_STATUS_NOT_VALID", "TXN_RESULT_ACCOUNT_STATUS_NOT_VALID");
				put("TXN_RESULT_ACCOUNT_BALANCE_NOT_ENOUGH", "TXN_RESULT_ACCOUNT_BALANCE_NOT_ENOUGH");
				put("CA_USER_NOT_USE_CA", "CA_USER_NOT_USE_CA");
				put("BATCH_REFUND_LOCK_ERROR", "BATCH_REFUND_LOCK_ERROR");
				put("REFUND_SUBTRADE_FEE_ERROR", "REFUND_SUBTRADE_FEE_ERROR");
				put("NANHANG_REFUND_CHARGE_AMOUNT_ERROR", "NANHANG_REFUND_CHARGE_AMOUNT_ERROR");
				put("REFUND_AMOUNT_NOT_VALID", "REFUND_AMOUNT_NOT_VALID");
				put("TRADE_PRODUCT_TYPE_NOT_ALLOW_REFUND", "TRADE_PRODUCT_TYPE_NOT_ALLOW_REFUND");
				put("RESULT_FACE_AMOUNT_NOT_VALID", "RESULT_FACE_AMOUNT_NOT_VALID");
				put("REFUND_CHARGE_FEE_ERROR", "REFUND_CHARGE_FEE_ERROR");
				put("REASON_REFUND_CHARGE_ERR", "REASON_REFUND_CHARGE_ERR");
				put("DUP_ROYALTY_REFUND_ITEM", "DUP_ROYALTY_REFUND_ITEM");
				put("RESULT_ACCOUNT_NO_NOT_VALID", "RESULT_ACCOUNT_NO_NOT_VALID");
				put("REASON_TRADE_REFUND_FEE_ERR", "REASON_TRADE_REFUND_FEE_ERR");
				put("REASON_HAS_REFUND_FEE_NOT_MATCH", "REASON_HAS_REFUND_FEE_NOT_MATCH");
				put("REASON_REFUND_AMOUNT_LESS_THAN_COUPON_FEE", "REASON_REFUND_AMOUNT_LESS_THAN_COUPON_FEE");
				put("ATCH_REFUND_STATUS_ERROR", "ATCH_REFUND_STATUS_ERROR");
				put("BATCH_REFUND_DATA_ERROR", "BATCH_REFUND_DATA_ERROR");
				put("REFUND_TRADE_FAILED", "REFUND_TRADE_FAILED");
				put("REFUND_FAIL", "REFUND_FAIL");

			}
		};

	}
}
