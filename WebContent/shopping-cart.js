function increaseQuantity(element) {
    console.log("increasing quantity");
    console.log(element);
    console.log(element.value);
}

function decreaseQuantity(element) {
    console.log("decreasing quantity");
    console.log(element);
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart",
        data: addToCartBtn.serialize(),
        success: (resultData) => handleCartArray(resultData),
        error: handleError
    });
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

    for (let i = 0; i < resultDataArray.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + resultDataArray[i]["movie_title"] + "</td>";
        rowHTML += "" +
            "<td>" +
            "<form action='api/shopping-cart' target='_self' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' name='id' value='" + resultDataArray[i]["movie_id"] + "'>" +
                "<input type='hidden' name='title' value='" + resultDataArray[i]["movie_title"] + "'>" +
                // value of 1 means to increment quantity (per ShoppingCartServlet.java)
                "<input type='hidden' name='quantity' value='-1'>" +
                // FIXME: include movie's price "<input type='hidden' name='price' value='" + resultDataArray[i]["movie_price"] + "'>" +
                "<input type='submit' VALUE='-'>" +
            "</form>" +
            resultDataArray[i]["movie_quantity"] +
            "<form action='api/shopping-cart' target='_self' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' name='id' value='" + resultDataArray[i]["movie_id"] + "'>" +
                "<input type='hidden' name='title' value='" + resultDataArray[i]["movie_title"] + "'>" +
                // value of 1 means to increment quantity (per ShoppingCartServlet.java)
                "<input type='hidden' name='quantity' value='1'>" +
                // FIXME: include movie's price "<input type='hidden' name='price' value='" + resultDataArray[i]["movie_price"] + "'>" +
                "<input type='submit' VALUE='+'>" +
            "</form>" +
            "</td>";
        rowHTML += "<td>" + resultDataArray[i]["movie_price"] + "</td>";
        rowHTML += "<td>" + resultDataArray[i]["total_price"] + "</td>";
        rowHTML += "" +
            "<td>" +
            "<form action='api/shopping-cart' target='_self' method='post' class='add-to-cart-btn'>" +
                "<input type='hidden' name='id' value='" + resultDataArray[i]["movie_id"] + "'>" +
                "<input type='hidden' name='title' value='" + resultDataArray[i]["movie_title"] + "'>" +
                // value of 1 means to increment quantity (per ShoppingCartServlet.java)
                "<input type='hidden' name='quantity' value='0'>" +
                // FIXME: include movie's price "<input type='hidden' name='price' value='" + resultDataArray[i]["movie_price"] + "'>" +
                "<input type='submit' VALUE='REMOVE'>" +
            "</form>" +
            "</td>";
        rowHTML += "</tr>";

        cartListBodyElement.append(rowHTML);
    }

}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: (resultData) => handleSessionData(resultData)
});