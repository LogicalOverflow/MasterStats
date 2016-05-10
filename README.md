# MasterStats

MasterStats is a web application providing statistical data on champion
mastery. It gathers and depicts highest grades and champion level distribution graphs
for both summoners and champions. Champions additionally show a mastery score
and a player region distribution graph as well as the highest mastery scores known.
For summoners the top champions without chests, the top champions overall,
the sum of all champion mastery scores as well as a distribution graph of chests
granted and champions played are also provided.

This project runs on a tomcat (Version 8.0.33) server using the wicket framework
for the web interface and storing the data in an Amazon web services DynamoDB. Maven is used as build
system. The live demo is deployed using Amazon's AWS Beanstalk. A running demo can be found at
http://masterstats-default.eu-central-1.elasticbeanstalk.com/.

## Setting Up
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

If you want to use other table/index names you can archive this by also update the DBTable
enum and the matching DataClass (all in the Db package) accordingly. If you use
different capacities, the code will automatically use them.

Next, you must create 2 properties files in the folder src/main/resources:
* api.properties: has to contain the following properties:
  * apiKey: your API key
  * devKey: true, if you use a development key and false in case you are using a
  production key (used to configure the rate limits). If the option has
  an invalid value or is not set at all, false is used as default value.
* dynamoDB.properties: has to contain the following properties:
  * accessKey: the access key for an IAM user with access to the database
  * secretKey: the secret key for an IAM user with access to the database
  * region: the region where the dynamoDB is hosted (e.g. "EU_CENTRAL_1")

Now you can build the MasterStats-Server.war file using maven. The war file
can then be deployed to your tomcat server.

As new summoners are collected by using all the existing summoners in the database,
you might have to add some from each region manually to the database. The easiest
way to do this is to just search for them on your instance.

## Technology

I use [Tomcat](http://tomcat.apache.org/), [Wicket](http://wicket.apache.org/) and mainly because I
already have experience acquired development experience in using them.
[AWS DynamoDB](https://aws.amazon.com/dynamodb) is used because it can cope with my large amount of
collected data while remaining fast and being easily scalable. Additionally, Amazon distributes nice
and helpful [Java SDK](https://aws.amazon.com/sdk-for-java/) for their web services.

[AWS Beanstalk](https://aws.amazon.com/elasticbeanstalk) is used for deployment of the live demo and
was very easy to use, as I just needed to upload my war file to have my application up and
running.

[Highcharts](http://www.highcharts.com/) is the graphing library I have chosen to use because it allows me
to generate the graphs from the Java code without the need of touching any CSS or HTML. The
[Bootstrap](http://getbootstrap.com/) framework is used as it makes it easy to create
good-looking and state of the art websites.

[Lombok](https://projectlombok.org/) and [Apache Commons](https://commons.apache.org/) have both
made my life much easier with their convenient annotations and provided function sets.

[Gunava](https://github.com/google/guava) is only used for their RateLimiter, which enables me to
not send too many requests to the database or the Riot's API while having multithreading and many
asynchronous and scheduled calls.

[Slf4j](http://www.slf4j.org/) and [Log4j](http://logging.apache.org/log4j) are used for logging,
because of their widely spread common usage and I already have experience in using them.

## Code Documentation and Comments

Even though the entire code is pretty much readable on its own, it ii
still commented (except some simple data classes) for easier reading
and understanding, so if you want to know how anything works, just have a look at
the code. If any questions pop up, feel free to contact me so I can provide you with an
answer and clarify the comments.

## Challenges

### Storing and analyzing the data
While working on the project, one of the challenges I encountered, was how to store
the huge amount of data. My first try was to use a MySQL database. It worked well at first,
but when I reached around 100000 summoners, the database got really slow. Then I
decided to go for DynamoDB, that I was using already a little bit while looking around
the AWS Management Console. I created my tables and transferred all the data from
the MySQL to DynamoDB using proprietary Java code. While transferring I updated my code to
use annotated data classes for the DynamoDB items and a mapper to access the
database. Additionally, I refactored all my functions (statistic generation, adding
summoners, etc.) to be able to use DynamoDB.

When I was using MySQL at first I leveraged SQL intrinsic used aggregate functions to
generate my statistics. With DynamoDB, I needed to take a different approach, because
there are no aggregate functions. I started scanning the whole database to collect
the necessary data while scanning. This process is very read intensive for the database and I decided
to store the generated statistics in the database as well and generate them only once a day
in the middle of the night using schedulers.

### Collecting the summoners
Finding new summoners was a challenge because there is no way of getting
random summoners using Riot's API directly. I first started checking randomly created summoner
ids on all regions, always generating 10 random ids at a time and then requesting the summoner
information from the API for each region. This actually worked better than I had expected
and after a few days, there were more than 40000 summoners from 6 regions (TR, KR, EUNE,
EUW, NA, BR) in my database. Then I decided to change things up because the summoners I
was collecting were partially not active and I needed a lot of requests for only a few summoners.

My new and current method is, to get a group of summoners from the database and then request
their match histories. After that all summoner from their last games played are added to the
database as well. If a summoner has no games in their history, they will be deleted from the
database because those summoners are not actively playing.

### Keeping my credentials save
As all the code must be published on GitHub, hardcoding the API key and my AWS credentials
didn't sound like a great idea. I looked for an easy and clean way to keep my API key
and my AWS credentials save without storing them in system variables because I want to be
easily able to switch the server hosting my page. Additionally, making it easy to set them
for other people, was important for me as well, as I want other people to easily set
up their own instance of my code.

After thinking about the problem for a while, I decided to put the information, I want to
keep save, in properties files. On the one hand, they are easily accessible from the code
as I added classes (AWSPropertiesProvider and RiotApiFactory) to read the properties from
the file and provide them to the application where needed. On the other hand, they are easily
accessible from outside, without a need to recompile anything, and can be excluded from GitHub
without any problems. I also decided to move the AWS Region to this properties file
as it is more easily accessible in contrast to a hardcoded region and an option one might want to change.

### Visualizing the data
Even though I had my statistics, just printing them out as text does not look nice and makes it
hard to look at and draw any conclusions. That is why I needed a way to make nice graphs using
my data. I first tried [chartist.js](https://gionkunz.github.io/chartist-js/), but even though
the library seems to be nice, getting the data from the Java code to the charts, styling and
configuring was not something done well. So I started looking for alternatives and found
[Wicked Charts](https://github.com/thombergs/wicked-charts), a wrapper for the JavaScript library
[Highcharts](http://www.highcharts.com/). Using it, I was able to completely configure the charts
using Java and create charts, looking the way I wanted them to and having features like zooming or
filtering.


### Respecting rate limits
As both the Riot API as well as the reads and writes to DynamoDB are limited I had to ensure I do not
run too many requests too fast. After some research, I found [Guavas RateLimiter](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/util/concurrent/RateLimiter.html),
which does exactly what I need. I created an instance per API region and called the rate limiters
acquire to wait for available capacity.

For DynamoDB rate limiters are used as well with two rate limiters for each table and global secondary
index (one for reading and one write for writing), and whenever a database action is performed,
the rate limiters are used to ensure the provisioned throughput is not exceeded. Additionally,
the rate limiters limits are requested directly from the database. This way the capacities can be
updated and the code will automatically use the new rate limits. The local rate limits are updated
every night as the first step of the nightly data updating process because this way these updates always
use up-to-date rate limits.