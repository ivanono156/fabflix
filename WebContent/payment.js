let paymentForm = $("#payment-form");

function getCartInfo(resultData) {
    console.log("retrieving cart info");

    $("#total-cart-price").text("Total price: " + resultData["total_cart_price"]);
}

function handlePayment(resultData) {
    console.log("handle payment response");

    if (resultData["status"] === "success") {
        console.log("payment received successfully. enjoy your non-existent prize.");
        $("#error-message").text("");
        window.location.replace("order-confirmation.html");
        // show confirmation page
    } else {
        console.log("fraud ? question mark ?");
        console.log(resultData["message"]);
        $("#error-message").text(resultData["message"]);
    }
}

function submitPayment(submitEvent) {
    console.log("submit payment form");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        data: paymentForm.serialize(),
        url: "api/payment",
        success: (resultData) => handlePayment(resultData)
    });
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: getCartInfo
});

paymentForm.submit(submitPayment);