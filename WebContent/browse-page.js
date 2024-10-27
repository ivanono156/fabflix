function handleBrowseResult(resultData) {
    console.log("handleBrowseResult: populating genre from resultData");

    let genres_Table_Element = jQuery("#genres_grid");

    let genres = resultData["genres"];
    for (let i = 0; i < genres.length; i++) {
        let genreRowHTML = "<div class='grid-item'>"
            + "<a href='movie-list.html?page-number=1&display=" + getSessionDisplay()
            + "&gid=" + genres[i]["genre_id"] + "'>" + genres[i]["genre_name"] + "</a>"
            + "</div>";
        genres_Table_Element.append(genreRowHTML);
    }

    console.log("handleBrowseResult: populating title from resultData");

    let titles_Table_Element = jQuery("#titles_grid");

    let titles = resultData["titles"];
    for (let i = 0; i < titles.length; i++) {
        let titlesRowHTML = "<div class='grid-item'>"
            + "<a href='movie-list.html?page-number=1&display=" + getSessionDisplay()
            + "&title-starts-with=" + titles[i]["title_id"] + "'>" + titles[i]["title_name"] + "</a>"
            + "</div>";
        titles_Table_Element.append(titlesRowHTML);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse-page",
    success: (resultData) => handleBrowseResult(resultData)
});