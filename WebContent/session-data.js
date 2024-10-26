const pageNumberKeyName = "pagenumber";
const displayKeyName = "display";
const genreKeyName = "gid";
const titleStartsWithKeyName = "title-starts-with";


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

function getSessionGenreId() {
    return sessionStorage.getItem(genreKeyName);
}

function getSessionTitleStartsWith() {
    return sessionStorage.getItem(titleStartsWithKeyName);
}

function createUrlParams(key, value) {
    return key + "=" + value;
}

function getSessionDataAsUrl() {
    let sessionPageNumber = getSessionPageNumber();
    let sessionDisplay = getSessionDisplay();
    let sessionGenreId = getSessionGenreId();
    let sessionTitleStartsWith = getSessionTitleStartsWith();

    let urlString = createUrlParams(pageNumberKeyName, sessionPageNumber) + "&" + createUrlParams(displayKeyName, sessionDisplay)

    if (sessionGenreId != null) {
        urlString += "&" + createUrlParams(genreKeyName, sessionGenreId);
    } else if (sessionTitleStartsWith != null) {
        urlString += "&" + createUrlParams(titleStartsWithKeyName, sessionTitleStartsWith);
    }

    return urlString;
}