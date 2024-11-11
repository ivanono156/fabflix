# CS 122B Fall 2024 Project - Team 997

## Project 3

Demo Video URL: https://youtu.be/I6DXhdB92VA

## Contributions

Ivan Onofre
- Task 1
- Task 3
- Task 5 - Add star (html, js, java)
- Task 6

Carlos Arias
- Task 2
- Task 4
- Task 5 - Add movie, metadata, employee login pages

### Filenames with Prepared Statements
- [AddStarServlet.java](src/AddStarServlet.java)
- [BrowsePageServlet.java](src/BrowsePageServlet.java)
- [BrowsePageTitleServlet.java](src/BrowsePageTitleServlet.java)
- [EmployeeLoginServlet.java](src/EmployeeLoginServlet.java)
- [LoginServlet.java](src/LoginServlet.java)
- [MovieListServlet.java](src/MovieListServlet.java)
- [OrderConfirmationServlet.java](src/OrderConfirmationServlet.java)
- [PaymentServlet.java](src/PaymentServlet.java)
- [SearchPageServlet.java](src/SearchPageServlet.java)
- [ShoppingCartServlet.java](src/ShoppingCartServlet.java)
- [SingleMovieServlet.java](src/SingleMovieServlet.java)
- [SingleStarServlet.java](src/SingleStarServlet.java)

### Two parsing time optimization strategies compared with the naive approach.
1. Batch inserts
2. In-memory hashmap holding:
   - movie info
   - star info
   - star_in_movie relations
   
- Naive approach: ~8 minutes (local machine)
- Optimized approach ~ 3 minutes (local machine)

### Inconsistent data reports from parsing
    16311 stars inserted.
    8886 movies inserted.
    69 genres inserted.
    9862 genres in movies inserted.
    46703 stars in movies inserted.
    53 movies inconsistent
    54 movies duplicate
    3122 movies have no stars
    2186 stars duplicate
    1010 movies not found