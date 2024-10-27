function handleBrowseResult(resultData) {
    console.log("handleBrowseResult: populating genre from resultData");

    let genres = resultData["genres"];

    let genres_Table_Element = jQuery("#genres_grid");

    for (let i = 0; i < genres.length; i++) {
        let genreRowHTML = "";
        genreRowHTML += "<div class='grid-item'>"
            + "<a href='movie-list.html?pagenumber=1&display=" + getSessionDisplay()
            + "&gid=" + genres[i]["genreId"] + "'>" + genres[i]["genreName"] + "</a>"
            + "</div>";

        genres_Table_Element.append(genreRowHTML);
    }

    console.log("handleBrowseResult: populating title from resultData");

    let titles = resultData["titles"];

    let titles_Table_Element = jQuery("#titles_grid");

    let titlesRowHTML = "";
    for (let i = 0; i < titles.length; i++) {
        titlesRowHTML += "<div class='grid-item'>"
            + "<a href='movie-list.html?pagenumber=1&display=" + getSessionDisplay()
            + "&title-starts-with=" + titles[i] + "'>" + titles[i] + "</a>"
            + "</div>";
    }

    // add the "*" character too
    titlesRowHTML += "<div class='grid-item'>"
        + "<a href='movie-list.html?pagenumber=1&display=" + getSessionDisplay() + "&title-starts-with=non-alnum'>*</a>"
        + "</div>";

    titles_Table_Element.append(titlesRowHTML);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse-page",
    success: (resultData) => handleBrowseResult(resultData)
});