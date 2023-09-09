# Discover Instance Spanning Exceptions from process execution logs - Web Service
The web service enables the discovery of instance spanning exceptions (ISE), i.e. exceptions in processes that span multiple process instances. There are five different classes of ISE, wait, cancel, redo, change and rework. This web service allows to discover these ISE based on procss event logs. The implementation is based on the algorithms developed in [Link](https://doi.org/10.1109/CBI54897.2022.10048) as well as the visualizations and process performance indicators for ISE developed in my bachelor thesis. 

## Usage

 There are two ways to use the web service:

1) The web service is accessible via the link https://csl.bpm.in.tum.de/ise-web-service

2) The web service can be used locally. For this purpose, the JAR file provided in this project should be started with the following command:
   ```java -jar ISEWebService-0.0.0.1.jar```
  Afterwards, the web service can be called via localhost:8011. Please note that Java Verison 17 is required to run this command.

## Technologies
The project is based on the Spring Boot Application Framework and uses several additional libraries and dependencies
