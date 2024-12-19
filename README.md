# Fabflix Movies - Database & Web Application

### Ivan Onofre & Carlos Arias

* Built over the course of 12 weeks
* Full stack architecture
* Deployed using Apache Tomcat and then Kubernetes on AWS instances
* Developed an ETL pipeline to parse large XML files to expand the database

#### App Description
Fabflix is a web app that allows users to browse from thousands of movies.
Users can log into their account, add movies to their cart, and purchase them 
at checkout. They can browse by genre and by title, or they can search for their
favorite movies. 

#### Technologies Used
The frontend is built using JavaScript, jQuery, and AJAX. Java and JDBC are used
to connect to and communicate with the MySQL database that contains all the data
for the Fabflix app. The application is deployed using kubernetes to handle multiple
AWS instances.

#### Using Fabflix
You can log in to Fabflix using the test email 

`a@email.com` 

and its associated password

`a2`. 

From the home page, you can select the browse page or the search page. You can browse 
from the 1000+ catalog of movies by genre or by name, or you can search for a specific 
movie. You can add movies to your cart and check them out, which will take you to a page 
where you can place your order. To check out, you can enter the test information

```
First Name: a
Last Name: a
Credit Card Number: 941
Date of Expiration: 11-01-2005
```