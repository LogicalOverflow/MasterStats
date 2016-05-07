# MasterStats

Running demo: http://masterstats-default.eu-central-1.elasticbeanstalk.com/

MasterStats is a web application providing statistical data on champion
mastery. It provides highest grades and champion level distributions
for summoners and champions. Champions additionally have a mastery score
and a player region distribution as well as the highest mastery score known.
For summoners the top champions without chests, the top champions overall,
the sum of all champion mastery scores as well as a distribution of chests
granted and champions played are also provided.

This project runs on a tomcat server using the wicket framework for the web
interface and storing the data in an AWS DynamoDB. Maven is used as build
system.

# Getting started
First of all you need to create the 5 DynamoDB tables:
* champion
  * primary key (read capacity: 1, write capacity: 1):
    * partition key: keyName (string)
    * sort key: championId (number)
* championMastery
  * primary key (read capacity: 10, write capacity: 10):
    * partition key: summonerKey (string)
    * sort key: championId (number)
  * championPoints-chestGranted-index (read capacity: 1, write capacity: 10):
    * partition key: championPoints (number)
    * sort key: chestGranted (number)
* championStatistic
  * primary key (read capacity: 1, write capacity: 1):
    * partition key: keyName (string)
    * sort key: championId (number)
* summoner
  * primary key (read capacity: 5, write capacity: 1):
    * partition key: summonerKey (string)
    * sort key: summonerName (string)
  * division-tier-index (read capacity: 1, write capacity: 1):
    * partition key: division (string)
    * sort key: tier (string)
  * masteryScore-lastUpdated-index (read capacity: 5, write capacity: 1):
    * partition key: masteryScore (number)
    * sort key: lastUpdated (number)
* summonerStatistic
  * primary key (read capacity: 1, write capacity: 1):
    * partition key: summonerKey (string)
    * sort key: summonerName (string)

When you want to use other table/index names and/or capacities you must also
update the DBTable enum (Db package) and the matching DataClass (Db package)
accordingly.

Next you must create 2 properties files in the resource package:
* api.properties: only contains one the property "apiKey" holding you api key
* dynamoDB.properties: must contain the following properties:
  * accessKey: the access key for a IAM user with access to the database
  * secretKey: the secret key for a IAM user with access to the database
  * region: the region the dynamoDB is hosted (e.g. "EU_CENTRAL_1")

Now you can build the MasterStats-Server.war file using maven. The war file
can then be deployed to your tomcat server.

As new summoners are collected using the existing summoners in the database,
you will have to add some from each region by hand to the database. The easiest
was to do this is to just search for them on your instance.

# Technologies

I use Tomcat, Wicket and AWS DynamoDB manly because I already have experience
using them. AWS Beanstalk is used for deployment of the live demo and was
very easy to use, as I just needed to upload my war file to have my
application running.

I use Highcharts as graphing library as it is possible to generate the graphs
from the java code without touching any CSS or HTML. And Bootstrap is used
as it makes it easy to create good-looking website easily.

Lombok and Apache Commons are used as the made my life much easier with their
convenient annotations and functions.

Slf4j and Log4j are used for logging, just because their easy, as I already
had some experience using them.

# Comments

The whole code (except some simple data classes which do nothing) is commented
for easier reading, so if you want to know how something works, just look at
the code. If any questions crop up, feel free to contact me so I can clarify
the comments.
