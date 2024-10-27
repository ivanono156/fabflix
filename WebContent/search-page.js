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


        const title = form.elements['title_entry'].value;
        const year= form.elements['year_entry'].value;
        const director = form.elements['director_entry'].value;
        const star = form.elements['star_entry'].value;

        console.log("Title:", title);
        console.log("Director:", director);

        let redirectUrl = 'movie-list.html?'
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
/*
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search-page", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSearchResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

 */