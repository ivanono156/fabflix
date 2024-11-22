# General
- #### Team#: 997

- #### Names: Ivan Onofre, Carlos Arias

- #### Project 4 Video Demo Link:

- #### Instruction of deployment:

- #### Collaborations and Work Distribution:


# Connection Pooling
  #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    
  #### Explain how Connection Pooling is utilized in the Fabflix code.
  #### We use connection pooling by reusing database connections in our serlvets when a request is made. We use this for load balancing between, the master and slave instances that we created. This helps to distribute tasks between the two servers.
  #### Explain how Connection Pooling works with two backend SQL.
  #### Connection Poolling works by pooling all read/write requests and connecting them to the primary database. All of the read requests get pooled together and get connected to the secondary database.
    

# Master/Slave
#### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
  - [src/AddMovieServlet.java](src/AddMovieServlet.java)
  - [src/AddStarServlet.java](src/AddStarServlet.java)
  - [src/AutoCompleteServlet.java](src/AutoCompleteServlet.java)
  - [src/BrowsePageServlet.java](src/BrowsePageServlet.java)
  - [src/EmployeeLoginServlet.java](src/EmployeeLoginServlet.java)
  - [src/LoginServlet.java](src/LoginServlet.java)
  - [src/MetaDataServlet.java](src/MetaDataServlet.java)
  - [src/MovieListServlet.java](src/MovieListServlet.java)
  - [src/OrderConfirmationServlet.java](src/OrderConfirmationServlet.java)
  - [src/PaymentServlet.java](src/PaymentServlet.java)
  - [src/SearchPageServlet.java](src/SearchPageServlet.java)
  - [src/ShoppingCartServlet.java](src/ShoppingCartServlet.java)
  - [src/SingleMovieServlet.java](src/SingleMovieServlet.java)
  - [src/SingleStarServlet.java](src/SingleStarServlet.java)

#### How read/write requests were routed to Master/Slave SQL?
Two data sources are created in the context.xml file: read/write operations are sent to "jdbc/moviedbReadWrite", and 
read-only operations are sent to "jdbc/moviedbReadOnly". These two resources are registered in the web.xml file under 
the same name. The read/write data source contains a connection to the aws instance that has the master mysql on it, and 
the read-only data source contains a connection to the aws instance that has the slave mysql on it.

USE TERM "Gond" TO DEMONSTRATE FUZZY SEARCH

# Extra Credit - Fuzzy Search
####  Include a brief explanation of the design and the implementation of your fuzzy Search in README.
