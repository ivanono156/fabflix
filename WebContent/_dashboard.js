let Employee_login_form = $("#employee-login-form");

function handleEmployeeLoginResult(resultData) {

    console.log("handle login response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultData["status"] === "success") {
        console.log("SUCESSSSSS");
        window.location.replace("meta-data.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultData["message"]);
        $("#employee_login_error_message").text(resultData["message"]);
    }
}

function submitEmployeeLoginForm(formSubmitEvent) {
    console.log("submit login form");

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/employee-login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: Employee_login_form.serialize(),
            success: handleEmployeeLoginResult
        });
}

Employee_login_form.submit(submitEmployeeLoginForm);


