function updateCart(submitEvent) {
    console.log("updating shopping cart");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart",
        data: $(this).serialize(),
        success: (resultData) => handleSessionData(resultData),
    });
}

function createButton(movieData, value) {
    // Set the text that will appear on the button
    let buttonText = "";
    if (value === -1) {buttonText = "-";}
    else if (value === 0) {buttonText = "REMOVE";}
    else if (value === 1) {buttonText = "+";}
    else {buttonText = "THIS SHOULD NOT APPEAR!!! YOU MESSED UP";}

    return "<form action='#' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' name='id' value='" + movieData["movie_id"] + "'>" +
                "<input type='hidden' name='title' value='" + movieData["movie_title"] + "'>" +
                // check ShoppingCartServlet.java for quantity values
                "<input type='hidden' name='quantity' value='" + value + "'>" +
                // FIXME: include movie's price "<input type='hidden' name='price' value='" + movieData["movie_price"] + "'>" +
                "<input type='submit' value='" + buttonText + "'>" +
            "</form>";
}

function handleSessionData(resultData) {
    console.log("handle session response");
    console.log(resultData);

    fillShoppingCart(resultData["cart_items"]);

    $("#total-cart-price").text("Total price: " + resultData["total_cart_price"]);
}

function fillShoppingCart(resultDataArray) {
    console.log("filling shopping cart");
    console.log(resultDataArray);

    let cartListBodyElement = $("#cart-list-body");

    // Empty table body
    cartListBodyElement.empty();

    for (let i = 0; i < resultDataArray.length; i++) {
        let rowHTML = "<tr id='" + resultDataArray[i]["movie_id"] + "'>";
        rowHTML += "<td>" + resultDataArray[i]["movie_title"] + "</td>";
        rowHTML += "" +
            "<td>" +
            createButton(resultDataArray[i], -1) +
            resultDataArray[i]["movie_quantity"] +
            createButton(resultDataArray[i], 1) +
            "</td>";
        rowHTML += "<td>" + resultDataArray[i]["movie_price"] + "</td>";
        rowHTML += "<td>" + resultDataArray[i]["total_price"] + "</td>";
        rowHTML += "" +
            "<td>" +
            createButton(resultDataArray[i], 0) +
            "</td>";
        rowHTML += "</tr>";

        cartListBodyElement.append(rowHTML);
    }

    // Bind update cart function to all add to cart buttons
    $(".add-to-cart-btn").submit(updateCart);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: (resultData) => handleSessionData(resultData)
});