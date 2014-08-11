package net.pocketmine.server;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import net.pocketmine.server.R;

public class InstallActivity extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_activity);

		//
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		if (!ServerUtils.checkIfInstalled()) {
			HomeActivity.btn_runServer.setEnabled(false);
			HomeActivity.btn_stopServer.setEnabled(false);
			InstallerAsync ia = new InstallerAsync();
			ia.ctx = this;
			ia.fromWhichAct = 0;
			ia.fromAssets = true;
			ia.toLoc = ServerUtils.getAppDirectory() + "/";
			ia.orgLoc = "data.zip";
			ia.tv_install_exec = (TextView) findViewById(R.id.installProgress);
			ia.tv_install_exec.setVisibility(1);
			ia.execute();
		}
	}
	
	public void contiuneInstall(){
		if(HomeActivity.prefs!=null){
			SharedPreferences.Editor spe = HomeActivity.prefs.edit();
			spe.putInt("filesVersion", 4);
			spe.commit();
		}
		Intent ver = new Intent(this, VersionManagerActivity.class);
		ver.putExtra("install", true);
		startActivity(ver);
		finish();
	}
}