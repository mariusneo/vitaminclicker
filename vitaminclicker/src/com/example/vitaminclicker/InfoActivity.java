package com.example.vitaminclicker;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InfoActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		
		// gitHubLink has links specified by putting <a> tags in the string
	    // resource.  By default these links will appear but not
	    // respond to user input.  To make them active, you need to
	    // call setMovementMethod() on the TextView object.

	    TextView gitHubLink = (TextView) findViewById(R.id.gitHubLink);
	    gitHubLink.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
}
