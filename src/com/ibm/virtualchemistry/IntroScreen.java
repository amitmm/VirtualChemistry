package com.ibm.virtualchemistry;

import com.ibm.virtualchemistry.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class IntroScreen extends Activity {

	@Override
	protected void onCreate(Bundle UBT) {
		super.onCreate(UBT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.intro_screen);
		
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(2000); //2000ms = 2 seconds								
				} catch (InterruptedException e){
					e.printStackTrace();
				} finally{
					Intent openStartingPoint = new Intent("com.ibm.virtualchemistry.MainActivity");
					startActivity(openStartingPoint); 
				}
			}			
		};
		
		timer.start();
	}
}