let addMovieForm= $("#add-movie-form");

function handleAddMovieResult(resultData) {
    // Check the status message from the server
    console.log("SUCCESS: Movie added!");
    if(resultData["status_message"] === "Movie already exists"){
        $("#error-message").text("Movie already exists").show();
    }
    else{
        $("#success-message").text("Movie added successfully!").show();

        if (resultData["new_movie_id"]) {
            $("#new-movie-id").text("New Movie ID: " + resultData["new_movie_id"]).show();
        }
        if (resultData["new_star_id"]) {
            $("#new-star-id").text("Star ID: " + resultData["new_star_id"]).show();
        }
        if (resultData["new_genre_id"]) {
            $("#new-genre-id").text("Genre ID: " + resultData["new_genre_id"]).show();
        }
    }
}

function handleError() {
    console.log("error occurred while adding movie");
}

function submitAddMovieForm(formSubmitEvent) {
    console.log("submit add movie form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-movie", {
            dataType: "json",
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: addMovieForm.serialize(),
            success:(resultData) => handleAddMovieResult(resultData),
            error: handleError
        });
}

addMovieForm.submit(submitAddMovieForm);