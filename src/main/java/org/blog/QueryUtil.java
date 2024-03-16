package org.blog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.blog.dto.ResponseDto;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUtil {

    public ResponseDto makeQuery(Map<String, String> input) {
        ResponseDto response = new ResponseDto();
        try {
            String method = input.get("method");
            String query = input.get("query");

            if (method.equals("GET")) {
                response.setStatusCode(200);
                response.setBody(new ObjectMapper().writeValueAsString(get(query)));
            }

            if (method.equals("POST")) {
                response.setStatusCode(200);
                response.setBody(new ObjectMapper().writeValueAsString(post(query)));
            }

        } catch (JsonProcessingException e) {
            response.setStatusCode(500);
            response.setBody(e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

    private List<Object> get(String query) {
        List<Object> response = new ArrayList<>();
        try (java.sql.Connection conn = Connection.makeConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, String> rowMap = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            String columnValue = rs.getString(i);
                            rowMap.put(columnName, columnValue);
                        }
                        response.add(rowMap);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error running time: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    private int post(String query) {
        try (java.sql.Connection conn = Connection.makeConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error running time: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return 0;
    }
}
