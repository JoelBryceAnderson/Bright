package joelbryceanderson.com.bright.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import joelbryceanderson.com.bright.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Check if app is in dark mode, switch theme if it is
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            setTheme(R.style.AppThemeNight);
        }


        //Set up the views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setUpToolBar();
        if (darkMode) {
            setUpDarkMode();
        }
    }

    private void setUpDarkMode() {
        CardView cardView = (CardView) findViewById(R.id.about_card);
        TextView aboutAppName = (TextView) findViewById(R.id.about_app_name);
        TextView aboutAppCreator = (TextView) findViewById(R.id.about_app_creator);
        TextView aboutAppVersion = (TextView) findViewById(R.id.about_version);

        //Adjust View Colors for DarkMode
        cardView.setCardBackgroundColor(Color.parseColor("#263238"));
        aboutAppName.setTextColor(Color.parseColor("#ffffff"));
        aboutAppCreator.setTextColor(Color.parseColor("#ffffff"));
        aboutAppVersion.setTextColor(Color.parseColor("#ffffff"));
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpToolBar() {
        //Set up the Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Finish activity if close button is pressed
        finish();
        return false;
    }
}
