// Example searches need this hack to work. http://www.mkyong.com/jsf2/how-to-pass-new-hidden-value-to-backing-bean-in-jsf/

$(document).ready(function() {
	$('.example-input').text("701847496");
	$('.example-input').text("694508871");
	$('.example-input').text("688859445");

	initializeAutocomplete();
});

function initializeAutocomplete() {
	$('.myautocomplete').autocomplete({delay: 500, minLength: 2, source: autocomplete});/*['item', 'iphone', 'iphone 4', 'iphone 4s']});/* autocomplete});*/
}

function autocomplete(request, responseCallback) {
	var tradeMeACUrl = "http://api.trademe.co.nz/v1/Search/Suggestions.json?search_string=" + request.term;
	var suggestions = [];
	$.getJSON(tradeMeACUrl, function(json) {
		$.each(json.AutoSuggestions, function(index) {
			suggestions.push(this.SearchTerm);
		});
		responseCallback(suggestions);
	});

}




