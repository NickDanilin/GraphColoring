package edu.spbstu.menu;


import java.io.IOException;

public interface IOAdapter {
    void print(String message) throws IOException;

    void printLine(String message) throws IOException;

    String readLine() throws IOException;
}
