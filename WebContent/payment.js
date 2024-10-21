let paymentForm = $("#payment-form");

const priceFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
})

function getCartInfo(resultData) {
    console.log("retrieving cart info");

    $("#total-cart-price").text("Total price: " + priceFormatter.format(resultData["total_cart_price"]));
}

function handlePayment(resultData) {
    console.log("handle payment response");

    if (resultData["status"] === "success") {
        console.log("payment received successfully");
        $("#error-message").text("");
        window.location.replace("order-confirmation.html");
    } else {
        console.log("payment was not received");
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