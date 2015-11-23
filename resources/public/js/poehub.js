function searchMatcher(q, cb, cbAsync) {
    if (q.length >= 3) {
        search(q + "*", function(res, err) {
            if (res) {
                var ret = [];
                res.forEach(function(r) {
                    ret.push(r._source.title);
                });
                cbAsync(ret);
            }
        });
    } else {
        cb([]);
    }
}

function init() {
    var nav = document.querySelectorAll("ul.nav a");
    for (var i=0;i<nav.length;i++) {
        nav[i].parentNode.setAttribute("class", ""); 
        if (window.location.href.startsWith(nav[i].href)) {
            nav[i].parentNode.setAttribute("class", "active"); 
        }
    }
    $('#query').typeahead({
        hint: true,
        highlight: true,
        minLength: 3
    }, {
        name: 'search',
        source: searchMatcher
    });
}

function search(q, cb) {
    $.post("/search", {query: q}, function(res) {
        window.searchResults = window.searchResults || {};
        res.forEach(function(r) {
            window.searchResults[r._source.title] = r._source.url;
        });
        cb(res);
    }).fail(function(e) {
        cb(null, e);
    });
}

function formSearch() {
    $('#query').typeahead('close');
    var q = document.querySelector("#query").value;
    if (window.searchResults && window.searchResults[q]) {
        window.location.href = window.searchResults[q];
        return false;
    }
    var resultElement = document.querySelector("#search-results");
    if (q && q.length >= 3) {
        resultElement.innerHTML = "Searching...";
        search(q, function(res, err) {
            if (res) {
                if (res.length == 0) {
                    resultElement.innerHTML = "No results.";
                } else {
                    resultElement.innerHTML = "";
                    for (var i=0;i<res.length;i++) {
                        var r = res[i];
                        var a = document.createElement("a");
                        a.href = r._source.url;
                        a.innerHTML = r._source.title;
                        var br = document.createElement("br");
                        resultElement.appendChild(a);
                        resultElement.appendChild(br);
                    }
                }
            } else {
                resultElement.innerHTML = "Failed to search";
            }
        });
    } else {
        resultElement.innerHTML = "Minimum search length is 3 characters";
    }
    return false;
}

document.addEventListener('DOMContentLoaded', init, false);
