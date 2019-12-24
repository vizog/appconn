How to run
===========
- [Install](https://www.scala-sbt.org/1.0/docs/Setup.html) `sbt 1.x`
- Start the cassandra docker container by running `docker-compose up -d` in project root directory
- Run the application: `sbt "runMain com.vahid.appconn.Main"`
- Application will start listening to port `8888` for REST API and port `9999` as UDP events endpoint.

Design decisions
================ 
For this exercise, my focus has been on implementing a solution which has a reasonable performance and is easily scalable.
Key considerations:
 - Uses persistent distributed Akka actors backed by Cassandra.
 - Each actor (MobileAppActor) represents a single mobile application which is being monitored for it's external communication.
 - All in memory state of the actors are deleted (by actor passivation) after a certain idle time. 
   This results in significant low memory consumption.
 - The system state is recovered after shutdown or failure/restart. 
 - The implementation leverages Akka's reactive nature and hence there is zero thread blocking in the app flow.
 - It is fully distributable on multiple nodes by just making configuration changes.
 - Each node has its own rest endpoint and finding the right actor is done by cluster sharding.
 
Shortcomings 
=============
- The tests written for this application are only demonstrating my style and they are not enough for a production application.
Obviously, given more time, I would add more tests.
- Some real-world sanity checks have been skipped in this exercise. For example, it is assumed that 
each mobile app communicates with a limited number of unique IPs. So the list of unique IPs are kept in the memory
for each app. While this is a sensible assumption, for a real production release, I would limit the number of IPs
for each actor to a reasonable number and I would fail the requests only for those actors, rather than getting an
OutOfMemoryError.
- Some optimizations like configuring Snapshot stores for the events and cluster management strategies  
 (like split-brain resolution) are skipped in this exercise.
- I have not provided a version which is directly runnable on a public cloud but that is not hard to do 
and the app can be deployed to a public cloud with mostly configuration changes.
- The logic implemented to decide which IPs are "bad" is my best effort of what the exercise is asking for. 
While the app is passing all runs of the test client (with millions of events), it is only implemented to mark the 
*largest* group of IPs that belong to a common /28 network as "good" and the rest of the IPs as "bad".   