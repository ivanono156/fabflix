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

function updateMovieList() {
    window.location.href = "movie-list.html?" + createUrlParams(pageNumberKeyName, pageNumber)
        + "&" + createUrlParams(displayKeyName, displayOptionElement.val())
        + "&" + createUrlParams(sortOrderKeyName, sortOrderOptionElement.val())
        + getSearchQueriesAsUrl();
}

function changePage(newPageNumber) {
    window.location.href = "movie-list.html?" + createUrlParams(pageNumberKeyName, newPageNumber)
        + "&" + createUrlParams(displayKeyName, getParameterByName(displayKeyName))
        + "&" + createUrlParams(sortOrderKeyName, getParameterByName(sortOrderKeyName))
        + getSearchQueriesAsUrl();
}

function handleMovieResult(resultData) {
    if (resultData.length === 0) {
        $("#no-results-message").append("<h3>No results found.</h3>");
        return;
    }

    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    let movieTableBodyElement = jQuery("#movie_list_body");

    movieTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["movie_id"] + "'>" + resultData[i]["movie_title"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";

        let genresHTML = "<td><ul>";
        let genres = resultData[i]["genres"];
        for (let i = 0; i < genres.length; i++) {
            genresHTML += "<li><a href='movie-list.html?" + createDefaultMovieListUrl()
                + "&" + createUrlParams(genreKeyName, genres[i]["id"]) + "'>" + genres[i]["name"] + "</a></li>";
        }
        genresHTML += "</ul></td>";
        rowHTML += genresHTML;

        let starsHTML = "<td><ul>";
        let stars = resultData[i]["stars"];
        for (let i = 0; i < stars.length; i++) {
            starsHTML += "<li><a href='single-star.html?id=" + stars[i]["id"] + "'>" + stars[i]["name"] + "</a></li>";
        }
        starsHTML += "</ul></td>"
        rowHTML += starsHTML;

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

    sortOrderOptionElement.val(getParameterByName(sortOrderKeyName));

    displayOptionElement.val(getParameterByName(displayKeyName));

    let changePageButtonElement = $("#change-page-button");

    let currentPageNumber = Number(getParameterByName(pageNumberKeyName));

    if (currentPageNumber > 1) {
        let prevButtonHTML = "<button id='prev-button'>Previous</button>";
        changePageButtonElement.append(prevButtonHTML);
        $("#prev-button").on("click", function() {
            changePage(currentPageNumber - 1);
        });
    }

    changePageButtonElement.append("<span>Current Page: " + currentPageNumber + "</span>");

    if (resultData.length == getParameterByName(displayKeyName)) {
        let nextButtonHTML = "<button id='next-button'>Next Page</button>";
        changePageButtonElement.append(nextButtonHTML);
        $("#next-button").on("click", function() {
            changePage(currentPageNumber + 1);
        });
    }
}

let displayOptionElement = $("#display");
displayOptionElement.on("change", function() {
    updateMovieList();
})

let sortOrderOptionElement = $("#sort-order");
sortOrderOptionElement.on("change", function() {
    updateMovieList();
})

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

// Pagination params
setSearchParamData(pageNumberKeyName);
setSearchParamData(displayKeyName);
setSearchParamData(sortOrderKeyName)
// Browse params
setSearchParamData(genreKeyName);
setSearchParamData(titleStartsWithKeyName);
// Search params
setSearchParamData(searchQueryKeyName);
setSearchParamData(searchByTitleKeyName);
setSearchParamData(searchByYearKeyName);
setSearchParamData(searchByDirectorKeyName);
setSearchParamData(searchByStarKeyName);

let searchQuery = getParameterByName(searchQueryKeyName)
let title_name = getParameterByName(searchByTitleKeyName);
let year = getParameterByName(searchByYearKeyName);
let director_name = getParameterByName(searchByDirectorKeyName);
let star_name = getParameterByName(searchByStarKeyName);

let requestUrl = "movie-list";
if(searchQuery !== null){
    requestUrl = "search-page";
}

console.log(requestUrl + " servlet executed");
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/" + requestUrl,
    data: searchParams,
    success: (resultData) => handleMovieResult(resultData)
});