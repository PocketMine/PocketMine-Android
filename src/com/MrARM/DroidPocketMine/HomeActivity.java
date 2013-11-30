/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.MrARM.DroidPocketMine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

/**
 * Activity to Home Screen
 */

@android.annotation.TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HomeActivity extends SherlockActivity {

	// private final String TAG = "com.github.com.DroidPHP";
	final static int PROJECT_CODE = 143;
	final static int VERSION_MANAGER_CODE = PROJECT_CODE + 1;
	final static int FILE_MANAGER_CODE = VERSION_MANAGER_CODE + 1;
	final static int FORCE_CLOSE_CODE = FILE_MANAGER_CODE + 1;
	final static int ABOUT_US_CODE = FORCE_CLOSE_CODE + 1;
	final static int CONSOLE_CODE = ABOUT_US_CODE + 1;
	public static HashMap<String, String> server;
	public static SharedPreferences prefs;

	private final Context mContext = HomeActivity.this;
	public static HomeActivity ha = null;
	

	/**
	 * Buttons for managing server state
	 */
	public static Button btn_runServer;
	public static Button btn_stopServer;
	public static Intent servInt;
	public static Boolean isStarted = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		ha = this;
		setContentView(R.layout.home);
		// startService(new Intent(mContext, ServerService.class));
		ServerUtils.StrictModePermitAll();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//ServerUtils.setHttpDocsUri(prefs.getString("k_docs_dir", "htdocs"));
		//ServerUtils.setServerPort(prefs.getString("k_server_port", "8080"));

		ServerUtils.setContext(mContext);

		btn_runServer = (Button) findViewById(R.id.RunTime_Http);
		btn_stopServer = (Button) findViewById(R.id.RunTime_Http_Kill);

		
		btn_runServer.setEnabled(!isStarted);
		btn_runServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ServerUtils.runServer();
				btn_runServer.setEnabled(false);
				btn_stopServer.setEnabled(true);
				btn_runServer.setText(R.string.server_online);
				String msg = "Unable to start server";
				if (ServerUtils.isRunning()) {
					msg = "Server is now running";
				}
				android.widget.Toast.makeText(mContext, msg,
						android.widget.Toast.LENGTH_LONG).show();

				servInt = new Intent(mContext, ServerService.class);

				//i.putExtra(ServerService.EXTRA_PORT,
				//		prefs.getString("k_server_port", "8080"));

				startService(servInt);
				isStarted = true;

			}
		});

		btn_stopServer.setEnabled(isStarted);
		btn_stopServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if(ServerUtils.isRunning()){
					LogActivity.log("[DroidPocketMine] Stopping server...");
					ServerUtils.executeCMD("stop");
				}
				
			}
		});

	}
	
	public static void stopNotifyService(){
		
		 if(ha != null && servInt != null){
			 ha.runOnUiThread(new Runnable(){
				    public void run(){
						 isStarted = false;
						 ha.stopService(servInt);
						 ha.btn_runServer.setEnabled(true);
						 btn_stopServer.setEnabled(false);
				    }
			 });
		 }
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!ServerUtils.checkIfInstalled()) {
			/*btn_runServer.setEnabled(false);
			btn_stopServer.setEnabled(false);
			InstallerAsync ia = new InstallerAsync();
			ia.tv_install_exec = (TextView) findViewById(R.id.tv_bin);
			ia.tv_install_exec.setVisibility(1);
			ia.tv_install_exec.setText(R.string.install_bin);
			ia.execute();*/
			startActivity(new Intent(mContext, InstallActivity.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONSOLE_CODE, 0, "Console").setIcon(R.drawable.hardware_dock)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu sub = menu.addSubMenu(getString(R.string.abs_settings));
		/**
		 * Set Icon for Submenu
		 */
		sub.setIcon(R.drawable.action_settings);
		/**
		 * Build navigation for submenu
		 */
		//sub.add(0, PROJECT_CODE, 0, getString(R.string.abs_project));
		// sub.add(0, DEV_CODE, 0, getString(R.string.abs_dev));
		sub.add(0, VERSION_MANAGER_CODE, 0, getString(R.string.abs_version_manager));
		//sub.add(0, FILE_MANAGER_CODE, 0, getString(R.string.abs_file_manager));
		sub.add(0, FORCE_CLOSE_CODE, 0, getString(R.string.abs_force_close));
		sub.add(0, ABOUT_US_CODE, 0, getString(R.string.abs_about));
		//sub.add(0, SETTING_CODE, 0, getString(R.string.abs_settings));
		sub.getItem().setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
			return false;
		}

		if (item.getItemId() == FILE_MANAGER_CODE) {
			
			startActivity(new Intent(mContext, FileManagerActivity.class));

		} else if (item.getItemId() == VERSION_MANAGER_CODE) {
			
			startActivity(new Intent(mContext, VersionManagerActivity.class));

		} else if(item.getItemId() == FORCE_CLOSE_CODE){
			
			btn_runServer.setEnabled(true);
			btn_stopServer.setEnabled(false);
			ServerUtils.stopServer();
			if(servInt != null) stopService(servInt);
			isStarted = false;
			
		} else if (item.getItemId() == ABOUT_US_CODE) {

			startActivity(new Intent(mContext, About.class));

		} else if (item.getItemId() == CONSOLE_CODE) {
			startActivity(new Intent(mContext, LogActivity.class));
		}

		return true;
	}

	/*final protected boolean isServerRunning() throws IOException {
		InputStream is;
		java.io.BufferedReader bf;
		boolean isRunning = false;
		try {
			is = Runtime.getRuntime().exec("ps").getInputStream();
			bf = new java.io.BufferedReader(new java.io.InputStreamReader(is));

			String r;
			while ((r = bf.readLine()) != null) {
				if (r.contains("php")) {
					isRunning = true;
					break;
				}

			}
			is.close();
			bf.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return isRunning;

	}*/


}
