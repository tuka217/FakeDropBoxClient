package org.awalasek.fakeDropBoxClient;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws IOException {
        try {
            Path dir = Paths.get(args[0]);

            Thread thread = new Thread(new DirectoryWatchService(dir));
            thread.start();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}