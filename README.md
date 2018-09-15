### Design Problem : Writing Data Prediction Without Any 3rd Party Library.

An investment company sends out returns on its investment to users at certain intervals usually twice or thrice a month.
The interval may vary for differet investment investment plans viz. Currently Company Has Six Investment Plans viz

* LOWCAP
* MIDCAP
* HIGHCAP
* SHORTTERM
* MIDTERM
* LONGTERM

### Objective
This user wants to predict the next date-time when investment return will be credited to his/her account. 

* Write a simple command line tool in Java for this prediction. Use simple average for Prediction 
* The tool will take plan type as input from the user and will produce output in the following format: 
* The tool will make a web service call to retrieve JSON response, parse the JSON and calculate the next date. 
* For one of the user history of credit time and amount of each return can be obtained by following. 

```
http://demo4729673.mockable.io/LOWCAP
```

This URL returns the information in JSON format. 

EXAMPLE INPUT FOR PREDICTOR APP
```
{
	"name": "Jarrod Simon",
	"investmentType": "LOWCAP",
	"investmentDate": "12-01-2011",
	"returns": [{
		"tranDate": "16-01-2011 23:23:52",
		"tranAmount": "2,688.87"
	},
	{
		"tranDate": "02-08-2011 22:40:05",
		"tranAmount": "653.47"
	},
	{
		"tranDate": "07-08-2011 03:41:10",
		"tranAmount": "9,834.85"
	},
	{
		"tranDate": "13-08-2011 12:41:55",
		"tranAmount": "9,967.21"
	}]
}

```

SAMPLE OUTPUT
```
Next Date:<date-time> 
```


### Please note: 

* Create a java project and then create a file Predictor.java. 
* All the code should be written in this file only. You can define all the interfaces/classes in this single file. Please do not create any other file.
* Apart from JSON-Simple library to be used for json parsing, no other third party library should be used in the application. Code will be disqualified if any third party library (apart from json-simple) is used.
 
### The code written should be of good quality and should have these Aspects:

* Code should compile and should be complete. 
* Should be modular and should use Abstraction. 
* Should handle all exceptions and cases (for e.g. parsing error, connection error etc.)
* Identify the Junit test methods and mention method names in comments over the main method of your application. 

