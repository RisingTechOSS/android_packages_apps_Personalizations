/*
 * Copyright (C) 2016-2021 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.fragments.about;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.PreferenceFragment;

import com.android.settings.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangelogFragment extends PreferenceFragment {

    TextView textView;

    private static final String README_URL = "https://raw.githubusercontent.com/RisingTechOSS/risingOS_changelogs/fourteen/README.md";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changelog, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = view.findViewById(R.id.changelog_text);
        new FetchReadmeTask().execute(README_URL);
    }

    private class FetchReadmeTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String readmeUrl = params[0];
            try {
                URL url = new URL(readmeUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                inputStreamReader.close();
                return stringBuilder.toString();
            } catch (IOException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String htmlContent = Html.fromHtml(result).toString();
                htmlContent = htmlContent.replaceAll("#### ", "\n ");
                htmlContent = htmlContent.replaceAll("### ", "\n ");
                htmlContent = htmlContent.replaceAll("## ", "\n ");
                htmlContent = htmlContent.replaceAll("---", "\n   \n");
                htmlContent = htmlContent.replaceAll("- ", "\n- ");
                
                textView.setText(htmlContent);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
}
