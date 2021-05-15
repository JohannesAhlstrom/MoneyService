package MoneyServiceAPP;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import affix.java.project.moneyservice.Config;
import affix.java.project.moneyservice.Currency;
import affix.java.project.moneyservice.ExchangeSite;
import affix.java.project.moneyservice.MoneyBox;
import affix.java.project.moneyservice.MoneyServiceIO;
import affix.java.project.moneyservice.Order;
import affix.java.project.moneyservice.Transaction;
import affix.java.project.moneyservice.TransactionMode;

/**
 * This class is the MoneyServiceApp for MoneyService
 * MoneyServiceApp holds main and should take in argument in a String Array
 */
public class MoneyServiceAPP {

	/**
	 * Attribute logger holding the Logger for MoneyServiceAPP
	 */
	private static Logger logger;
	/**
	 * Attribute fileHandler holding the FileHandler for MoneyServiceAPP
	 */
	private static FileHandler fileHandler;

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		if(args.length >= 1) {
			if(!Config.readConfigFile(args[0])){
				System.out.println("All config parameters in config file need to be correct!");
				System.out.println("Exiting the program...");
				System.exit(0);
			}
		}
		else {
			System.out.println("You need a config file to get the program started!");
			System.out.println("Exiting the program...");
			System.exit(0);
		}

		logger = Logger.getLogger("affix.java.project.moneyservice");

		try {
			if(Config.getLogFormat().equals("text")) {
				fileHandler = new FileHandler(MoneyServiceIO.getPathName("Orders")+Config.getLogName()+".txt");
				fileHandler.setFormatter(new SimpleFormatter());
			}
			else {
				fileHandler = new FileHandler(MoneyServiceIO.getPathName("Orders")+Config.getLogName()+".xml");
				fileHandler.setFormatter(new XMLFormatter());
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.addHandler(fileHandler);

		String currentLevel = Config.getLogLevel();

		logger.setLevel(Level.parse(currentLevel));

		ExchangeSite theSite = new ExchangeSite(Config.getSiteName());

		theSite.startTheDay();

		Scanner keyboard = new Scanner(System.in);
		keyboard.useDelimiter(System.lineSeparator());

		boolean exit=false;
		int choice=0;
		do { 
			try { 
				System.out.println("\n******** Employee menu for Exchange site "+Config.getSiteName()+" *********");
				System.out.println("\nPlease make a choice:");
				System.out.println("1. - Show todays exchange rates");
				System.out.println("2. - Show cash box content");
				System.out.println("3. - Create order for customer");
				System.out.println("4. - User menu");
				System.out.println("5. - Show daily transactions");
				System.out.println("0. - Exit the program, and end the day");
				System.out.println("\n*********************************");

				String input=keyboard.next();
				choice=Integer.parseInt(input);	

				switch (choice) {
				case 1: 
					System.out.println("********************* Todays rates *********************");
					for(Entry<String, Currency> er:theSite.getCurrencyMap().entrySet()) {
						if(!(er.getKey().equalsIgnoreCase(MoneyServiceIO.referenceCurrency)) && !(er.getValue().getBuyRate() == 0.000f)){
							System.out.format(er.getKey().toString());
							System.out.format(" Buying: %.3f",er.getValue().getBuyRate());
							System.out.format(" Selling: %.3f",er.getValue().getSellRate());
							System.out.println("");							
						}
					}
					System.out.println("********************************************************");
					logger.finer("Press 1. Showing todays rates for employee");
					break;

				case 2:
					System.out.println("Content in cash box right now: ");
					logger.finer("Press 2. Showing content in cash box for employee");
					for(String k:theSite.getCurrencyMap().keySet()) {
						System.out.println(k+": "+theSite.getCurrencyMap().get(k).getTotalValue().intValue());
						logger.finer(k+": "+theSite.getCurrencyMap().get(k).getTotalValue().intValue());
					}
					break;

				case 3: 
					logger.finer("Press 3. Creating new order for customer from employee menu");
					createOrder(theSite, keyboard, true); 
					break;

				case 4:
					logger.finer("Press 4. Enter user menu");
					clientMenu(theSite);
					break;		

				case 5:
					logger.finer("Press 5. Show transactions");
					for(Transaction t:theSite.getTransactionList()) {
						System.out.println(t.toString());
					}
					break;

				case 0:
					logger.finer("Press 0. Exit the program from menu");
					exit=true;
					break;

				default:
					System.out.println("Wrong choice!");
					break;
				}

			}
			catch (NumberFormatException e) {
				System.out.println("Wrong choice! Try again");
				logger.warning("Wrong choice! Try again");
			}
		}
		while(!exit);

		String destination = "testShutDown.db";

		theSite.shutDownService(destination);

		System.exit(0);

	}

	/**
	 * Starts up the client menu for the customer
	 * @param theSite holding the ExchangeSite
	 */
	public static void clientMenu(ExchangeSite theSite) {
		Scanner keyboard = new Scanner(System.in);
		keyboard.useDelimiter(System.lineSeparator());

		boolean backToSettings=false;

		int choice=0;

		do {
			try { 
				System.out.println("********* Customer menu **********");
				System.out.println("\n\nPlease make a choice:");
				System.out.println("1. - Show todays exchange rates");
				System.out.println("2. - Create order");
				System.out.println("3. - Employee menu ");

				String input=keyboard.next();
				choice=Integer.parseInt(input);	

				switch (choice) {
				case 1:
					System.out.println("********************* Todays rates *********************");
					for(Entry<String, Currency> er:theSite.getCurrencyMap().entrySet()) {
						if(!(er.getKey().equalsIgnoreCase(MoneyServiceIO.referenceCurrency)) && !(er.getValue().getBuyRate() == 0.000f)){
							System.out.format(er.getKey().toString());
							System.out.println();
							System.out.format("Buying: %.3f",er.getValue().getSellRate());
							System.out.format(" Selling: %.3f",er.getValue().getBuyRate());
							System.out.println();
							System.out.println("Buying 100 "+er.getKey().toString()+", cost you: "+theSite.calculatePrice(er.getKey().toString(),100, TransactionMode.SELL) +" "+MoneyServiceIO.referenceCurrency);
							System.out.println("Selling 100 "+er.getKey().toString()+", you will get paid: "+theSite.calculatePrice(er.getKey().toString(),100, TransactionMode.BUY) +" "+MoneyServiceIO.referenceCurrency);
							System.out.println();
						}
					}
					logger.fine("Press 1. Showing todays rates in customer menu");
					System.out.println("********************************************************");

					break;
				case 2:
					logger.fine("Press 2. Creating new order from customer menu");
					createOrder(theSite, keyboard, false);
					break;
				case 3:
					logger.fine("Press 3. Going to employee menu from customer menu");
					System.out.println("Enter password: ");
					String temp = keyboard.next().strip();
					if(Config.controlPwd(temp)) {
						logger.fine("Correct password entered");
						backToSettings = true;						
					}
					else {
						System.out.println("Wrong password!");
						logger.fine("Wrong password entered!");
					}
					break;
				default:
					System.out.println("Wrong choice!");
					break;
				}

			}
			catch (NumberFormatException e) {
				System.out.println("Wrong choice! Try again");
				logger.warning("Wrong choice! Try again");
			}
		}
		while(!backToSettings);
	}


	/**
	 * Creates a Order using user inputs
	 * @param theSite holding the ExchangeSite
	 * @param keyboard holding the Scanner for input handling
	 */
	private static void createOrder(ExchangeSite theSite, Scanner keyboard, boolean employee) {
		boolean okInput = false;

		String transactionType ="";
		TransactionMode transMode = TransactionMode.BUY;
		int amount = 0;

		System.out.println("BUY or SELL?"); 
		if(employee) {
			System.out.println("(SELL if customer want to buy ex 50 EUR from exchange site)");
			System.out.println("(BUY if customer want to sell ex 350 GBP to exchange site)");
			System.out.println();
			System.out.println("press s for SELL a currency to customer");
			System.out.println("press b for BUY currency from customer");
		}
		else {
			System.out.println("(BUY if you want to buy ex 50 EUR from exchange site)");
			System.out.println("(SELL if you want to sell ex 350 GBP to exchange site)");
			System.out.println();
			System.out.println("press b for BUY a currency");
			System.out.println("press s for SELL a currency");
		}

		do {
			transactionType = keyboard.next().strip().toLowerCase();
			switch (transactionType) {
			case "b":
				logger.finer("BUY currency chosen (Transaction.SELL)");
				okInput = true;
				if(employee) {
					transMode = TransactionMode.BUY;			
				}
				else {
					transMode = TransactionMode.SELL;
				}
				break;
			case "s":
				logger.finer("SELL currency chosen (Transaction.BUY)");
				okInput = true;
				if(employee) {
					transMode = TransactionMode.SELL;					
				}
				else {
					transMode = TransactionMode.BUY;
				}
				break;
			default:
				System.out.println("Wrong choice!");
				break;
			}
		}
		while(!okInput);

		System.out.println("Available currencies are: ");

		String currencyChoice="";

		if(transMode == TransactionMode.SELL) {
			Set<String> keySet = theSite.getCurrencyMap().keySet();
			for(String k:keySet) {
				if(!(k.equalsIgnoreCase(MoneyServiceIO.referenceCurrency))) {
					if(theSite.getCurrencyMap().get(k).getTotalValue() >= Config.getMIN_AMMOUNT()){ 
						System.out.println(k.toString());									
					}								
				}
			}
		}
		else {
			Set<String> keySet = theSite.getCurrencyMap().keySet();
			for(String k:keySet) {
				if(!(k.equalsIgnoreCase(MoneyServiceIO.referenceCurrency))){
					System.out.println(k.toString());																	
				}
			}
		}

		System.out.println("Enter USD for US dollar, GBP for British pound... ");
		System.out.println("If you want to exit press 0");
		boolean stopOrder = false;
		do {
			okInput = false;
			currencyChoice = keyboard.next().strip().toUpperCase();

			if(currencyChoice.equals("0")) {
				logger.finer("User stopped creating order by pressing 0");
				okInput = true;
				stopOrder = true;
			}
			else {
				if(!theSite.getCurrencyMap().keySet().contains(currencyChoice))  {
					System.out.println("Bad input of currency, try again!");
					logger.finer("Bad input of currency: "+currencyChoice);
				}
				else {
					if(transMode == TransactionMode.SELL){ 
						if(theSite.getCurrencyMap().get(currencyChoice).getTotalValue() >= Config.getMIN_AMMOUNT() &! 
								currencyChoice.equalsIgnoreCase(MoneyServiceIO.referenceCurrency)){ 
							logger.finer("OK input of currency: "+currencyChoice);
							okInput = true;
						}
						else {
							System.out.println("Bad input of currency, try again!");
							logger.finer("Bad input of currency: "+currencyChoice);
						}
					}
					else {
						if(!currencyChoice.equalsIgnoreCase(MoneyServiceIO.referenceCurrency)){ 
							logger.finer("OK input of currency: "+currencyChoice);
							okInput = true;
						}
						else {
							System.out.println("Bad input of currency, try again!");
							logger.finer("Bad input of currency: "+currencyChoice);
						}
					}
				}	
			}
		}
		while(!okInput);

		if(!stopOrder) {

			System.out.println("Enter amount in steps of 50 "+currencyChoice);
			System.out.println("Existing denominations are: 50, 100, 200, 500, 1000");
			System.out.println("(If you ex write 453 you will have 450 "+currencyChoice+")");
			do {
				try {
					okInput = false;
					String temp = keyboard.next();
					amount = Integer.parseInt(temp);
					if(amount >= Config.getMIN_AMMOUNT() && amount <= Config.getMAX_AMMOUNT()) {
						amount= MoneyBox.denominationControl(currencyChoice, amount);
						logger.finer("OK input of amount: "+amount);
						okInput = true;	
					}
					else {
						System.out.println("Bad input! The amount must be between "+Config.getMIN_AMMOUNT()+" and "+Config.getMAX_AMMOUNT());
						logger.finer("Bad input!"+Config.getMIN_AMMOUNT()+" and "+Config.getMAX_AMMOUNT());
						System.out.println("Try again");
					}
				}
				catch (NumberFormatException e) {
					System.out.println("Bad input! Only number are allowed. Try again..");
					logger.warning("Bad input! Only number are allowed.");
				}
			}
			while(!okInput);

			int price= theSite.calculatePrice(currencyChoice, amount, transMode); 

			if(employee) {
				if(transMode == TransactionMode.SELL) {
					System.out.println("\nPrice for customter buying "+amount+" "+currencyChoice+ ": "+price+" "+MoneyServiceIO.getReferenceCurrency()); 
					System.out.format("Exchange buy rate in calculation: %.3f",theSite.getCurrencyMap().get(currencyChoice).getSellRate());
				}
				else {
					System.out.println("Cost for Exchangesite buying: "+amount+" "+currencyChoice+": "+price+" "+MoneyServiceIO.getReferenceCurrency());
					System.out.format("Exchange sell rate in calculation: %.3f",theSite.getCurrencyMap().get(currencyChoice).getBuyRate());
				}
			}
			
			else {
				if(transMode == TransactionMode.SELL) {
					System.out.println("\nCost for buying "+amount+" "+currencyChoice+ ": "+price+" "+MoneyServiceIO.getReferenceCurrency()); 
					System.out.format("Exchange buy rate in calculation: %.3f",theSite.getCurrencyMap().get(currencyChoice).getSellRate());
				}
				else {
					System.out.println("You will get paid: "+price+ " "+MoneyServiceIO.getReferenceCurrency()+" when selling "+amount+" "+currencyChoice);
					System.out.format("Exchange sell rate in calculation: %.3f",theSite.getCurrencyMap().get(currencyChoice).getBuyRate());
				}				
			}




			System.out.println("\n\nComplete order? ");
			System.out.println("press y for complete order ");
			System.out.println("press n for cancel order");

			do {
				String choiceContinue = keyboard.next().strip().toLowerCase();
				List<Order> orderList;
				okInput = false;
				switch(choiceContinue) {
				case "y":
					logger.finer("Complete order YES chosen");
					Order myOrder = new Order(amount,currencyChoice, transMode);

					if(myOrder.getTransactionType() == TransactionMode.SELL) {
						logger.fine("Order created from user input: "+myOrder.toString());
						if(theSite.sellMoney(myOrder)) {
							orderList = theSite.addOrderToQueue(myOrder);
							theSite.processOrderQueue(orderList);	
							System.out.println("Thanks you for doing business with us!");

						}
						else {
							System.out.println("Not enough money that currency. Order canceled");
							logger.fine("\n*****Not enough money that currency. Order canceled. In box:" +theSite.getCurrencyMap().get(currencyChoice).getTotalValue()+"*****\n");
						}
					}
					else {
						logger.finer("Order created from user input: "+myOrder.toString());
						if(theSite.buyMoney(myOrder)) {
							orderList = theSite.addOrderToQueue(myOrder);
							theSite.processOrderQueue(orderList);
							System.out.println("Thanks you for doing business with us!");
						}
						else {
							System.out.println("Not enough money in that currency. Order canceled");
							logger.fine("\n*****Not enough money that currency. Order canceled. In box:" +theSite.getCurrencyMap().get(currencyChoice).getTotalValue()+"*****\n");
						}

					}

					okInput = true;
					break;
				case "n":
					System.out.println("Order canceled");
					logger.finer("Canceled order chosen");
					okInput = true;
					break;
				default:
					System.out.println("Wrong choice!");
					break;
				}
			}
			while(!okInput);
		}
	}

}