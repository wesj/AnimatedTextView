package org.digdug.animatedtextview;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.digdug.widget.AnimatedTextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private static final String LOGTAG = MainActivity.class.getSimpleName();
    int state = 0;
    private AnimatedTextView text;

    List<CharSequence> strings = new ArrayList<CharSequence>()  {{
            add("Hello world!");
            add("Something something");
            add("Hello world with more");
            add("Hello wesley");
            add("Bye   wes");
    }};

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Hello ");
        builder.append("world!");
        builder.setSpan(new ForegroundColorSpan(Color.BLUE), 6, 12, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        strings.add(builder);

        setContentView(R.layout.activity_main);

        text = (AnimatedTextView) findViewById(R.id.text);
        Button b = (Button) findViewById(R.id.toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state++;
                state = state % strings.size();
                text.toggleText(strings.get(state));
            }
        });

        SeekBar spacing = (SeekBar) findViewById(R.id.spacing);
        spacing.setProgress(text.getSpacing());
        spacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float scale = getResources().getDisplayMetrics().density;
                text.setSpacing(seekBar.getProgress());
            }
        });

        final SeekBar duration = (SeekBar) findViewById(R.id.duration);
        duration.setProgress(text.getDuration());
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text.setDuration(seekBar.getProgress());
            }
        });

        SeekBar textSizeSeekbar = (SeekBar) findViewById(R.id.textSizeSeekbar);
        final float scale = getResources().getDisplayMetrics().density;
        textSizeSeekbar.setProgress((int) ((text.getTextSize() - 10) / scale - 0.5));
        textSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text.setTextSize((int) (10 + seekBar.getProgress()));
            }
        });

        Spinner transitions = (Spinner) findViewById(R.id.transitions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transitions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transitions.setAdapter(adapter);
        transitions.setSelection(0, true);
        text.setAnimations(R.anim.fade_in, R.anim.fade_out);
        transitions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence obj = (CharSequence) parent.getItemAtPosition(position);
                if (obj.equals("Fade")) {
                    text.setAnimations(R.anim.fade_in, R.anim.fade_out);
                } else if (obj.equals("Rotate")) {
                    text.setAnimations(R.anim.rotate_in, R.anim.rotate_out);
                } else if (obj.equals("Drop in")) {
                    text.setAnimations(R.anim.drop_in, R.anim.drop_out);
                } else if (obj.equals("Rise up")) {
                    text.setAnimations(R.anim.rise_in, R.anim.rise_out);
                } else if (obj.equals("Squash")) {
                    text.setAnimations(R.anim.squash_in, R.anim.squash_out);
                } else if (obj.equals("Zoom")) {
                    text.setAnimations(R.anim.zoom_in, R.anim.zoom_out);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner gravities = (Spinner) findViewById(R.id.gravitySelector);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.gravities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gravities.setSelection(2, true);
        gravities.setAdapter(adapter);
        gravities.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence obj = (CharSequence) parent.getItemAtPosition(position);
                if (obj.equals("Left")) {
                    text.setGravity(Gravity.LEFT);
                } else if (obj.equals("Right")) {
                    text.setGravity(Gravity.RIGHT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        char key = (char) event.getUnicodeChar();
        text.toggleText(text.getText().toString() + key);
        super.onKeyDown(keyCode, event);
        return true;
    }
}
