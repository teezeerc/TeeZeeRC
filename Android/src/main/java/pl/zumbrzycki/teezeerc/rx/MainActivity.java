/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import pl.zumbrzycki.teezeerc.rx.MultitouchView.Mode;
import pl.zumbrzycki.teezeerc.rx.R;

/**
 * @author Tomasz Zumbrzycki Android Activity class responsible for
 *         reinitializing view after mode change, handling application exit
 * 
 */
public class MainActivity extends Activity {

	private MultitouchView multitouchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reinitializeView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	/**
	 * Method handles flight mode change. Reinitializes view with new mode
	 * settings
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_option_item_mode_1:
			multitouchView.getTask().cancel(true);
			multitouchView.setMODE(Mode.Mode_1);
			reinitializeView();
			Log.d("mode type", "1");
			return true;
		case R.id.settings_option_item_mode_2:
			multitouchView.getTask().cancel(true);
			multitouchView.setMODE(Mode.Mode_2);
			reinitializeView();
			Log.d("mode type", "2");
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Method rebuilds view
	 */
	private void reinitializeView() {
		setContentView(R.layout.activity_main);
		multitouchView = (MultitouchView) findViewById(R.id.multitouchView);
		multitouchView.getTask()
				.setView((TextView) findViewById(R.id.textview));
	}

	/**
	 * Method handles exit when back button is pressed. Cancels background task
	 * before exit.
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		multitouchView.getTask().cancel(true);
		finish();
	}

}
