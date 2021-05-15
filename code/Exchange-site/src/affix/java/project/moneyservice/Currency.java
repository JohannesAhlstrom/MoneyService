package affix.java.project.moneyservice;


/**
 * This class defines a Currency in MoneyService
 * Currency should be created by:
 * -int holding the amount
 * -Float holding the buy rate
 * -Float holding the sell rate
 *
 */
public class Currency implements java.io.Serializable {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 1L;


	/**
	 * Attribute totalValue holding the value for Currency
	 */
	private double totalValue;
	/**
	 * Attribute buyRate holding the buy rate for Currency
	 */
	Float buyRate;
	/**
	 * Attribute sellRate holding the sell rate for Currency
	 */
	Float sellRate;


	/**
	 * Constructor
	 * @param totalValue holding the value defined as int
	 * @param buyRate holding the buy rate defined as Float
	 * @param sellRate holding the sell rate defined as Float
	 */
	public Currency(int totalValue, Float buyRate, Float sellRate) {
		if(totalValue <= 0) {
			totalValue =0;
		}
		this.totalValue = totalValue;
		this.buyRate = buyRate;
		this.sellRate = sellRate;	
	}

	/**
	 * Getter for attribute buyRate
	 * @return Float holding buyRate
	 */
	public Float getBuyRate() {
		return buyRate;
	}
	/**
	 * Setter for attribute buyRate
	 * @param buyRate holding a Float
	 */
	public void setBuyRate(Float buyRate) {
		this.buyRate = buyRate;
	}
	/**
	 * Getter for attribute sellRate
	 * @return Float holding sellRate
	 */
	public Float getSellRate() {
		return sellRate;
	}
	/**
	 * Setter for attribute sellRate
	 * @param sellRate holding a Float
	 */
	public void setSellRate(Float sellRate) {
		this.sellRate = sellRate;
	}
	/**
	 * Getter for attribute totalValue
	 * @return Double holding totalValue
	 */
	public Double getTotalValue() {
		return totalValue;
	}

	/**
	 * Setter for attribute totalValue
	 * @param valueToChange holding an int
	 */
	public void setTotalValue(int valueToChange) {
		this.totalValue = valueToChange;   
	}

	@Override
	public String toString() {
		return String.format("Currency [totalValue=%s]",totalValue);
	} 


}
