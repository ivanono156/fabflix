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
function handleBrowseResult(resultData) {
    console.log("handleBrowseResult: populating genre and title table from resultData");

    // Populate the movie table
    // Find the empty table body by id "star_table_body"
    let genres_Table_Element = jQuery("#genre_table_body");
    let titles_Table_Element = jQuery("#titles_table_body");

    genres_Table_Element.empty();

    let genreRowHTML = "";
    genreRowHTML += "<tr>";
    // Iterate through resultData, number of genres 23
    for (let i = 0; i < resultData.length; i++) {
        genreRowHTML += "<td><a href='movie-list.html?pagenumber=1&display=50gid=" + resultData[i]["genreId"] + "'>" + resultData[i]["genreName"] + "</a></td>";
    }
    genreRowHTML += "</tr>";

    //append the row to the body
    genres_Table_Element.append(genreRowHTML);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse-page", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleBrowseResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});