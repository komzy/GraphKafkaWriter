package utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Utility to write JSON Lines into a gzip file (.jsonl.gz)
 */
public class GzipJsonlWriter {

    private static BufferedWriter writer;
    private static boolean initialized = false;
    private static final String DEFAULT_FILE_NAME = "output.jsonl.gz";

    /**
     * Initialize writer with default file name
     */
    private static void initIfNeeded() throws IOException {
        if (!initialized) {
            init(DEFAULT_FILE_NAME);
        }
    }

    /**
     * Initialize writer with custom file path
     */
    public static void init(String filePath) throws IOException {
        if (initialized) {
            return;
        }

        FileOutputStream fos = new FileOutputStream(filePath);
        GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
        OutputStreamWriter osw = new OutputStreamWriter(gzipOut, StandardCharsets.UTF_8);
        writer = new BufferedWriter(osw);

        initialized = true;

        System.out.println("GzipJsonlWriter initialized: " + filePath);
    }

    /**
     * Write one JSON line
     */
    public static synchronized void writeLine(String jsonLine) throws IOException {
        initIfNeeded();

        writer.write(jsonLine);
        writer.newLine();  // important for JSONL format
    }

    /**
     * Flush buffer
     */
    public static void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    /**
     * Close the writer safely
     */
    public static void close() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
                System.out.println("GzipJsonlWriter closed.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writer = null;
                initialized = false;
            }
        }
    }
}