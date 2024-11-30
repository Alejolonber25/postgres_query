package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://xhgrid3:5432/votaciones";
        String user = "postgres";
        String password = "postgres";

        String filePath = App.class.getResource("/data.txt").getPath();

        List<String> documents = reader(filePath);

        if (documents.isEmpty()) {
            System.out.println("File is empty.");
            return;
        }

        String query = query(documents);

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            long startTime = System.currentTimeMillis();

            ResultSet resultSet = statement.executeQuery();

            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;

            System.out.printf("time: %d ms.%n", duration);


        } catch (SQLException e) {
            System.err.println("Connection or Query Error: " + e.getMessage());
        }
    }

    private static List<String> reader(String filePath) {
        List<String> documents = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    documents.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return documents;
    }

    private static String query(List<String> documentos) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < documentos.size(); i++) {
            inClause.append("'").append(documentos.get(i)).append("'");
            if (i < documentos.size() - 1) {
                inClause.append(", ");
            }
        }
        return "SELECT " +
                "    ciudadano.documento AS documento, " +
                "    departamento.nombre AS departamento, " +
                "    municipio.nombre AS municipio, " +
                "    puesto_votacion.nombre AS puesto, " +
                "    mesa_votacion.consecutive AS mesa " +
                "FROM ciudadano " +
                "JOIN mesa_votacion ON ciudadano.mesa_id = mesa_votacion.id " +
                "JOIN puesto_votacion ON mesa_votacion.puesto_id = puesto_votacion.id " +
                "JOIN municipio ON puesto_votacion.municipio_id = municipio.id " +
                "JOIN departamento ON municipio.departamento_id = departamento.id " +
                "WHERE ciudadano.documento IN (" + inClause.toString() + ");";
    }
}
