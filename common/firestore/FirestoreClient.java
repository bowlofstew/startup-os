/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.startupos.common.firestore;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

// TODO: Fix open Firestore rules
public class FirestoreClient {
  // Base path formatted by project name and path, that starts with a /.
  private static final String BASE_PATH =
          "https://firestore.googleapis.com/v1beta1" + "/projects/%s/databases/(default)/documents%s";

  private final String firestoreProject;

  public FirestoreClient(String firestoreProject){
    this.firestoreProject = firestoreProject;
  }

  private String getGetUrl(String path) {
    return String.format(BASE_PATH, firestoreProject, path);
  }

  private String getCreateDocumentUrl(String user) {
    // GET and CreateDocument are the same if the server selects the ID.
    return getGetUrl(user);
  }

  public Message getDocument(String path, Message.Builder proto) {
    try {
      StringBuilder result = new StringBuilder();
      URL url = new URL(getGetUrl(path));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        result.append(line);
      }
      reader.close();
      if (connection.getResponseCode() != HTTP_OK) {
        throw new IllegalStateException("getDocument failed: " + connection.getResponseMessage());
      }
      FirestoreJsonFormat.parser().merge(result.toString(), proto);
      return proto.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void createDocument(String path, MessageOrBuilder proto) {
    try {
      URL url = new URL(getCreateDocumentUrl(path));

      String prototxt = JsonFormat.printer().print(proto);
      String json = FirestoreJsonFormat.printer().print(proto);
      byte[] postDataBytes = json.getBytes("UTF-8");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
      connection.setDoOutput(true);
      connection.getOutputStream().write(postDataBytes);

      Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

      for (int c; (c = in.read()) >= 0; ) System.out.print((char) c);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}