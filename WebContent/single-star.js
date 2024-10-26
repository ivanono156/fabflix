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


/**
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
    document.title = resultData["star_name"];

    console.log("handleResult: populating star info from resultData");

    // find the empty h1 body by id "star_name"
    let starNameElement = jQuery("#star_name");

    // add the star name to the h1 element
    starNameElement.append("<p>" + resultData["star_name"] + "</p>");

    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Date Of Birth: " + resultData["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Json array containing movies this star acted in
    const starMovies = resultData["star_movies"];

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < starMovies.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>"
            // Add a link to each single-movie.html page
            + '<a href="single-movie.html?id=' + starMovies[i]["movie_id"] + '">'
            + starMovies[i]["movie_title"] // Display movie title as link text
            + '</a>'
            + "</td>";
        rowHTML += "<td>" + starMovies[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + starMovies[i]["movie_director"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    let backToMovieListElement = $("#back-to-movie-list");

    let href = "movie-list.html?" + getSessionDataAsUrl();

    backToMovieListElement.append('<a href="' + href + '" class="back-btn">Back to Movies List</a>');
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});