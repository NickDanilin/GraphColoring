package edu.spbstu.BDD;

import edu.spbstu.App;
import edu.spbstu.menu.ResourceClassHelpInfoProvider;
import edu.spbstu.menu.StreamIOAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class AppContext {
    public final App app;
    public final PipedInputStream pipedInputStream = new PipedInputStream();
    public final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
    public final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    public AppContext() throws IOException {
        this.app = new App(
                new StreamIOAdapter(outputStream, pipedInputStream),
                new ResourceClassHelpInfoProvider(AppContext.class, "/testfile.txt")
                );
        new Thread(app::run).start();
    }

    public void writeAndFlush(String data) throws IOException {
        pipedOutputStream.write(data.getBytes());
        pipedOutputStream.flush();
    }


    public void waitForOutputContains(String expected, boolean reset) throws InterruptedException {
        int maxTries = 50;
        while (maxTries-- > 0) {
            String outputStreamString = outputStream.toString();
            if (outputStreamString.contains(expected)) {
                if (reset) outputStream.reset();
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Timed out waiting for output to contain: " + expected);
    }

}
