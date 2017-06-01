package org.awalasek.fakedropbox.client;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        try {
            String username = args[0];
            Path dir = Paths.get(args[1]);

            ServerWatchService serverWatchService = new ServerWatchService(username, dir);

            Thread localWatch = new Thread(new DirectoryWatchService(username, dir));
            localWatch.start();

            Thread remoteWatch = new Thread(serverWatchService);
            remoteWatch.start();

        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}