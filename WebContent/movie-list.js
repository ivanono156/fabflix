/*
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

function setSearchParamData (keyName) {
    let value = getParameterByName(keyName);
    if (value != null && value !== "") {
        searchParams[keyName] = value;
        sessionStorage.setItem(keyName, value);
    } else {
        sessionStorage.removeItem(keyName);
    }
}

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
    let movieTableBodyElement = jQuery("#movie_list_body");

    movieTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        let genresHTML = "<td><ul>";
        let genres = resultData[i]["genres"];
        for (const genreId in genres) {
            let genreName = genres[genreId]
            genresHTML += "<li><a href='movie-list.html?page-number=1&display=" + getSessionDisplay() + "&gid=" + genreId + "'>" + genreName + "</a></li>";
        }
        genresHTML += "</ul></td>";

        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>" + resultData[i]["movie_title"] + "</a></td>";

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

        movieTableBodyElement.append(rowHTML);
    }

    $(".add-to-cart-btn").submit(addToCart);
}


// Once this .js is loaded, following scripts will be executed by the browser
let searchParams = {}

let pageNumber = getParameterByName(pageNumberKeyName);
if (pageNumber == null) {
    console.log("movie-list.js: page number is missing from url!");
}

let displayAmount = getParameterByName(displayKeyName);
if (displayAmount == null) {
    console.log("movie-list.js: display amount is missing from the url!");
}

setSearchParamData(pageNumberKeyName);
setSearchParamData(displayKeyName);
setSearchParamData(genreKeyName);
setSearchParamData(titleStartsWithKeyName);
setSearchParamData(searchByTitleKeyName);
setSearchParamData(searchByYearKeyName);
setSearchParamData(searchByDirectorKeyName);
setSearchParamData(searchByStarKeyName);
setSearchParamData(searchBySortField);
setSearchParamData(searchBySortOrder);
setSearchParamData(searchBySortField2);
setSearchParamData(searchBySortOrder2);

let title_name = getParameterByName(searchByTitleKeyName);
let year = getParameterByName(searchByYearKeyName);
let director_name = getParameterByName(searchByDirectorKeyName);
let star_name = getParameterByName(searchByStarKeyName);

//for browsepage reads the letter the user selected in the url
let search_letter = getParameterByName("search_title_entry");

if(title_name != null || year != null || director_name != null || star_name != null){
    console.log("search page servlet executed");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/search-page", // Setting request url
        data: searchParams, // Setting search query data
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully
    });
} else if(search_letter != null){
    console.log("Browse page title servlet executed");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/browse-page-title", // Setting request url
        data: searchParams, // Setting search query data
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully
    });
} else{
    console.log("movie list servlet executed");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movie-list", // Setting request url
        data: searchParams, // Setting search query data
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully
    });
}
