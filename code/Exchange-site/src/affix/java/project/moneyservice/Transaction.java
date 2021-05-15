package affix.java.project.moneyservice;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class defines a Transaction in MoneyService
 * Transaction should be created by an Order + a LocalDateTime
 */
public class Transaction implements Serializable {
	
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 1L;
	
	/**
	 * Attribute id holding the Id for the Transaction
	 */
	private final int id;
	/**
	 * Attribute timeStamp defining the Time of the Transaction
	 */
	private final LocalDateTime timeStamp;
	/**
	 * Attribute currencyCode holding the Currency of Transaction 
	 */
	private final String currencyCode;
	/**
	 * Attribute amount holding the Amount of the Transaction
	 */
	private final int amount;
	/**
	 * Attribute mode holding the enum TransactionMode of the Transaction
	 */
	private final TransactionMode mode;
	/**
	 * Attribute uniqueId holding the unique id of the Transaction
	 */
	private static int uniqueId = 0;
	
	/**
	 * Constructor
	 * @param order holding value, currencyCode and transaction mode
	 */
	public Transaction(Order order) {
		this(order, LocalDateTime.now());
	}
	
	/**
	 * Overloaded Constructor
	 * @param order holding value, currencyCode and transaction mode
	 * @param ref Time indicating when the order is being processed
	 */
	public Transaction(Order order, LocalDateTime ref) {
		++uniqueId;
		this.id = uniqueId;
		this.currencyCode = order.getCurrencyCode();
		this.amount = order.getValue();
		this.mode = order.getTransactionType();
		this.timeStamp = ref;
	}
	
	/**
	 * Getter for attribute uniqueId
	 * @return an int holding uniqueId 
	 */
	public static int getUniqueId() {
		return uniqueId;
	}

	/**
	 * Sets the uniqueId for Transaction according to input
	 * @param newuniqueId holds a value for uniqueId
	 */
	public static void setUniqueId(int newuniqueId) {
		Transaction.uniqueId = newuniqueId;
	}

	/**
	 * Getter for attribute id
	 * @return an int holding id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Getter for attribute timeStamp
	 * @return a LocalDateTime holding timeStamp
	 */
	public LocalDateTime getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Getter for attribute currencyCode
	 * @return a String holding currencyCode
	 */
	public String getCurrencyCode() {
		return this.currencyCode;
	}

	/**
	 * Getter for attribute amount
	 * @return a int holding amount
	 */
	public int getAmount() {
		return this.amount;
	}

	/**
	 * Getter for attribute mode
	 * @return a enum holding mode
	 */
	public TransactionMode getMode() {
		return this.mode;
	}

	@Override
	public String toString() {
		return String.format("Transaction [id=%s, timeStamp=%s, currencyCode=%s, amount=%s, mode=%s]", this.id, this.timeStamp,
				this.currencyCode, this.amount, this.mode);
	}
}