package org.awalasek.fakedropbox.client;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws IOException {
        try {
            String username = args[0];
            Path dir = Paths.get(args[1]);

            Thread thread = new Thread(new DirectoryWatchService(username, dir));
            thread.start();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}