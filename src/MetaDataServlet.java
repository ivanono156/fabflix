import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@WebServlet(name = "MetaDataServlet", urlPatterns = "/api/meta-data")
public class MetaDataServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            JsonArray tablesArray = new JsonArray();
            try (ResultSet rstables = md.getTables(null, null, null, new String[]{"TABLE"})) {
                while (rstables.next()) {
                    //iterate through the tables
                    JsonObject tableObj = new JsonObject();
                    String tableName = rstables.getString("TABLE_NAME");
                    tableObj.addProperty("table_name", tableName);

                    JsonArray columnsArray = new JsonArray();
                    try (ResultSet columns = md.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            JsonObject columnObj = new JsonObject();
                            columnObj.addProperty("column_name", columns.getString("COLUMN_NAME"));
                            columnObj.addProperty("column_type", columns.getString("TYPE_NAME"));
                            columnsArray.add(columnObj);
                        }
                    }
                    tableObj.add("columns", columnsArray);
                    tablesArray.add(tableObj);
                }
            }
            jsonResponse.add("tables", tablesArray);
            // Set response status to 200 (OK)
            response.setStatus(HttpServletResponse.SC_OK);
            out.write(jsonResponse.toString());
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
