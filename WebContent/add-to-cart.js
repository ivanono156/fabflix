let addToCartBtn = $("#add-to-cart");

function handleCartArray(resultData) {
    console.log("movie was successfully added to cart");
    console.log(resultData);
}

function handleError() {
    console.log("could not add movie to cart");
}

function addMovieToCart(submitEvent) {
    console.log("adding movie to cart");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart",
        data: addToCartBtn.serialize(),
        success: (resultData) => handleCartArray(resultData),
        error: handleError
    });
}

addToCartBtn.submit(addMovieToCart);