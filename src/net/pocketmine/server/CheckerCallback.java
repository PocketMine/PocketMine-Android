package net.pocketmine.server;

import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;

public class CheckerCallback implements LicenseCheckerCallback {

	@Override
	public void allow(int reason) {
		//
	}

	@Override
	public void dontAllow(int reason) {
		if(HomeActivity.isStarted){
			ServerUtils.stopServer();
		}
		
		if(reason != Policy.RETRY)
			HomeActivity.hangUp();
	}

	@Override
	public void applicationError(int errorCode) {
		// nah
	}

}
