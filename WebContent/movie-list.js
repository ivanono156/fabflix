/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
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

function handleAddToCartResult(resultData) {
    alert("Successfully added movie to cart!")
    console.log("Added movie to cart successfully");
    console.log(resultData);
}

function handleCartError() {
    alert("Could not add movie to cart!")
    console.log("could not add movie to cart");
}

function addToCart(submitEvent) {
    console.log("adding movie to shopping cart");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart",
        data: $(this).serialize(),
        success: (resultData) => handleAddToCartResult(resultData),
        error: handleCartError
    });
}


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

        let genresHTML = "<td><ul>";
        let genres = resultData[i]["genres"];
        for (const genreId in genres) {
            let genreName = genres[genreId]
            genresHTML += "<li><a href='movie-list.html?gid=" + genreId + "'>" + genreName + "</a></li>";
        }
        genresHTML += "</ul></td>";

        //add link the sing-movie page in the title column
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>" + resultData[i]["movie_title"] + "</a></td>";

        //adding in the rest of the column data
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += genresHTML;
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
            "<form action='#' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' name='id' value='" + resultData[i]["movie_id"] + "'>" +
                // value of 1 means to increment quantity (per ShoppingCartServlet.java)
                "<input type='hidden' name='quantity' value='1'>" +
                "<input type='submit' VALUE='Add to Cart'>" +
            "</form>" +
        "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    // Bind add to cart function to all add to cart buttons
    $(".add-to-cart-btn").submit(addToCart);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let searchParams = {
    pagenumber: getSessionPageNumber(),
    display: getSessionDisplay()
}

let genreId = getParameterByName("gid");
// let genreId = getParameterByName(genreKeyName)
if (genreId != null) {
    searchParams["gid"] = genreId;  // add gid parameter to url
    sessionStorage.setItem("gid", genreId);
} else {
    sessionStorage.removeItem("gid");
}

let titleStartsWith = getParameterByName("title-starts-with");
if (titleStartsWith != null) {
    searchParams["title-starts-with"] = titleStartsWith;  // add gid parameter to url
    sessionStorage.setItem("title-starts-with", titleStartsWith);
} else {
    sessionStorage.removeItem("title-starts-with")
}
/*
let titleId = getParameterByName("title_entry");
if (titleId != null) {
    searchParams["title_entry"] = titleId;  // add gid parameter to url
}
*/
let title_name = getParameterByName("title_entry");
if (title_name != null && title_name !== "") {
    searchParams["title_entry"] = title_name;
    sessionStorage.setItem("title_entry", title_name);
} else {
    sessionStorage.removeItem("title_entry")
}

let year = getParameterByName("year_entry");
if (year != null && year !== "") {
    searchParams["year_entry"] = year;
    sessionStorage.setItem("year_entry", year);
} else {
    sessionStorage.removeItem("year_entry")
}

let director_name = getParameterByName("director_entry");
if (director_name != null && director_name !== "") {
    searchParams["director_entry"] = director_name;
    sessionStorage.setItem("director_entry", director_name);
} else {
    sessionStorage.removeItem("director_entry")
}

let star_name = getParameterByName("star_entry");
if (star_name != null && star_name !== "") {
    searchParams["star_entry"] = star_name;
    sessionStorage.setItem("star_entry", star_name);
} else {
    sessionStorage.removeItem("star_entry")
}

if(title_name != null || year != null || director_name != null || star_name != null){
    console.log("search page servlet executed");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/search-page", // Setting request url
        data: searchParams, // Setting search query data
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully
    });
}

else{
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movie-list", // Setting request url
        data: searchParams, // Setting search query data
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully
    });
}
