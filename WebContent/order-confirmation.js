const priceFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
})

function handleOrder(resultData) {
    console.log("displaying order confirmation results");
    console.log(resultData);

    let salesTableBodyElement = $("#sales-table-body");

    let salesJsonArray = resultData["sales"];

    for (let i = 0; i < salesJsonArray.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<td>" + salesJsonArray[i]["sale_id"] + "</td>";
        rowHTML += "<td>" + salesJsonArray[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + salesJsonArray[i]["movie_quantity"] + "</td>";
        rowHTML += "<td>" + priceFormatter.format(salesJsonArray[i]["movie_price"]) + "</td>";
        rowHTML += "<td>" + priceFormatter.format(salesJsonArray[i]["total_movie_price"]) + "</td>";
        rowHTML += "</tr>";

        salesTableBodyElement.append(rowHTML);
    }

    $("#total-cart-price").text("Total price: " + priceFormatter.format(resultData["total_cart_price"]));
}

jQuery.ajax({
    datatype: "json",
    method: "GET",
    url: "api/order-confirmation",
    success: (resultData) => handleOrder(resultData)
});