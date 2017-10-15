package com.unibet.worktest.exception;

import com.unibet.worktest.bank.BusinessException;

/**
 * Business exception thrown if account already exists in database
 * 
 * @author Moupiya
 *
 */
public class AccountAlreadyExistsException extends BusinessException{

	public AccountAlreadyExistsException(String message) {
		super(message);
	}

}
