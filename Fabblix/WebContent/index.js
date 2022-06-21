/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movies_id'] + '">'
            + resultData[i]["movies_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movies_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movies_director"] + "</th>";
        let genre = resultData[i]["movie_genre"].split(";");
        rowHTML += "<th>";
        for (let j = 0; j < genre.length - 1; j++) {
            rowHTML += genre[j] + ", ";
        }
        rowHTML = rowHTML.slice(0, rowHTML.length-2)
        rowHTML += "</th>";

        rowHTML += "<th>";
        let stars = resultData[i]["stars"].split(";");
        for (let j = 0; j < stars.length - 1; j+=2) {
            rowHTML +=
                '<a href="single-star.html?id=' + stars[j+1] + '">'
                + stars[j] + '</a>';
            rowHTML += ", ";
        }
        rowHTML = rowHTML.slice(0, rowHTML.length-2)
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movies_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});