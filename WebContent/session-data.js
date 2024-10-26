// used in all movie list pages
const pageNumberKeyName = "pagenumber";
const displayKeyName = "display";
// browse page parameters
const genreKeyName = "gid";
const titleStartsWithKeyName = "title-starts-with";
// search page parameters
const searchByTitleKeyName = "title_entry";
const searchByYearKeyName = "year_entry";
const searchByDirectorKeyName = "director_entry";
const searchByStarKeyName = "star_entry";


function getSessionPageNumber() {
    let sessionPageNumber = sessionStorage.getItem(pageNumberKeyName)
    if (sessionPageNumber == null) {
        sessionPageNumber = 1;
        sessionStorage.setItem(pageNumberKeyName, sessionPageNumber.toString());
    }
    return parseInt(sessionPageNumber);
}

function getSessionDisplay() {
    let sessionDisplay = sessionStorage.getItem(displayKeyName)
    if (sessionDisplay == null) {
        sessionDisplay = 100;
        sessionStorage.setItem(displayKeyName, sessionDisplay.toString());
    }
    return parseInt(sessionDisplay);
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

function setParamSessionData (keyName, searchParams) {
    let value = getParameterByName(keyName);
    if (value != null && value !== "") {
        searchParams[keyName] = value;
        sessionStorage.setItem(keyName, value);
    } else {
        sessionStorage.removeItem(keyName);
    }
    return searchParams;
}

function createUrlParams(key, value) {
    return key + "=" + value;
}

function getSessionDataAsUrl() {
    let sessionPageNumber = getSessionPageNumber();
    let sessionDisplay = getSessionDisplay();
    let sessionGenreId = getBrowseByGenreSessionData();
    let sessionTitleStartsWith = getTitleStartsWithSessionData();

    let urlString = createUrlParams(pageNumberKeyName, sessionPageNumber) + "&" + createUrlParams(displayKeyName, sessionDisplay)

    if (sessionGenreId != null) {
        urlString += "&" + createUrlParams(genreKeyName, sessionGenreId);
    } else if (sessionTitleStartsWith != null) {
        urlString += "&" + createUrlParams(titleStartsWithKeyName, sessionTitleStartsWith);
    } else {
        // search page session data
        let title = getSearchByTitleSessionData();
        if (title != null) {
            urlString += "&" + createUrlParams(searchByTitleKeyName, title);
        }

        let year = getSearchByYearSessionData()
        if (year != null) {
            urlString += "&" + createUrlParams(searchByYearKeyName, year);
        }

        let director = getSearchByDirectorSessionData();
        if (director != null) {
            urlString += "&" + createUrlParams(searchByDirectorKeyName, director);
        }

        let star = getSearchByStarSessionData()
        if (star != null) {
            urlString += "&" + createUrlParams(searchByStarKeyName, star);
        }
    }

    return urlString;
}