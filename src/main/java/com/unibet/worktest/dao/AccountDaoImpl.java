package com.unibet.worktest.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.model.Account;


/**
 * Provides implementation of interface AccountDao 
 * for data access operations on database table account
 * 
 * @author Moupiya
 *
 */
public class AccountDaoImpl extends JdbcDaoSupport implements AccountDao {

	@Override
	public boolean accountExists(final String accountRef) {
		String sql = "SELECT id FROM account WHERE account_ref = ?";
    	List<Long> accountIds = getJdbcTemplate().query(sql, new RowMapper<Long>(){
					@Override
					public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
						return rs.getLong(1);
					}
    			}, accountRef);
        return !accountIds.isEmpty();
	}

	@Override
	public void createAccount(final String accountRef, final Money initialBalance) {
		String sql = "INSERT INTO account (account_ref, amount, currency) VALUES (?, ?, ?)";
        getJdbcTemplate().update(sql, accountRef, initialBalance.getAmount().doubleValue(), initialBalance.getCurrency().getCurrencyCode());
	}

	@Override
	public Account getAccount(final String accountRef) {
		String sql = "SELECT account_ref, amount, currency FROM account WHERE account_ref = ?";
		return getJdbcTemplate().query(sql, new ResultSetExtractor<Account>(){

					@Override
					public Account extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (rs.next()) {
			                String accountRef = rs.getString("account_ref");
			                BigDecimal amount = new BigDecimal(rs.getString("amount"));
			                Currency currency = Currency.getInstance(rs.getString("currency"));
			                Money balance = new Money(amount, currency);
			                return new Account(accountRef, balance);
			            }
						return null;
					}
			
				}, accountRef);
	}

	@Override
	public void deleteTable() {
		getJdbcTemplate().execute("DELETE FROM account");
	}

	@Override
	public void updateBalance(final TransactionLeg leg) {
		String sql = "UPDATE account SET amount = amount + ? WHERE account_ref = ?";
        getJdbcTemplate().update(sql, leg.getAmount().getAmount().doubleValue(), leg.getAccountRef());
	}

}
