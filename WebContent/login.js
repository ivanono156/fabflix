let login_form = $("#login-form");

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

function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);

