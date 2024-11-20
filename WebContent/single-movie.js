/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it knows which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/*
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // Setting webpage title
    document.title = resultData["movie_title"];

    console.log("handleResult: populating movie info from resultData");

    // find the empty h1 body by id "movie_title"
    let movieTitleElement = jQuery("#movie_title");

    // add the movie title to the h1 element
    movieTitleElement.append("<p>" + resultData["movie_title"] + "</p>");

    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Release Year: " + resultData["movie_year"] + "</p>" +
        "<p>Director: " + resultData["movie_director"] + "</p>" +
        "<p>Rating: " + resultData["movie_rating"] + "</p>");

    console.log("handleResult: populating genre table from resultData");

    // Find the empty table body by id "genre_table_body"
    let genreTableBodyElement = jQuery("#genre_table_body");

    // Json array containing this movie's genres
    const genres = resultData["movie_genres"];

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (const genreId in genres) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>"
            // Add a link to browse by genre
            + '<a href="movie-list.html?' + createDefaultMovieListUrl() + '&' + createUrlParams(genreKeyName, genreId) + '">'
            + genres[genreId] // Display genre name
            + '</a>'
            + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        genreTableBodyElement.append(rowHTML);
    }

    console.log("handleResult: populating star table from resultData");

    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Json array containing movies this star acted in
    const stars = resultData["movie_stars"];

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (const starId in stars) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + '<a href="single-star.html?id=' + starId + '">' + stars[starId] + '</a>' + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }

    let movieDetailsDivElement = $("#movie-details");
    movieDetailsDivElement.append("" +
        "<input type='hidden' name='id' value='" + resultData["movie_id"] + "'>" +
        // value of 1 means to increment quantity (per ShoppingCartServlet.java)
        "<input type='hidden' name='quantity' value='1'>");

    let backToMovieListElement = $("#back-to-movie-list");

    let href = "movie-list.html?" + getSessionDataAsUrl();

    backToMovieListElement.append('<a href="' + href + '" class="back-btn">Back to Movies List</a>');
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});