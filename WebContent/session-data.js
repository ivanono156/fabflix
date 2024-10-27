// used in all movie list pages
const pageNumberKeyName = "page-number";
const displayKeyName = "display";
const titleSortOrderKeyName = "sort-by-title";
const ratingSortOrderKeyName = "sort-by-rating"
// browse page parameters
const genreKeyName = "gid";
const titleStartsWithKeyName = "title-starts-with";
// search page parameters
const searchByTitleKeyName = "title_entry";
const searchByYearKeyName = "year_entry";
const searchByDirectorKeyName = "director_entry";
const searchByStarKeyName = "star_entry";
const searchBySortField = "sort_field";
const searchBySortOrder = "sort_order";

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

function getSessionTitleSortOrder() {
    let titleSortOrder = sessionStorage.getItem(titleSortOrderKeyName);
    if (titleSortOrder == null) {
        titleSortOrder = "asc";
        sessionStorage.setItem(titleSortOrderKeyName, titleSortOrder);
    }
    return titleSortOrder;
}

function getSessionRatingSortOrder() {
    let ratingSortOrder = sessionStorage.getItem(ratingSortOrderKeyName);
    if (ratingSortOrder == null) {
        ratingSortOrder = "desc";
        sessionStorage.setItem(ratingSortOrderKeyName, ratingSortOrder);
    }
    return ratingSortOrder;
}

function getBrowseByGenreSessionData() {
    return sessionStorage.getItem(genreKeyName);
}

function getTitleStartsWithSessionData() {
    return sessionStorage.getItem(titleStartsWithKeyName);
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

function getSearchBySortFieldSessionData(){
    return sessionStorage.getItem(searchBySortField);
}

function getSearchBySortOrderSessionData(){
    return sessionStorage.getItem(searchBySortOrder);
}


function createUrlParams(key, value) {
    return key + "=" + value;
}

function getSessionDataAsUrl() {
    let pageNumber = getSessionPageNumber();
    let display = getSessionDisplay();
    let titleSortOrder = getSessionTitleSortOrder();
    let ratingSortOrder = getSessionRatingSortOrder();

    let urlString = createUrlParams(pageNumberKeyName, pageNumber)
        + "&" + createUrlParams(displayKeyName, display)
        + "&" + createUrlParams(titleSortOrderKeyName, titleSortOrder)
        + "&" + createUrlParams(ratingSortOrderKeyName, ratingSortOrder);

    let genreId = getBrowseByGenreSessionData();
    let titleStartsWith = getTitleStartsWithSessionData();

    if (genreId != null) {
        urlString += "&" + createUrlParams(genreKeyName, genreId);
    } else if (titleStartsWith != null) {
        urlString += "&" + createUrlParams(titleStartsWithKeyName, titleStartsWith);
    } else {
        // search page session data
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

        let sortField = getSearchBySortFieldSessionData();
        if(sortField != null) {
            urlString += "&" + createUrlParams(searchBySortField, sortField);
        }

        let sortOrder = getSearchBySortOrderSessionData();
        if(sortOrder != null){
            urlString += "&" + createUrlParams(searchBySortOrder, sortOrder);
        }
    }

    return urlString;
}

function createDefaultMovieListUrl() {
    return createUrlParams(pageNumberKeyName, 1)
        + "&" + createUrlParams(displayKeyName, getSessionDisplay())
        + "&" + createUrlParams(titleSortOrderKeyName, getSessionTitleSortOrder())
        + "&" + createUrlParams(ratingSortOrderKeyName, getSessionRatingSortOrder());
}