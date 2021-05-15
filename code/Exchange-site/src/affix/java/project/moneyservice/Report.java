package affix.java.project.moneyservice;

import java.time.LocalDateTime;
import java.util.List;

/**
 *	This class defines the Report in MoneyService
 *	Report should be created with a List<Transactions> and LocalDateTime.
 */
public class Report {

	/**
	 * Attribute id holding the Id for the Report
	 */
	private final long id;
	/**
	 * Attribute day defining the Day of the Report
	 */
	LocalDateTime day;
	/**
	 * Attribute dailyTransactions holding a List of Transaction
	 */
	List<Transaction> dailyTransactions;
	/**
	 * Attribute uniqueId holding the unique id of the Report
	 */
	public static int uniqueId = 0;
	/**
	 * Attribute uniqueFilename holding the filename for the Report
	 */
	public String uniqueFilename;
	
	/**
	 * Constructor
	 * @param day Time indicating the day of the Report
	 * @param dailyTransactions List of Transactions for the Report
	 */
	public Report(LocalDateTime day, List<Transaction> dailyTransactions) {
		++uniqueId;
		this.id = uniqueId;
		this.day = day;
		this.dailyTransactions = dailyTransactions;
		this.uniqueFilename = String.format("Report_%s_%s.ser",ExchangeSite.name.toString().trim(), day.toLocalDate());
	}

	/**
	 * Getter for attribute day
	 * @return a LocalDateTime holding day
	 */
	public LocalDateTime getDay() {
		return day;
	}

	/**
	 * Sets the day to Report according to input
	 * @param inputDay holds a value for day
	 */
	public void setDay(LocalDateTime inputDay) {
		this.day = inputDay;
	}

	/**
	 * Getter for attribute dailyTransactions
	 * @return a List<Transactions> holding dailyTransactions
	 */
	public List<Transaction> getDailyTransaction() {
		return dailyTransactions;
	}

	/**
	 * Sets the dailyTransactions to Report according to input
	 * @param newDailyTransactions holds a list for dailyTransactions
	 */
	public void setDailyTransaction(List<Transaction> newDailyTransactions) {
		this.dailyTransactions = newDailyTransactions;
	}

	/**
	 * Getter for attribute id
	 * @return a long holding id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Getter for attribute uniqueFilename
	 * @return a String holding uniqueFilename
	 */
	public String getUniqueFileName() {
		return this.uniqueFilename;
	}
	
	@Override
	public String toString() {
		return String.format("Rapport [id=%s, day=%s, dailyTransactions=%s]", id, day, dailyTransactions);
	}

}
