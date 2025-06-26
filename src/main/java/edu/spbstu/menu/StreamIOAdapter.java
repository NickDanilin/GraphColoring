package edu.spbstu.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StreamIOAdapter implements IOAdapter, AutoCloseable {
    public static final String LINE_SEPARATOR = System.lineSeparator();
    private final OutputStream out;
    private final InputStream in;
    private final BufferedReader reader;


    public StreamIOAdapter(OutputStream out, InputStream in) {
        this.out = out;
        this.in = in;
        this.reader = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void print(String message) throws IOException {
        out.write(message.getBytes());
    }

    @Override
    public void printLine(String message) throws IOException {
        out.write((message + LINE_SEPARATOR).getBytes());
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public void close() throws Exception {
        reader.close();
        out.close();
        in.close();
    }
}
