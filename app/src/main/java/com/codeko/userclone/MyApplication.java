package com.codeko.userclone;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        /*Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("a54a21f1b0fe509aaa4b6ed3805fddbc5e21facd")
                .clientKey("f11ca4c5a8a3bb0d85d0a0d72696911b87a156aa")
                .server("http://ec2-54-191-135-160.us-west-2.compute.amazonaws.com:80/parse/")
                .build()
        );*/

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("instagrambt8HxR7SFA&209l")
                .clientKey("instagramVEfzoSRF9j1Y0xp")
                .server("https://instagramandroid-codeko.herokuapp.com/parse/")
                .build()
        );

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
