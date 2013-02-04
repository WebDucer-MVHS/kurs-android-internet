package de.amvhs.android.androidinternet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SocketsActivity extends Activity {
  private EditText _Message = null;
  private TextView _Result  = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_sockets);
    super.onCreate(savedInstanceState);

    // Initialisierung der Elemente
    _Message = (EditText) findViewById(R.id.txtMessage);
    _Result = (TextView) findViewById(R.id.txtResult);
    Button send = (Button) findViewById(R.id.cmdSend);
    send.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        SendSocketThread sendThread = new SendSocketThread();
        sendThread.execute(_Message.getText().toString());
      }
    });
  }

  private class SendSocketThread extends AsyncTask<String, Void, String> {

    @Override
    protected void onPostExecute(String result) {
      if (result != null) {
        _Result.append("\n");
        _Result.append(result);
      }
      super.onPostExecute(result);
    }

    @Override
    protected String doInBackground(String... params) {
      String returnValue = null;
      if (params != null && params.length == 1) {
        String message = params[0];
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
          socket = new Socket("192.168.178.32", 8888);
          dos = new DataOutputStream(socket.getOutputStream());
          dis = new DataInputStream(socket.getInputStream());
          // Senden der Nachricht an den Server
          dos.writeUTF(message);
          // Lesen der Antwort von dem Server
          returnValue = dis.readUTF();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          // Socket schließen
          if (socket != null) {
            try {
              socket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          // Output schließen
          if (dos != null) {
            try {
              dos.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          // Input schließen
          if (dis != null) {
            try {
              dis.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
      return returnValue;
    }
  }
}
