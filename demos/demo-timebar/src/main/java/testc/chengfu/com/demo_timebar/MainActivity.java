package testc.chengfu.com.demo_timebar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chengfu.fuplayer.ui.DefaultTimeBar;
import com.chengfu.fuplayer.ui.TimeBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimeBar timeBar = findViewById(R.id.timeBar);

        timeBar.setDuration(10);
        timeBar.setBufferedPosition(8);
        timeBar.setPosition(5);
    }
}
