package com.example.testnetwork;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

  Button b1, b2;
  TextView textView;

  private ConnectivityManager.NetworkCallback mWifiNetworkCallback, mMobileNetworkCallback;
  private Network mWifiNetwork, mMobileNetwork;

  NetworkType networkType;

  private static final String URL_DEV = "https://dev.uvertz.lftechnology.com/api/v1";
  private static final String URL_UVERTZ = "http://192.168.4.1:3000/api/v1/status";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    b1 = findViewById(R.id.button);
    b2 = findViewById(R.id.button2);

    textView = findViewById(R.id.textView);

    networkType = new NetworkType(this);

    enableStrictMode();

    final ConnectivityManager manager =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    if (mWifiNetworkCallback == null) {
      //Init only once
      mWifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(final Network network) {
          try {
            mWifiNetwork = network;
          } catch (NullPointerException npe) {
            npe.printStackTrace();
          }
        }
      };
    }

    if (mMobileNetworkCallback == null) {
      //Init only once
      mMobileNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(final Network network) {
          try {
            mMobileNetwork = network;
          } catch (NullPointerException npe) {
            npe.printStackTrace();
          }
        }
      };
    }

    NetworkRequest.Builder wifiBuilder;
    wifiBuilder = new NetworkRequest.Builder();
    wifiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
    manager.requestNetwork(wifiBuilder.build(), mWifiNetworkCallback);

    NetworkRequest.Builder mobileNwBuilder;
    mobileNwBuilder = new NetworkRequest.Builder();
    mobileNwBuilder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
    manager.requestNetwork(mobileNwBuilder.build(), mMobileNetworkCallback);

    b1.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        makeHTTPRequest(URL_UVERTZ, mWifiNetwork);
        //makeHTTPRequest(URL_GOOGLE, networkType.get(NetworkConstants.NETWORK_WIFI));
      }
    });

    b2.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        makeHTTPRequest(URL_DEV, mMobileNetwork);
        //makeHTTPRequest(URL_GOOGLE, networkType.get(NetworkConstants.NETWORK_CELLULAR));
      }
    });
  }

  public void makeHTTPRequest(final String httpUrl, Network network) {
    try {
      URL url = new URL(httpUrl);
      HttpURLConnection conn = (HttpURLConnection) network.openConnection(url);
      conn.setRequestMethod("GET");

      final int responseCode = conn.getResponseCode();
      final String statusMessage = conn.getResponseMessage();
      Log.e("Code", String.valueOf(responseCode));
      Log.e("Status", statusMessage);
      Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();

      String response = getResponse(conn.getInputStream());
      textView.setText(response);
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getResponse(InputStream inputStream) throws IOException {

    String response;

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    StringBuffer buffer = new StringBuffer();

    while ((response = reader.readLine()) != null) {
      buffer.append(response + "\n");
      Log.d("Response: ", response);
    }

    return buffer.toString();
  }

  public void enableStrictMode() {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
  }
}
