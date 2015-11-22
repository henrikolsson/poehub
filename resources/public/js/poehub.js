function init() {
    var nav = document.querySelectorAll("ul.nav a");
    for (var i=0;i<nav.length;i++) {
        nav[i].parentNode.setAttribute("class", ""); 
        if (window.location.href.startsWith(nav[i].href)) {
            nav[i].parentNode.setAttribute("class", "active"); 
            console.log(window.location.href);
            console.log(nav[i].href);
        }
    }
}

function search() {
    var q = document.querySelector("#query").value;
    var resultElement = document.querySelector("#search-results");
    if (q && q.length >= 3) {
        resultElement.innerHTML = "Searching...";
        $.post("/search", {query: q}, function(res) {
            if (res.length == 0) {
                resultElement.innerHTML = "No results.";
            } else {
                resultElement.innerHTML = "";
                res.forEach(function(r) {
                    var a = document.createElement("a");
                    a.href = r._source.url;
                    a.innerHTML = r._source.title;
                    var br = document.createElement("br");
                    resultElement.appendChild(a);
                    resultElement.appendChild(br);
                });
            }
        });
    } else {
        resultElement.innerHTML = "Minimum search length is 3 characters";
    }
    return false;
}

document.addEventListener('DOMContentLoaded', init, false);
