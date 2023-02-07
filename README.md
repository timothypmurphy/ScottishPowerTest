# Scottish Power - Java - Tech Test

This project is a RESTful web service, creating in Spring Boot, designed to retrieve and add smart meter readings to a database.
Some assumptions about the data structure have been made and those will be detailed below.
This project come with a suite of Unit and BDD tests.

## Table of Contents
1. [Initial Design](#initial-design)
2. [Notable Decisions](#notable-decisions)
3. [Areas of Improvement](#areas-of-improvement)
4. [Running](#running)
5. [Testing](#testing)

## Initial Design

### Table Structure
When I was deciding what tables I should create for this challenge, I knew I would have an Account table, but I was debating whether I should create two seperate tables for the two different types of readings, or just have one.
In the end I decided that one table made sense as they would have the same columns and would only need an additional column to differentiate between a GAS and ELECTRIC reading.
When it came to the extension tasks, having all readings in one table did create a bit more work for me, as I would have to filter an account's reading by reading type.

### Solution Design
The first step I took when starting this task was to create the bulk of the classes that I would need:
* The entity files for Account & Reading
* The repository files, so I could send CRUD commands to the DB
* The service files, which is where all my business logic would be stored
* The controller files, for handling requests that are sent to the service

As you can see from above, when designing this I followed the Controller-Service-Repository design, creating a separation of concerns.
## Notable Decisions

### Validation
I implemented two different methods of validation in this project: Custom exceptions and validation annotations. The annotations
check that compulsory fields are supplied when POSTing new meter readings. Currently, they do not provide a meaningful error
message to the client and just send a generic 400 Bad Request response - if I worked on this longer then I would want to
ensure the client receives a bit more information on why their request was rejected.

For the custom exceptions, I created four, along with a ControllerAdvisor, so that they could all be managed within one Class:
- AccountNotFoundException - quite self-explanatory, this will throw if the client requests meter readings for an account that does not exist.
This exception is also thrown if the client attempts to POST meter readings for an account that does not currently exist - this was intentional
as I assumed account creation would be a separate process before submitting meter readings.
- DuplicateReadingFoundException - this was used to validate that a reading for an account, of a particular type and with the same date,
did not already exist in the DB, as was requested in the test brief.
- HistoricReadingFoundException - I added this exception as I assumed it should not be possible to submit a meter reading dated before the
previous meter reading on an account
- ReadingTooLowException - This was another assumption I made, not allowing a reading to be submitted that has a value below the previous
meter reading on the account.

All of these exceptions return a meaningful error message back to the client, with specific information on the data that caused the exception.

### Big Decimal / Integer
Reading through my code you will notice that I make use of a lot of Big Decimals and Integers. This was a change I made about half-way through
this challenge, after I remembered about the existence of these data types while I was struggling to divide a long by an int (need to get me head
out of JavaScript). My thinking was that meter readings,
and calculations based on them, should always remain accurate, as these numbers are responsible for generating bills for customers. I am aware that
calculations using Big Decimals and Integers can be much slower, but I am not familiar with the impact they would have on an application such
as this, so perhaps they were not the best data types to use - this is something I would want to look into further if I worked on this project
more.

### Initially calculating averages when SELECTing from table
When I began the second extension task, to add usage and period since last read, I initially attempted to calculate these values everytime an
account's readings were requested. I pivoted away from this approach for a few reasons:
1. The code to calculate these values for every meter reading in an account was more tedious to write than I first thought;
2. This calculation would be carried out every time an account's readings were requested, instead of just being calculated once and stored somewhere;
3. As the number of readings in an account grew, so would the number of calculations, increasing the computation required to carry this task out.

Once I saw the next extension task was to also calculate average usage for each meter reading, I decided to calculate these values when they are
POSTed to the service and store them in the Reading table. This reduced the computation required, as well as the complexity of the code, but
would add a small amount to the storage required, though I think that's a fair trade off.

## Areas of Improvement
In this section I will highlight specific areas of this solution that I think could be improved with a bit more time or if I had a bit more experience writing Java / Spring

### Reading Repository
Firstly, something which bothered me was the name of the methods in the ReadingRepository class.
Using this naming convention meant that the SQL query would be automatically generated, allowing me to quickly create custom SELECT queries,
but given more time I would like to have taken the time to replace these with more readable names by using the @Query annotation.

### DTO Validation
I have implemented some simple validation, using the @NotNull and @Valid annotations, in the DTO classes - this allowed some
quick implementation of validation for the POSTed meter readings. Currently, the service will just reply with a '400 Bad Request'
when you attempt to send meter readings without one of these compulsory fields - in the future I would like to return a more
meaningful message to the client.

### Reading Service - validateReading()
The efficiency of this method is not ideal. Actually, while writing this README I just noticed that I was not filtering 
the account's reading by ReadingType until the service layer - I've just gone in and moved that to the repository layer.
This method will check through every reading of that type on an account, looking for a date match -
as the list is ordered by Date, a more efficient way of handling this would be to check if the date of the reading being submitted
is less than the date being compared against, if so then you can confirm that it is not a duplicate. 

### Reading Service - convertReadingsToDto()
Similarly to above, I was initially doing the filtering and sorting of readings at the service layer - 
moving to the repository layer should increase the efficiency of the method, particularly when it comes to sorting the readings
by Date.

### Reading Service - calculateCustomerAverageUsage()
This is a method that makes me really uneasy. I did not initially notice the vagueness of the task asking for a 
'comparison against other customers' and so assumed it would be the average daily energy consumption of the 
average account, which is what I then implemented. This would become quite a taxing calculation if the number of accounts grew - its
current implementation pulls the readings of a particular type for all accounts in the DB, pulls out the most recent reading
for each account, and then calculates a global average.
Also, this method is called every time an account's readings are requested, so it really would not scale well.
I had initially created a SQL Query that would only SELECT the most recent readings for each account,
but I could not find a way to translate it to JPQL.

A potential solution to this would be to only calculate this value after a certain period of time, say once every
24 hours, and then store it somewhere. Over the course of that time energy consumption is very unlikely to change drastically,
so it should still be a fairly accurate average. Also, I'm sure there is a way in Spring and JPA to only select the most
recent readings for all the accounts, which is something I would have looked into further if I spent more time working
on this.

### Model / Entity
Currently, my application does not have a separate model and entity class for Readings and Accounts - this has worked okay so far
but if the application were to grow in functionality then it may become necessary to have a separate model class for the
objects.

### Use of Java Streams
I make extensive use of Streams throughout my application, they were quick and easy to write and I find that they are quite easy to read (if you have
encountered lambda functions before). But, streams can come with some issues if this were to be scaled up, namely their memory usage could become
a significant problem, particularly when paired with the ArrayLists I've used in abundance
(I found this [Medium article](https://medium.com/levi-niners-crafts/java-performance-improvement-java-8-streams-vs-loops-and-lists-vs-arrays-e824136832d6)
on the topic fascinating), and perhaps replacing some of my streams with loops would be a wiser idea. I also briefly looked into parallel streams, but
quickly abandoned this idea when considering the importance of the order of my ArrayLists for Readings.

## Running
The application is run from the ScottishPowerTestApplication class and when booted up the initDatabase method is called in LoadDatabase class,
which adds a few accounts and readings to the DB. If you wish to add other accounts or readings to the DB then they can be added here.

NOTE: validation is not carried out on entities that are added via this method.

## Testing
I have created several Unit tests and Cucumber tests for this project. Neither are complete in their coverage, but they cover many of the
potential scenarios that may arise.

### Unit Testing
My unit testing uses JUnit and Mockito. Currently, I only have unit tests for the two service classes, but these classes hold most of the code
and all the logic in the project. I'm quite pleased with the different scenarios I've covered in these unit tests, checking for exceptions being
thrown as well as testing different branches that the individual methods can take. I would like to have completed a full unit test suite, covering
some of the other classes in the application. I also think the tests I have written are not the cleanest, so given 
more time I would make them more readable.

### BDD Testing
I have edited and added to Cucumber tests before, but have never had to set them up from scratch, so this took some time to figure out.
The tests cover different scenarios of sending the GET and POST requests to the service and check for the different response codes and messages
that the client can receive. 

I have a few issues with the tests that I've created. Firstly, if you run the test suite twice, without rebooting the application between runs,
then all the POST tests will fail due to the meter readings already being submitted and a DuplicateFoundException being thrown by the
service. Also, all the data that is preloaded comes from LoadDatabase.class, I would like to investigate further if there is a way to load specific mock,
testing data for the Cucumber tests. Additionally, the way I have written the test steps is not the cleanest and most dynamic - I have written the expected response messages as
strings, with the dates and account IDs hardcoded into them. It would be far better to implement it in a way that pulls this information
from a parameter in the cucumber expression. Finally, I haven't written any tests that send a POST request and then a GET request checking that
the new meter readings are received.