package de.amvhs.android.androidinternet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class BackgroundLoader extends AsyncTask<String, Integer, String> {
  private final ProgressBar   _Progress;
  private final WebView       _Content;
  private final static String _HEADER = "<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset=\"UTF-8\" />" + "</head><body>";
  private final static String _FOOTER = "</body></html>";
  private final static String _BASE   = "fake://base";

  public BackgroundLoader(ProgressBar progress, WebView webContent) {
    _Progress = progress;
    _Content = webContent;
  }

  @Override
  protected void onPreExecute() {
    // Progress anzeigen, WebView ausschalten
    _Progress.setVisibility(View.VISIBLE);
    _Content.setVisibility(View.GONE);
    super.onPreExecute();
  }

  @Override
  protected void onPostExecute(String result) {
    // Progress ausschalten, WebView anzeigen
    _Progress.setVisibility(View.GONE);
    _Content.setVisibility(View.VISIBLE);
    if (result != null && !"".equals(result)) {
      _Content.loadDataWithBaseURL(_BASE, result, "text/html", "UTF-8", null);
    } else {
      _Content.loadDataWithBaseURL(_BASE, _HEADER + "Keine Daten vorhanden!" + _FOOTER, "text/html", "UTF-8", null);
    }
    super.onPostExecute(result);
  }

  @Override
  protected void onCancelled() {
    // Progress ausschalten, WebView anzeigen
    _Progress.setVisibility(View.GONE);
    _Content.setVisibility(View.VISIBLE);

    _Content.loadDataWithBaseURL(_BASE, _HEADER + "Fehler bei der Daten-Übertragung!" + _FOOTER, "text/html", "UTF-8", null);
    super.onCancelled();
  }

  @Override
  protected String doInBackground(String... urls) {
    String returnValue = null;
    if (urls == null || urls.length != 1) {
      // Verarbeitung abbrechen
      this.cancel(true);
    } else {
      try {
        URL url = new URL(urls[0]);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000); // 10 Sekunden
        conn.setConnectTimeout(10000); // 10 Sekunden
        conn.setRequestMethod("GET"); // Verbindungstyp
        conn.setDoInput(true); // Daten erwartet

        // Verbindung starten
        conn.connect();

        // Verbindung prüfen (200 => OK)
        if (conn.getResponseCode() == 200) {
          // Parsen von XML
          returnValue = parseXML(conn.getInputStream());
        } else {
          // Verarbeitung abbrechen
          this.cancel(true);
        }

        // Verbindung trennen
        conn.disconnect();

      } catch (MalformedURLException e) {
        e.printStackTrace();
        // Verarbeitung abbrechen
        this.cancel(true);
      } catch (IOException e) {
        e.printStackTrace();
        // Verarbeitung abbrechen
        this.cancel(true);
      }
    }
    return returnValue;
  }

  private String parseXML(InputStream is) {
    StringBuilder htmlOut = new StringBuilder();
    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(is, "UTF-8");

      // XML Auslesen, bis dieser zu Ende ist
      while (parser.next() != XmlPullParser.END_DOCUMENT) {
        // item Element suchen
        if ("item".equals(parser.getName()) && parser.getEventType() == XmlPullParser.START_TAG) {
          // Verarbeitung innerhalb des Items
          while (parser.next() != XmlPullParser.END_TAG && !"item".equals(parser.getName())) {
            // Titel gefunden
            if ("title".equals(parser.getName()) && parser.getEventType() == XmlPullParser.START_TAG) {
              // Verarbeitung des Titles
              while (parser.next() != XmlPullParser.END_TAG && !"title".equals(parser.getName())) {
                if (parser.getEventType() == XmlPullParser.TEXT) {
                  htmlOut.append("<h3>").append(parser.getText()).append("</h3>");
                }
              }
            }
            // Beschreibung gefunden
            else if ("description".equals(parser.getName()) && parser.getEventType() == XmlPullParser.START_TAG) {
              // Verarbeitung der Beschreibung
              while (parser.next() != XmlPullParser.END_TAG && !"description".equals(parser.getName())) {
                if (parser.getEventType() == XmlPullParser.TEXT) {
                  htmlOut.append("<p align=\"justify\">").append(parser.getText()).append("</p>");
                }
              }
            }
            // Link gefunden
            else if ("link".equals(parser.getName()) && parser.getEventType() == XmlPullParser.START_TAG) {
              // Verarbeitung der Beschreibung
              while (parser.next() != XmlPullParser.END_TAG && !"link".equals(parser.getName())) {
                if (parser.getEventType() == XmlPullParser.TEXT) {
                  htmlOut.append("<p align=\"right\"><a href=\"").append(parser.getText()).append("\">").append("more ...").append("</a></p>");
                }
              }
            } else if (parser.getEventType() == XmlPullParser.START_TAG) {
              String name = parser.getName();
              while (parser.next() != XmlPullParser.END_TAG && !name.equals(parser.getName())) {
                // Skip
              }
            }
          }
        } else {
          // Keine Aktion - Zum nächsten Element gehen
        }
      }
    } catch (XmlPullParserException e) {
      e.printStackTrace();
      // Verarbeitung abbrechen
      this.cancel(true);
    } catch (IOException e) {
      e.printStackTrace();
      // Verarbeitung abbrechen
      this.cancel(true);
    }

    return _HEADER + htmlOut.toString() + _FOOTER;
  }
}
