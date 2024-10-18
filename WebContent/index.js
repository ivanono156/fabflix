/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_list_body");

    movieTableBodyElement.empty();
    // Iterate through resultData, no more than 20 entries
    //resultData is the jsonArray of movie objects
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        //add link the sing-movie page in the title column
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>" + resultData[i]["movie_title"] + "</a></td>";

        //adding in the rest of the column data
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_genres"] + "</td>";
        rowHTML += "<td>" +
            "<ul>" +
            "<li><a href='single-star.html?id=" + resultData[i]["movie_star1_id"] + "'>" + resultData[i]["movie_star1"] + "</a></li>" +
            "<li><a href='single-star.html?id=" + resultData[i]["movie_star2_id"] + "'>" + resultData[i]["movie_star2"] + "</a></li>" +
            "<li><a href='single-star.html?id=" + resultData[i]["movie_star3_id"] + "'>" + resultData[i]["movie_star3"] + "</a></li>" +
            "</ul>" +
            "</td>";
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";
        rowHTML += "" +
            "<td>" +
            "<form action='api/shopping-cart' target='_self' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' id='movie-id' name='id' value='" + resultData[i]["movie_id"] + "'>" +
                "<input type='hidden' id='movie-title' name='title' value='" + resultData[i]["movie_title"] + "'>" +
                "<input type='submit' VALUE='Add to Cart'>" +
            "</form>" +
        "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});