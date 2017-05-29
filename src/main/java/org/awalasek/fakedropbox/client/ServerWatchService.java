package org.awalasek.fakedropbox.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.awalasek.fakedropbox.common.ChangeType;
import org.awalasek.fakedropbox.common.FileChangeFactory;
import org.json.JSONObject;

public class ServerWatchService implements Runnable {

    private static final String SERVICE_ADDRESS = "http://172.17.0.2:8080/FakeDropBox/synchronizeFiles";
    private static final Integer SLEEP_TIME_SECONDS = 10;
    private static final Integer SLEEP_TIME_MILLIS = SLEEP_TIME_SECONDS * 1000;

    private String username;
    private Path watchedDirectory;
    private List<Path> watchedFiles;
    private ChangeSubmitter changeSubmitter;

    public ServerWatchService(String username, Path watchedDirectory) throws Exception {
        this.username = username;
        this.watchedDirectory = watchedDirectory;
        this.watchedFiles = restoreFilesFromServer(username);
        this.changeSubmitter = new ChangeSubmitterImpl();
        checkSynchronization();
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.watchedFiles = restoreFilesFromServer(username);
                checkSynchronization();
                Thread.sleep(SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkSynchronization() throws IOException {
        List<Path> localFiles = new ArrayList<>();
        Files.walk(watchedDirectory).forEach(localFiles::add);

        System.out.println("Local paths:");
        for (Path path : localFiles) {
            System.out.println(path.toString());
        }
        
        System.out.println();
        System.out.println("Remote paths:");
        for (Path path : watchedFiles) {
            System.out.println(path.toString());
        }
        System.out.println();
        
        List<Path> localFilesNotOnRemote = new ArrayList<>(localFiles);
        List<Path> remoteFilesNotOnLocal = new ArrayList<>(watchedFiles);

        localFilesNotOnRemote.removeAll(watchedFiles);
        remoteFilesNotOnLocal.removeAll(localFiles);

        FileChangeFactory fileChangeFactory = new FileChangeFactory();
        for (Path path : localFilesNotOnRemote) {
            if (path != watchedDirectory) {
                System.out.println("Sending " + path.toString() + " to server.");
                changeSubmitter.submitFileChange(fileChangeFactory.getFileChange(username, path, ChangeType.CREATE));
            }
        }
        
        for (Path path : remoteFilesNotOnLocal) {
            System.out.println("Creating missing file " + path.toString() + " from server.");
            path.toFile().createNewFile();
        }
    }

    public static List<Path> restoreFilesFromServer(String username) throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(SERVICE_ADDRESS);

        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("username", username));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        JSONObject myObject = new JSONObject(EntityUtils.toString(entity));
        return toList(myObject);
    }

    private static List<Path> toList(JSONObject json) {
        Map<String, Object> sourceMap = json.toMap();
        List<Path> targetList = new ArrayList<>();

        for (Entry<String, Object> entry : sourceMap.entrySet()) {
            targetList.add(Paths.get(entry.getKey()));
        }
        return targetList;
    }

}
