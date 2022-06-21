import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")

public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT movies.title, movies.year, movies.director, ratings.rating\n" +
                    "FROM ratings, movies\n" +
                    "WHERE ratings.movieId = ? and movies.id = ? ";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            statement.setString(2, id);
            // Perform the query
            ResultSet rsMovie = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rsMovie.next()) {

                String movies_title= rsMovie.getString("title");
                String movies_year = rsMovie.getString("year");
                String movies_director = rsMovie.getString("director");
                String movies_rating = rsMovie.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movies_title", movies_title);
                jsonObject.addProperty("movies_year", movies_year);
                jsonObject.addProperty("movies_director", movies_director);
                jsonObject.addProperty("movies_rating", movies_rating);

                String query_genres = "SELECT genres.name\n" +
                        "FROM genres_in_movies, genres\n" +
                        "WHERE genres_in_movies.movieId = ? and genres.id = genres_in_movies.genreId";

                PreparedStatement statementGenre = conn.prepareStatement(query_genres);
                statementGenre.setString(1,id);
                ResultSet rsGenre = statementGenre.executeQuery();
                String temp = "";

                while (rsGenre.next()){
                    temp += rsGenre.getString("name") + ";";
                }
                jsonObject.addProperty("movie_genres",temp);
                rsGenre.close();
                statementGenre.close();


                String query_star = "SELECT stars.name, stars.id, stars.birthYear\n" +
                        "FROM stars_in_movies, stars\n" +
                        "WHERE stars_in_movies.movieId = ? and stars_in_movies.starid = stars.id;";

                PreparedStatement statementStar = conn.prepareStatement(query_star);
                statementStar.setString(1,id);
                ResultSet rsStar = statementStar.executeQuery();
                temp = "";

                while (rsStar.next()){
                    temp += rsStar.getString("name") + ";" ;
                    temp += rsStar.getString("id") + ";";
                    temp += rsStar.getString("birthYear") + ";";
                }
                jsonObject.addProperty("stars",temp);
                rsStar.close();
                statementStar.close();

                // Create a JsonObject based on the data we retrieve from rs

                jsonArray.add(jsonObject);
            }
            rsMovie.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
