/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");
    console.log(resultData);
    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");
    let movieBodyElement = jQuery("#movie_body");


    let genre = (resultData[0]["movie_genres"]).split(";");
    let temp = "";
    for (let j = 0; j < genre.length - 1; j++) {
        temp += genre[j] + ", ";
    }
    temp = temp.slice(0, temp.length-2)
    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<center><p>Movie Title: " + resultData[0]["movies_title"] + "</p>" +
        "<p>Movie Year: " + resultData[0]["movies_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movies_director"] + "</p>" +
        "<p>Rating: " + resultData[0]["movies_rating"] + "</p>" +
        "<p>Genres: " + temp + "</p>");




    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"


    // Concatenate the html tags with resultData jsonObject to create table rows


    let stars = resultData[0]["stars"].split(";");



    for (let j = 0; j < stars.length - 1; j+=3) {
        let rowHTML = "";
        rowHTML += "<tr>"
        rowHTML += "<th>"
        rowHTML += '<a href="single-star.html?id=' + stars[j+1] + '">' + stars[j] + ' </a>';
        rowHTML += "</th>";
        let dob = stars[j+2];
        if ( dob === 'null'){
            dob = "N/A";
        }
        rowHTML += "<th>" + dob + "</th>";
        rowHTML += '</tr>';

        movieBodyElement.append(rowHTML);
    }
        // Append the row created to the table body, which will refresh the page




}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by moviesServlet in movies.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SinglemovieServlet
});