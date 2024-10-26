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

        // Get values from form fields (replace with your actual input names)
        //const genre = document.getElementById("genre").value; // Example field
        //const title = document.getElementById("title").value; // Example field

        // Construct the redirect URL with query parameters
        let redirectUrl = `movie-list.html?`;
/*
        if (genre) {
            redirectUrl += `gid=${encodeURIComponent(genre)}&`;
        }
        if (title) {
            redirectUrl += `title_entry=${encodeURIComponent(title)}&`;
        }
*/
        // Redirect to the constructed URL
        window.location.href = redirectUrl;
    });
});