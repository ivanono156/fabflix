// used in all movie list pages
const pageNumberKeyName = "page-number";
const displayKeyName = "display";
const sortOrderKeyName = "sort-order";
// browse page parameters
const genreKeyName = "gid";
const titleStartsWithKeyName = "title-starts-with";
// search page parameters
const searchQueryKeyName = "search-query";
const searchByTitleKeyName = "title_entry";
const searchByYearKeyName = "year_entry";
const searchByDirectorKeyName = "director_entry";
const searchByStarKeyName = "star_entry";

function getSessionPageNumber() {
    let pageNumber = sessionStorage.getItem(pageNumberKeyName)
    if (pageNumber == null) {
        pageNumber = 1;
        sessionStorage.setItem(pageNumberKeyName, pageNumber.toString());
    }
    return parseInt(pageNumber);
}

function getSessionDisplay() {
    let display = sessionStorage.getItem(displayKeyName)
    if (display == null) {
        display = 25;
        sessionStorage.setItem(displayKeyName, display.toString());
    }
    return parseInt(display);
}

function getSessionSortOrder() {
    let sortOrder = sessionStorage.getItem(sortOrderKeyName);
    if (sortOrder == null) {
        sortOrder = "rating desc, title asc";
        sessionStorage.setItem(sortOrderKeyName, sortOrder);
    }
    return sortOrder;
}

function getBrowseByGenreSessionData() {
    return sessionStorage.getItem(genreKeyName);
}

function getTitleStartsWithSessionData() {
    return sessionStorage.getItem(titleStartsWithKeyName);
}

function getSearchQuerySessionData() {
    return sessionStorage.getItem(searchQueryKeyName);
}

function getSearchByTitleSessionData() {
    return sessionStorage.getItem(searchByTitleKeyName);
}

function getSearchByYearSessionData() {
    return sessionStorage.getItem(searchByYearKeyName);
}

function getSearchByDirectorSessionData() {
    return sessionStorage.getItem(searchByDirectorKeyName);
}

function getSearchByStarSessionData() {
    return sessionStorage.getItem(searchByStarKeyName);
}

function createUrlParams(key, value) {
    return key + "=" + value;
}

function getSearchQueriesAsUrl() {
    let urlString = "";
    let genreId = getBrowseByGenreSessionData();
    let titleStartsWith = getTitleStartsWithSessionData();

    if (genreId != null) {
        urlString += "&" + createUrlParams(genreKeyName, genreId);
    } else if (titleStartsWith != null) {
        urlString += "&" + createUrlParams(titleStartsWithKeyName, titleStartsWith);
    } else {
        // search page session data
        let searchQuery = getSearchQuerySessionData();
        if (searchQuery != null) {
            urlString += "&" + createUrlParams(searchQueryKeyName, searchQuery);
        }

        let title = getSearchByTitleSessionData();
        if (title != null) {
            urlString += "&" + createUrlParams(searchByTitleKeyName, title);
        }

        let year = getSearchByYearSessionData();
        if (year != null) {
            urlString += "&" + createUrlParams(searchByYearKeyName, year);
        }

        let director = getSearchByDirectorSessionData();
        if (director != null) {
            urlString += "&" + createUrlParams(searchByDirectorKeyName, director);
        }

        let star = getSearchByStarSessionData();
        if (star != null) {
            urlString += "&" + createUrlParams(searchByStarKeyName, star);
        }
    }

    return urlString;
}

function getSessionDataAsUrl() {
    let pageNumber = getSessionPageNumber();
    let display = getSessionDisplay();
    let sortOrder = getSessionSortOrder();

    let urlString = createUrlParams(pageNumberKeyName, pageNumber)
        + "&" + createUrlParams(displayKeyName, display)
        + "&" + createUrlParams(sortOrderKeyName, sortOrder);

    let searchQueryUrlString = getSearchQueriesAsUrl();

    return urlString + searchQueryUrlString;
}

function createDefaultMovieListUrl() {
    return createUrlParams(pageNumberKeyName, 1)
        + "&" + createUrlParams(displayKeyName, getSessionDisplay())
        + "&" + createUrlParams(sortOrderKeyName, getSessionSortOrder());
}