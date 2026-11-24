package com.transit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryLogger {
    private static final String LOG_FILE_PATH = "search_history.txt";

    public static void logSearch(UserSession user, String source, String dest, String routeSummary) {
        File file = new File(LOG_FILE_PATH);

        // Syllabus: LocalDateTime and DateTimeFormatter
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String      logEntry = String.format(
                "[%s]\nUser Details: %s\nQuery: %s -> %s\nResults:\n%s\n",
                formattedDateTime,
                user.toString(),
                source,
                dest,
                routeSummary
        );


        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry);
            bw.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to search history log: " + e.getMessage());
        }
    }
}