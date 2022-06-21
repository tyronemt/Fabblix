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
import java.sql.Statement;
import java.util.ArrayList;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "StarsServlet", urlPatterns = "/api/movie-list")
public class StarsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            JsonArray jsonArray = new JsonArray();
            // Declare our statement
            Statement statement = conn.createStatement();

            //QUERY 1
            String query1 = "SELECT movies.title, movies.year, movies.director, ratings.rating, movies.id\n" +
                    "FROM ratings, movies\n" +
                    "WHERE ratings.movieId = movies.id\n" +
                    "ORDER BY ratings.rating DESC\n" +
                    "LIMIT 20;";

            // Perform the query
            ResultSet rsMovie = statement.executeQuery(query1);

            // Iterate through each row of rs
            while (rsMovie.next()) {
                String movies_id = rsMovie.getString("id");
                String movies_title= rsMovie.getString("title");
                String movies_year = rsMovie.getString("year");
                String movies_director = rsMovie.getString("director");
                String movies_rating = rsMovie.getString("rating");


                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movies_id", movies_id);
                jsonObject.addProperty("movies_title", movies_title);
                jsonObject.addProperty("movies_year", movies_year);
                jsonObject.addProperty("movies_director", movies_director);
                jsonObject.addProperty("movies_rating", movies_rating);

                String query_genres = "SELECT genres.name\n" +
                        "FROM genres_in_movies, genres\n" +
                        "WHERE genres_in_movies.movieId = ? and genres.id = genres_in_movies.genreId\n" +
                        "LIMIT 3";

                PreparedStatement statementGenre = conn.prepareStatement(query_genres);
                statementGenre.setString(1,movies_id);
                ResultSet rsGenre = statementGenre.executeQuery();
                String temp = "";

                while (rsGenre.next()){
                    temp += rsGenre.getString("name") + ";";
                }
                jsonObject.addProperty("movie_genre",temp);
                rsGenre.close();
                statementGenre.close();

                String query_star = "SELECT stars.name, stars.id\n" +
                        "FROM stars_in_movies, stars\n" +
                        "WHERE stars_in_movies.movieId = ? and stars_in_movies.starid = stars.id\n" +
                        "LIMIT 3";

                PreparedStatement statementStar = conn.prepareStatement(query_star);
                statementStar.setString(1,movies_id);
                ResultSet rsStar = statementStar.executeQuery();
                temp = "";

                while (rsStar.next()){

                    temp += rsStar.getString("name") + ";" ;
                    temp += rsStar.getString("id") + ";";
                }
                jsonObject.addProperty("stars",temp);
                rsStar.close();
                statementStar.close();
                jsonArray.add(jsonObject);


            }
            rsMovie.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}