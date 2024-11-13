let autocompleteSearchbar = $('#autocomplete');
let searchButton = $("#search-button");

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
				handleLookupAjaxSuccess(data, query, doneCallback);
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

	cacheJsonData(query, jsonData);

	doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
	window.location.href = "single-movie.html?id=" + suggestion["data"]["movie_id"];
}

autocompleteSearchbar.autocomplete({
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
	minChars: 3
});

function handleNormalSearch(query) {
	window.location.href = "movie-list.html?" + createDefaultMovieListUrl()
		+ "&" + createUrlParams(searchQueryKeyName, query);
}

autocompleteSearchbar.keypress(function(event) {
	if (event.keyCode === 13) {
		handleNormalSearch(autocompleteSearchbar.val());
	}
});

searchButton.on("click", function () {
	handleNormalSearch(autocompleteSearchbar.val());
});
