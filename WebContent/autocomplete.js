let autocompleteSearchbar = $('#autocomplete');

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated");

	// TODO: if you want to check past query results first, you can do it here
	console.log("sending AJAX request to backend Java Servlet");

	// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
	// with the query data
	jQuery.ajax({
		"method": "GET",
		// generate the request url from the query.
		// escape the query string to avoid errors caused by special characters
		"url": "api/autocomplete?search-query=" + query,
		"success": function(data) {
			// pass the data, query, and doneCallback function into the success handler
			handleLookupAjaxSuccess(data, query, doneCallback)
		},
		"error": function(errorData) {
			console.log("lookup ajax error")
			console.log(errorData)
		}
	});
}

function handleLookupAjaxSuccess(jsonData, query, doneCallback) {
	console.log("lookup ajax successful");

	// TODO: if you want to cache the result into a global variable you can do it here

	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
	console.log("you selected " + suggestion["value"] + " with ID " + suggestion["data"]["movie_id"]);
	window.location.href = "single-movie.html?id=" + suggestion["data"]["movie_id"];
}

autocompleteSearchbar.autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});

function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
}

// bind pressing enter key to a handler function
autocompleteSearchbar.keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode === 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch(autocompleteSearchbar.val());
	}
});

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button


