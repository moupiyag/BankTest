/**
 * 
 */
package com.unibet.worktest.model;

import java.math.BigDecimal;
import java.util.Currency;

import com.unibet.worktest.bank.Money;


/**
 * Value object to represent an account details
 * 
 * @author Moupiya
 *
 */
public class Account {
	private final String accountRef;
	private final Money balance;
	
	public Account(String accountRef, Money balance) {
		if (accountRef == null) {
			throw new NullPointerException("Account Ref is NULL");
		}
		if (balance == null) {
			throw new NullPointerException("Balance is NULL");
		}
		this.accountRef = accountRef;
		this.balance = balance;
	}
	
	public String getAccountRef() {
		return accountRef;
	}
	
	public Money getBalance() {
		return balance;
	}
	
	public Currency getCurrency() {
		return balance.getCurrency();
	}
	
	/**
	 * Returns true if account balance is negative
	 * 
	 * @return true if account balance is negative
	 */
	public boolean isOverdrawn() {
		return BigDecimal.ZERO.compareTo(balance.getAmount()) > 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Account)) {
			return false;
		}
		if (!accountRef.equals(((Account) other).accountRef)) {
			return false;
		}
		if (!balance.equals(((Account) other).balance)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = accountRef.hashCode();
		result = 31 * result + balance.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "Account{" +
				", accountRef = '" + accountRef + '\'' +
				", balance = " + balance +
				'}';
	}
}
