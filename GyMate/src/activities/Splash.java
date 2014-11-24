package activities;

import com.gen.gymate.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splash extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		Thread timer = new Thread(){
			public void run()
			{
				try{
					sleep(3000); //3 seconds
				}catch(InterruptedException ex){
					ex.printStackTrace();
				}finally
				{
					Intent initilizeGymate = new Intent("activities.MainActivity");
					startActivity(initilizeGymate);
				}
			}
		};
		timer.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}