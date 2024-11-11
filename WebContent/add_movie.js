let addMovieForm= $("#add-movie-form");

function handleAddMovieResult(resultData) {
    // Check the status message from the server
    $("#error-message").text("").hide();
    $("#success-message").text("").hide();
    $("#new-movie-id").text("").hide();
    $("#new-star-id").text("").hide();
    $("#new-genre-id").text("").hide();
    console.log("SUCCESS: Movie added!");
    if(resultData["status_message"] === "Movie already exists"){
        $("#error-message").text("Movie already exists").show();
    }
    else{
        $("#success-message").text("Movie added successfully!").show();

        if (resultData["new_movie_id"]) {
            $("#new-movie-id").text("New Movie ID: " + resultData["new_movie_id"]).show();
        }
        if (resultData["new_star_id"] !== null) {
            $("#new-star-id").text("Star ID: " + resultData["new_star_id"]).show();
        }
        else{
            $("#new-star-id").text("Star ID: Found").show();
        }
        if (resultData["new_genre_id"] !== null && resultData["new_genre_id"] !== 0) {
            $("#new-genre-id").text("Genre ID: " + resultData["new_genre_id"]).show();
        }
        else{
            $("#new-genre-id").text("Genre ID: Found").show();
        }

    }
    addMovieForm[0].reset();
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