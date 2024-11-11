let addStarForm = $("#add-star-form");

function handleAddingStar(resultData) {
    console.log("Updated rows: " + resultData["updated_rows"]);

    if (resultData["status"] === "success") {
        $("#add-star-message").text("Successfully added star!");
    } else {
        $("#add-star-message").text("Failed to add star! " + resultData["message"]);
    }
}

function handleError() {
    console.log("error occurred while adding star");
}

function submitAddStar(submitEvent) {
    console.log("submit add star form");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/add-star",
        data: addStarForm.serialize(),
        success: (resultData) => handleAddingStar(resultData),
        error: handleError
    });
}

addStarForm.submit(submitAddStar);