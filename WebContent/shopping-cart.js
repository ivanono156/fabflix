function handleSessionData(resultData) {
    console.log("handle session response");
    console.log(resultData);

    fillShoppingCart(resultData["cart_items"]);
}

function fillShoppingCart(resultDataArray) {
    console.log("filling shopping cart");
    console.log(resultDataArray);

    let cartListBodyElement = $("#cart-list-body");

    for (let i = 0; i < resultDataArray.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + resultDataArray[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + resultDataArray[i]["movie_quantity"] + "</td>";
        rowHTML += "<td>" + resultDataArray[i]["movie_price"] + "</td>";
        rowHTML += "<td>" + resultDataArray[i]["total_price"] + "</td>";
        rowHTML += "<td>INSERT DELETE BUTTON HERE</td>";
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