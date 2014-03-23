package isola;

import com.isola.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Isola isola = new Isola(this, null);
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		CharSequence items[] = new CharSequence[] {"2 players", "4 players"};
		builder.setSingleChoiceItems(items, 0, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isola.players = (which==0)?2:4;
			}
		});
		builder.setTitle("Select the players number");
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isola.init();
				setContentView(isola);
			}
		});
		builder.show();

		
		
		//setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
