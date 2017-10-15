package com.unibet.worktest.exception;

import com.unibet.worktest.bank.BusinessException;

/**
 * Business exception thrown if exception occurs while processing
 * transferFund request
 * 
 * @author Moupiya
 *
 */
public class TransferFundException  extends BusinessException{

	public TransferFundException(String message) {
		super(message);
	}

}
