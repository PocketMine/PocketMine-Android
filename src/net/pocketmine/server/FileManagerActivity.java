/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package net.pocketmine.server;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import net.pocketmine.server.R;

public class FileManagerActivity extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_manager);

		//
	}
}