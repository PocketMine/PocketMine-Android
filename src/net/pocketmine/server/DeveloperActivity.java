package net.pocketmine.server;

import java.util.ArrayList;

import net.pocketmine.forum.PluginListManager;
import net.pocketmine.forum.PluginListManager.PluginDownloadInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DeveloperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developer);

		Button localPluginsEditor = (Button) findViewById(R.id.developer_plugin_edit);
		localPluginsEditor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						DeveloperActivity.this);
				b.setTitle("Edit local plugin list");
				PluginListManager.load();
				final ArrayList<PluginDownloadInfo> p = PluginListManager.plugins;
				CharSequence[] items = new CharSequence[p.size()];
				for (int i = 0; i < p.size(); i++) {
					items[i] = i + "=" + p.get(i).id + ": " + p.get(i).filename;
				}
				b.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// display actions
						final int id = which;
						AlertDialog.Builder b = new AlertDialog.Builder(
								DeveloperActivity.this);
						b.setTitle("Edit #" + id);
						CharSequence[] actions = new CharSequence[1];
						actions[0] = "Set last update time to 0";
						b.setItems(actions, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(which == 0){
									p.get(id).updated = 0;
									PluginListManager.save();
								}
							}
						});
						b.show();
					}
				});
				b.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.developer, menu);
		return true;
	}

}
