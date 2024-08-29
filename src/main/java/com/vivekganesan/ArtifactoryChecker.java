package com.vivekganesan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArtifactoryChecker {

    public boolean checkDependencyInArtifactory(String jfrogUrl, String username, String password, String groupId, String artifactId, String version) throws Exception {
        String urlString = jfrogUrl + "/api/search/gavc?g=" + groupId + "&a=" + artifactId + "&v=" + version;
        URL url = new URL(urlString);
        HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return !content.toString().contains("\"results\":[]");
        } else {
            throw new RuntimeException("Failed to connect to Artifactory. HTTP error code: " + responseCode);
        }
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}