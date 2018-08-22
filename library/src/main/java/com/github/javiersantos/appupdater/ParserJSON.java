package com.github.javiersantos.appupdater;

import com.github.javiersantos.appupdater.objects.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

class ParserJSON {
    private URL jsonUrl;
    private HttpURLConnection jsonConnection;
    private Map<String, String> propertiesConnection;

    private static final String KEY_LATEST_VERSION = "latestVersion";
    private static final String KEY_LATEST_VERSION_CODE = "latestVersionCode";
    private static final String KEY_RELEASE_NOTES = "releaseNotes";
    private static final String KEY_URL = "url";
    private static final String KEY_APPLY_TO = "applyTo";

    public ParserJSON(String url) throws IOException {
        this.jsonUrl = new URL(url);
        this.jsonConnection = (HttpURLConnection) jsonUrl.openConnection();
    }

    public ParserJSON(String url, Map<String, String> propertiesConnection) throws IOException {
        this.jsonUrl = new URL(url);
        this.jsonConnection = (HttpURLConnection) jsonUrl.openConnection();
        this.propertiesConnection = propertiesConnection;
    }

    public Update parse() throws JSONException, IOException {

        JSONObject json = readJsonFromUrl();
        Update update = new Update();
        update.setLatestVersion(json.getString(KEY_LATEST_VERSION).trim());
        update.setLatestVersionCode(json.optInt(KEY_LATEST_VERSION_CODE));
        JSONArray releaseArr = json.optJSONArray(KEY_RELEASE_NOTES);
        JSONArray applyTo = json.optJSONArray(KEY_APPLY_TO);
        if (releaseArr != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < releaseArr.length(); ++i) {
                builder.append(releaseArr.getString(i).trim());
                if (i != releaseArr.length() - 1)
                    builder.append(System.getProperty("line.separator"));
            }
            update.setReleaseNotes(builder.toString());
        }
        if (applyTo != null) {
            String appVersionName = UtilsLibrary.getAppVersionName(AppUpdater.getInstance().context);
            String appVersionCode = UtilsLibrary.getAppVersionCode(AppUpdater.getInstance().context);
            switch (applyTo.length()) {
                case 0: {
                    break;
                }
                case 1: {
                    if (applyTo.getString(0).equals("*")) {
                        AppUpdater.getInstance().appRequireUpdate = true;
                    } else {
                        String applyVersion = applyTo.getString(0);//.replaceAll("\\.", "");
                        if (applyVersion.equals(appVersionName) || applyVersion.equals(appVersionCode)) {
                            AppUpdater.getInstance().appRequireUpdate = true;
                        }
                    }
                    break;
                }
                default: {
                    for (int i = 0; i < applyTo.length(); i++) {
                        String applyVersion = applyTo.getString(i);//.replaceAll("\\.", "");
                        if (applyVersion.equals(appVersionName) || applyVersion.equals(appVersionCode)) {
                            AppUpdater.getInstance().appRequireUpdate = true;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        URL url = new URL(json.getString(KEY_URL).trim());
        update.setUrlToDownload(url);
        return update;
        /*} catch (IOException e) {
            Log.e("AppUpdater", "The server is down or there isn't an active Internet connection.", e);
        } catch (JSONException e) {
            Log.e("AppUpdater", "The JSON updater file is mal-formatted. AppUpdate can't check for updates.");
        }*/
    }


    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JSONObject readJsonFromUrl() throws IOException, JSONException {
        jsonConnection.setRequestMethod("GET");
        if (propertiesConnection != null) {
            for (String key : propertiesConnection.keySet()) {
                if (key.equals("Method")) {
                    jsonConnection.setRequestMethod(propertiesConnection.get(key));
                } else {
                    jsonConnection.setRequestProperty(key, propertiesConnection.get(key));
                }
            }
        }
        jsonConnection.setDoOutput(true);
        InputStream is = jsonConnection.getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

}
