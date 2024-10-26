let search_form = $("#search-page-searches");

/*
function handleLoginResult(resultData) {

    console.log("handle login response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultData["status"] === "success") {
        window.location.replace("index.html");
    } else {
            // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultData["message"]);
        $("#login_error_message").text(resultData["message"]);
    }
}
*/
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
// Ensure the script runs after the DOM is fully loaded

document.addEventListener("DOMContentLoaded", function() {
    // Get the form element by its ID (replace 'searchForm' with your actual form ID)
    const form = document.getElementById("search-page-searches");

    form.addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent the default form submission

        const formData = new FormData(form);
        const title = formData.get('title_entry');
        const year= formData.get('year_entry');
        const director = formData.get('director_entry');
        const star = formData.get('star_entry');

        let redirectUrl = 'movie-list.html?'
        // Construct the redirect URL with query parameters

        if (title) {
            redirectUrl += `title_entry=${encodeURIComponent(title)}&`;
        }
        if (year) {
            redirectUrl += `title_entry=${encodeURIComponent(year)}&`;
        }
        if (director) {
            redirectUrl += `title_entry=${encodeURIComponent(director)}&`;
        }
        if (star) {
            redirectUrl += `title_entry=${encodeURIComponent(star)}&`;
        }
        if (redirectUrl.endsWith('&')) {
            redirectUrl = redirectUrl.slice(0, -1);
        }
        // Redirect to the constructed URL
        window.location.href = redirectUrl;
    });
});
/*
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search-page", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

 */