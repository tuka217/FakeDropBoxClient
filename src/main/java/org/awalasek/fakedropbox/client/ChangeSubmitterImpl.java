package org.awalasek.fakedropbox.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.awalasek.FakeDropBox.common.FileChange;

public class ChangeSubmitterImpl implements ChangeSubmitter {

    private String username;
    
    public ChangeSubmitterImpl(String username) {
        this.username = username;
    }
    
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
        HttpPost httppost = new HttpPost("http://172.17.0.2:8080/FakeDropBox/fileChange");

        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("filename", fileChange.getFilePath()));
        params.add(new BasicNameValuePair("fileEvent", fileChange.getChangeType().toString()));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                // do something useful
            } finally {
                instream.close();
            }
        }

    }
}
