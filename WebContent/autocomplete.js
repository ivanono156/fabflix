let autocompleteSearchbar = $('#autocomplete');

function cacheJsonData(key, jsonData) {
  sessionStorage.setItem(key, JSON.stringify(jsonData));
}

function getCachedJsonData(key) {
  const cachedData = sessionStorage.getItem(key);
  return cachedData ? JSON.parse(cachedData) : null;
}

function handleLookup(query, doneCallback) {
	console.log("autocomplete search initiated");

	if (getCachedJsonData(query) === null) {
		console.log("sending AJAX request to backend Java Servlet");

		jQuery.ajax({
			"method": "GET",
			"url": "api/autocomplete?search-query=" + query,
			"success": function(data) {
				handleLookupAjaxSuccess(data, query, doneCallback)
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		});
	} else {
		console.log("using cached query results");

		let jsonData = getCachedJsonData(query);
		console.log(jsonData);

		doneCallback( { suggestions: jsonData } );
	}
}

function handleLookupAjaxSuccess(jsonData, query, doneCallback) {
	console.log(jsonData);

	cacheJsonData(query, jsonData)

	doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
	window.location.href = "single-movie.html?id=" + suggestion["data"]["movie_id"];
}

autocompleteSearchbar.autocomplete({
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300,
	minChars: 3
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


