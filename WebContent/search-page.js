/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
// Ensure the script runs after the DOM is fully loaded

document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("search-page-searches");

    form.addEventListener("submit", function(event) {
        event.preventDefault();


        const title = form.elements['title_entry'].value;
        const year= form.elements['year_entry'].value;
        const director = form.elements['director_entry'].value;
        const star = form.elements['star_entry'].value;

        let redirectUrl = 'movie-list.html?page-number=1&display=' + getSessionDisplay() + '&';
        // Construct the redirect URL with query parameters
        if (title) {
            redirectUrl += `title_entry=${encodeURIComponent(title)}&`;
        }
        if (year) {
            redirectUrl += `year_entry=${encodeURIComponent(year)}&`;
        }
        if (director) {
            redirectUrl += `director_entry=${encodeURIComponent(director)}&`;
        }
        if (star) {
            redirectUrl += `star_entry=${encodeURIComponent(star)}&`;
        }
        if (redirectUrl.endsWith('&')) {
            redirectUrl = redirectUrl.slice(0, -1);
        }
        // Redirect to the constructed URL
        window.location.href = redirectUrl;
    });
});