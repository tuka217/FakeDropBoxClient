package org.awalasek.fakedropbox.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.awalasek.fakedropbox.common.FileChange;

public class ChangeSubmitterImpl implements ChangeSubmitter {

    private static final String SERVICE_ADDRESS = "http://172.17.0.2:8080/FakeDropBox/fileChange";
    
    @Override
    public void submitFileChange(FileChange fileChange) {
        try {
            sendPost(fileChange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPost(FileChange fileChange) throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(SERVICE_ADDRESS);

        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("username", fileChange.getUsername()));
        params.add(new BasicNameValuePair("filename", fileChange.getFilename()));
        params.add(new BasicNameValuePair("changeType", fileChange.getChangeType().toString()));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        httpclient.execute(httppost);
    }
}
