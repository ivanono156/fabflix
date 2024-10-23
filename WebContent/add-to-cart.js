let addToCartBtn = $("#add-to-cart");

function handleCartArray(resultData) {
    alert("Successfully added movie to cart!")
    console.log("movie was successfully added to cart");
    console.log(resultData);
}

function handleCartError() {
    alert("Could not add movie to cart!")
    console.log("could not add movie to cart");
}

function addMovieToCart(submitEvent) {
    console.log("adding movie to shopping cart");

    submitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart",
        data: addToCartBtn.serialize(),
        success: (resultData) => handleCartArray(resultData),
        error: handleCartError
    });
}

addToCartBtn.submit(addMovieToCart);