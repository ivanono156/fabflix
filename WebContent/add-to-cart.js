let addToCartBtn = $("#add-to-cart");

function handleCartArray(resultData) {
    console.log("movie was successfully added to cart");
    console.log(resultData);
}

function addMovieToCart(submitEvent) {
    console.log("adding movie to cart");

    submitEvent.preventDefault();

    jQuery.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: addToCartBtn.serialize(),
        success: (resultData) => handleCartArray(resultData)
    });
}

addToCartBtn.submit(addMovieToCart);