package com.e4all.main;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

abstract class SendRequest {
    URL urlObject;
    HttpURLConnection connection;
    BufferedReader in;
}
