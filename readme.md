Welcome to HeadQuarters and ExchangeSite
----------------------------------------------------------------------------------
Instructions for ExchangeSite.

1 Go into the Configs folder. Copy and paste a ProjectConfig file.
 
Change the date of the filename to the current date.

2 Open the copied file, localize the "CurrencyConfig" information.

Change that filename date to the current date as well.

3 Go into the DailyRates folder, Copy and paste a CurrencyConfig file. 

Change the date of the filename to the current date. 

4 Run one or more of the five shell-scripts named "ExchangeSite[Direction].sh"

----------------------------------------------------------------------------------
Instructions for HeadQuarters.

Run the shell-script named "HeadQuarters.sh"

----------------------------------------------------------------------------------
HeadQuarters and ExchangeSite

A Foreign-Exchange business simulation.

Currently contains around 2500 transactions.

Values and data exist for 2021-04-01 to 2021-04-30.


ExhangeSite functionality:

The ExchangeSite allows a company to set up and operate a foreign exchange business

complete with customer interface for placing orders, price calculation and processing

of an accepted order into a transaction. The addition of more sites are are supported,

these can operate independently.

In order to separate client and employee functionalities, a password is required. This password

can be found in the site configuration file.

The program parses configurationfiles containing the currencies and ammounts available

for the site as well as the daily exchange-rates as prepared by the Swedish Riksbank.


The customer is offered a buy and sell rate with a configurable profit-margin, if the customer

accepts the price when buying or selling a currency, the necessary treatment of daily funds

are calculated and a transaction is completed and stored. On program exit, the list of daily

transactions are saved using serialized format and sent to HeadQuarters for analysis.

A Site-report is also prepared to allow HeadQuarters to verify the impact of daily trade.


HeadQuarters functionality:

The HeadQuarters allows for the preparation of large amounts of transactions from several 

sites in order to calculate statistics based the parameters of Site, Period and Currency 

where Period refers to either Daily, Weekly or Monthly basis and the Currencies 

treated during the period in question.

The program uses the Streams-api to filter and the transactions.

The output is presented on the console.


The HeadQuarters prepares the daily ProjectConfig files for each of the sites.

For each site, the reference-currency, included amounts and currencies can be customized.


The HeadQuarters can verify the integrity of the sums of the transactions and their 

implications for the values reported by the sites on a daily basis, making sure there

are no differences in the reported values and the sum the of transactions made.

----------------------------------------------------------------------------------
My Contributions:

In HeadQuarters

Class - Statistics

Class - HQ

Class - Config

Class - HQIO

Class - HQTheApp (except functionality of case 2 and 3).

In ExchangeSite:

Class - MoneyServiceIO

Class - ExchangeRate

Class - Config

----------------------------------------------------------------------------------
@Authors

Johannes Ahlström

Karl Lindberg

Peter Angell

Albin Hjort

Jakob Solberg