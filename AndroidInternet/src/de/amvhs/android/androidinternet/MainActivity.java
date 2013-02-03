package de.amvhs.android.androidinternet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
  private ProgressBar _Progress = null;
  private WebView     _Content  = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialisierung der Elemente
    _Progress = (ProgressBar) findViewById(R.id.progress);
    _Content = (WebView) findViewById(R.id.webContent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.opt_socket:
        Intent socketIntent = new Intent(this, SocketsActivity.class);
        startActivity(socketIntent);
        break;

      case R.id.opt_reload:
        BackgroundLoader loader = new BackgroundLoader(_Progress, _Content);
        loader.execute("http://blog.webducer.de/feed/");
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStart() {
    BackgroundLoader loader = new BackgroundLoader(_Progress, _Content);
    // loader.execute("http://www.vogella.com/article.rss");
    loader.execute("http://blog.webducer.de/feed/");
    super.onStart();
  }
}
