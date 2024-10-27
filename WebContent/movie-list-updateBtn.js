/*
document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("update_movie_list_form");

    form.addEventListener("submit", function(event) {
        event.preventDefault();



        let redirectUrl = 'movie-list.html?page-number=1&';
        let display = form.elements['display'].value;
        let field = form.elements['sort_field'].value;
        let order = form.elements['sort_order'].value;
        // Construct the redirect URL with query parameters
        if(display){
            redirectUrl += `display=${encodeURIComponent(display)}&`;
        }
        if(field){
            redirectUrl +=`sort_field=${encodeURIComponent(field)}&`;
        }
        if(order){
            redirectUrl +=`sort_order=${encodeURIComponent(order)}&`;
        }
        if (redirectUrl.endsWith('&')) {
            redirectUrl = redirectUrl.slice(0, -1);
        }
        // Redirect to the constructed URL
        window.location.href = redirectUrl;
    });
});

*/
document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("update_movie_list_form");

    // Store current page number in a variable
    let pageNumber = parseInt(getParameterByName('page-number')) || 1; // Get page number from URL or default to 1

    form.addEventListener("submit", function(event) {
        event.preventDefault();
        setDefaultValues();
        redirect();
    });

    // Function to change page number
    function changePage(direction) {
        pageNumber += direction;
        if (pageNumber < 1) pageNumber = 1; // Prevent going to a negative page
        setDefaultValues();
        redirect();
    }

    function setDefaultValues() {
        //let display = getParameterByName('display');
        let sort_field = getParameterByName('sort_field');
        let sort_order = getParameterByName('sort_order');
        let field2 = getParameterByName('sort_field2');
        let order2 = getParameterByName('sort_order2');
/*
        if (display) {
            form.elements['display'].value = display;
        }
*/
        if (sort_field) {
            form.elements['sort_field'].value = sort_field;
        }

        if (sort_order) {
            form.elements['sort_order'].value = sort_order;
        }
        if(field2){
            form.elements['sort_field2'].value = field2;
        }
        if(order2){
            form.elements['sort_order2'].value = order2;
        }
    }
    setDefaultValues();

    // Redirect to the updated page number
    function redirect() {
        let redirectUrl = `movie-list.html?page-number=${pageNumber}&`;

        // Include form values in the URL
        let display = form.elements['display'].value;
        let field = form.elements['sort_field'].value;
        let order = form.elements['sort_order'].value;
        let field2 = form.elements['sort_field2'].value;
        let order2 = form.elements['sort_order2'].value;

        if(display){
            redirectUrl += `display=${encodeURIComponent(display)}&`;
        }
        if(field){
            redirectUrl += `sort_field=${encodeURIComponent(field)}&`;
            form.elements['sort_field'].value = field;
        }
        if(order){
            redirectUrl += `sort_order=${encodeURIComponent(order)}&`;
            form.elements['sort_order'].value = order;
        }
        if(field2){
            redirectUrl += `sort_field2=${encodeURIComponent(field2)}&`;
        }
        if(order2){
            redirectUrl += `sort_order2=${encodeURIComponent(order2)}&`;
        }
        if (redirectUrl.endsWith('&')) {
            redirectUrl = redirectUrl.slice(0, -1);
        }

        // Redirect to the constructed URL
        window.location.href = redirectUrl;
    }

    // Attach event listeners for pagination buttons
    document.getElementById("prev-button").addEventListener("click", function() {
        changePage(-1);
    });

    document.getElementById("next-button").addEventListener("click", function() {
        changePage(1);
    });
});