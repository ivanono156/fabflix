let addMovieForm= $("#add-movie-form");

function handleAddMovieResult(resultData) {
    let errorMessageElement = $("#error-message");
    let successMessageElement = $("#success-message");
    let movieIdElement = $("#new-movie-id");
    let starIdElement = $("#new-star-id");
    let genreIdElement = $("#new-genre-id");

    errorMessageElement.hide();
    successMessageElement.hide();
    movieIdElement.hide();
    starIdElement.hide();
    genreIdElement.hide();

    if (resultData["status_message"] === "success"){
        successMessageElement.text("Movie added successfully!").show();
        movieIdElement.text("Movie ID: " + resultData["new_movie_id"]).show();
        starIdElement.text("Star ID: " + resultData["new_star_id"]).show();
        genreIdElement.text("Genre ID: " + resultData["new_genre_id"]).show();
    } else {
        errorMessageElement.text("Movie already exists!").show();
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