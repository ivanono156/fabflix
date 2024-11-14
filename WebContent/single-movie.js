function getParameterByName(target) {
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

function handleResult(resultData) {

    document.title = resultData["movie_title"];

    console.log("handleResult: populating movie info from resultData");
    console.log(resultData);

    let movieTitleElement = jQuery("#movie_title");

    movieTitleElement.append("<p>" + resultData["movie_title"] + "</p>");

    let movieInfoElement = jQuery("#movie_info");

    movieInfoElement.append("<p>Release Year: " + resultData["movie_year"] + "</p>" +
        "<p>Director: " + resultData["movie_director"] + "</p>" +
        "<p>Rating: " + resultData["movie_rating"] + "</p>");

    console.log("handleResult: populating genre table from resultData");

    let genreTableBodyElement = jQuery("#genre_table_body");

    const genres = resultData["movie_genres"];

    for (let i = 0; i < genres.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>"
            + '<a href="movie-list.html?' + createDefaultMovieListUrl()
            + '&' + createUrlParams(genreKeyName, genres[i]["genre_id"]) + '">'
            + genres[i]["genre_name"]
            + '</a>'
            + "</td>";
        rowHTML += "</tr>";

        genreTableBodyElement.append(rowHTML);
    }

    console.log("handleResult: populating star table from resultData");

    let starTableBodyElement = jQuery("#star_table_body");

    const stars = resultData["movie_stars"];

    for (let i = 0; i < stars.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + '<a href="single-star.html?id=' + stars[i]["star_id"] + '">'
            + stars[i]["star_name"] + '</a>' + "</td>";
        rowHTML += "</tr>";

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

let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});