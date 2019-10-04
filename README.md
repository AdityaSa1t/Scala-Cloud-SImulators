# CS 441 - Homework 01 - Aditya Sawant
### Description: Create cloud simulators for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.
#### You can obtain this Git repo using the following command:

```git clone https://AdityaSa1t@bitbucket.org/AdityaSa1t/cs441_homeworkone.git```


## Overview
As part of the assignment, a total of 8 cloud simulations were developed to simulate a map-reduce job. While the same configuration was used across all simulations, it was the allocation policies that differed in each scheme.
Variations in schemes included scheduling the cloudlets and VMs in space-shared and time-shared methods. Some simulations also involve assigning the mappers and reducers to different datacenters which operate on different VM and Cloudlet Scheduling Policies.


## Instructions 
**SBT** is needed to build and run this project.   
Once cloned from the repository, open the terminal or command prompt, cd to the directory where the project is cloned and then run the following commands:  
1) ```sbt clean```    
2) ```sbt compile```    
3) ```sbt test```    
4) ```sbt run```    

**P.S**: A few of the test cases have been written in Scala and the others are written in java. So if you can't see all the executed test cases, then it is likely that they would have executed in parts.


### Map Reduce Implementation
All the simulation schemes represent an implementation of map reduce. 
From scheme one to scheme five, we deal with a single data center with variations in VM scheduling and Cloudlet allocation. 
In the others, where multiple data centers are used, one data center uses an entirely space shared sheme and the other uses a time shared scheme. Among the multi data center schemes, one implementation also works with a networked data center.   
  
From the execution patterns, it's observed that cloudlet scheduling drives the variation in execution patterns. These have been included in the ```/charts``` for each scheme and only shows the pattern for a scaled - down version with lesser number of cloudlets compared to the actual simulation results.

