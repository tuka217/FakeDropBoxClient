package org.awalasek.fakedropbox.client;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.awalasek.fakedropbox.common.FileChange;
import org.awalasek.fakedropbox.common.FileChangeFactory;

public class DirectoryWatchService implements Runnable {

    private static final int WATCHED_DIRS_ARE_INACCESSIBLE = 1;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private ChangeSubmitter changeSubmitter;
    private FileChangeFactory fileChangeFactory;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    DirectoryWatchService(String username, Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.changeSubmitter = new ChangeSubmitterImpl();
        this.fileChangeFactory = new FileChangeFactory(username);

        registerAll(dir);
    }

    private void registerAll(final Path start) throws IOException {
        System.out.format("Scanning %s ...\n", start);
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Done.");
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE/*, ENTRY_MODIFY*/);
        Path prev = keys.get(key);
        if (prev == null) {
            System.out.format("register: %s\n", dir);
        } else if (!dir.equals(prev)) {
            System.out.format("update: %s -> %s\n", prev, dir);
        }
        keys.put(key, dir);
    }

    @Override
    public void run() {
        while (true) {
            checkWatchEvents();
        }
    }

    private void checkWatchEvents() {
        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException x) {
            return;
        }

        Path dir = keys.get(key);
        if (dir == null) {
            System.err.println("WatchKey not recognized!");
            return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == OVERFLOW) {
                // do not handle, there is no need for it
                continue;
            }
            handleEvent(event, dir);
        }
        resetKeyIfInaccessible(key);
    }

    private void handleEvent(WatchEvent<?> event, Path dir) {
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        signalEvent(event, child);
        registerNewDirectories(event, child);
    }

    private void signalEvent(WatchEvent<?> event, Path child) {
        FileChange fileChange = fileChangeFactory.getFileChange(event.kind(), child);
        changeSubmitter.submitFileChange(fileChange);
        System.out.format("%s: %s\n", event.kind().name(), child);
    }

    private void registerNewDirectories(WatchEvent<?> event, Path child) {
        if (event.kind() == ENTRY_CREATE) {
            try {
                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                    registerAll(child);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetKeyIfInaccessible(WatchKey key) {
        boolean valid = key.reset();
        if (!valid) {
            keys.remove(key);
            checkDirectoryAccessibility();
        }
    }

    private void checkDirectoryAccessibility() {
        if (allDirectoriesAreInaccessible()) {
            System.err.println("Watched directories are not accessible anymore!");
            System.exit(WATCHED_DIRS_ARE_INACCESSIBLE);
        }
    }

    private boolean allDirectoriesAreInaccessible() {
        return keys.isEmpty();
    }
}
