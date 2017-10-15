/**
 * 
 */
package com.unibet.worktest.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.Transaction;

/**
 * Provides implementation of interface TransactionDao for data 
 * access operations on database tables transaction_history and transaction_leg
 * 
 * @author Moupiya
 *
 */
public class TransactionDaoImpl extends JdbcDaoSupport implements TransactionDao {

	@Override
	public void saveTransaction(final Transaction transaction) {
		String sql = "INSERT INTO transaction_history (transaction_ref, transaction_type, transaction_date) VALUES (?, ?, ?)";
		getJdbcTemplate().update(sql, transaction.getReference(), transaction.getType(), transaction.getDate());
		saveTransactionLegs(transaction.getLegs(), transaction.getReference());
	}
	
	private void saveTransactionLegs(final List<TransactionLeg> transactionLegs, final String transactionRef) {
		String sql = "INSERT INTO transaction_leg (transaction_ref, account_ref, amount, currency) VALUES (?, ?, ?, ?)";
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter()
				{
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						TransactionLeg transactionLeg = transactionLegs.get(i);
						ps.setString(1, transactionRef);
						ps.setString(2, transactionLeg.getAccountRef());
						ps.setBigDecimal(3, transactionLeg.getAmount().getAmount());
						ps.setString(4, transactionLeg.getAmount().getCurrency().getCurrencyCode());
					}
					
					@Override
					public int getBatchSize() {
						return transactionLegs.size();
					}
				});
	}

	@Override
	public Set<String> getTransactionRefsByAccountRef(final String accountRef) {
		String sql = "SELECT transaction_ref FROM transaction_leg WHERE account_ref = ?";
		List<String> transactionRefs = getJdbcTemplate().query(sql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
			
		}, accountRef);
		return new HashSet<String>(transactionRefs);
	}

	@Override
	public List<Transaction> getTransactions(final Set<String> transactionRefs) {
		StringBuilder sql = new StringBuilder()
		.append("SELECT transaction_ref, transaction_type, transaction_date FROM transaction_history WHERE transaction_ref IN (")
		.append(createCommaSeparatedQuestionMarks(transactionRefs.size()))
		.append(")");
		return getJdbcTemplate().query(sql.toString(), new RowMapper<Transaction>() {

			@Override
			public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
				String transactionRef = rs.getString("transaction_ref");
				String transactionType = rs.getString("transaction_type");
				Date transactionDate = new Date(rs.getDate("transaction_date").getTime());
				List<TransactionLeg> legs = getTransactionLegsForTransaction(transactionRef);
				return new Transaction(transactionRef, transactionType, transactionDate, legs);
			}
			
		}, transactionRefs.toArray());
	}
	
	public String createCommaSeparatedQuestionMarks(int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i<count; i++) {
			sb.append("?,");
		}
		String s = sb.toString();
		if (s.isEmpty()) {
			return "";
		}
		return s.substring(0, s.length() - 1);
	}
	
	/**
	 * Fetches a list of transaction legs for specified
	 * transactionRef from transaction_leg table
	 * 
	 * @param transactionRef client specified transaction reference
	 * @return list of transaction legs
	 */
	private List<TransactionLeg> getTransactionLegsForTransaction(final String transactionRef) {
		String sql = "SELECT account_ref, amount, currency FROM transaction_leg WHERE transaction_ref = ?";
		return getJdbcTemplate().query(sql.toString(), new RowMapper<TransactionLeg>(){

			@Override
			public TransactionLeg mapRow(ResultSet rs, int rowNum) throws SQLException {
				String accountRef = rs.getString("account_ref");
				BigDecimal amount = new BigDecimal(rs.getString("amount"));
				Currency currency = Currency.getInstance(rs.getString("currency"));
				Money transferAmount = new Money(amount, currency);
				return new TransactionLeg(accountRef, transferAmount);
			}
			
		}, transactionRef);
	}

	@Override
	public Transaction getTransactionByTransactionRef(final String transactionRef) {
		Set<String> transactionRefList = new HashSet<String>();
		transactionRefList.add(transactionRef);
		List<Transaction> transactionsList = getTransactions(transactionRefList);
		if (transactionsList.isEmpty()) {
			return null;
		}
		return transactionsList.get(0);
	}
	
	@Override
	public void deleteTables() {
		getJdbcTemplate().execute("TRUNCATE TABLE transaction_leg");
		getJdbcTemplate().execute("DELETE FROM transaction_history");
	}

}
