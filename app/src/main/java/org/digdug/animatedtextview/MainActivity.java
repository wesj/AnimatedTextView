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
        builder.append("world!", new ForegroundColorSpan(Color.BLUE), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
        spacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text.setSpacing((int) ((seekBar.getProgress() * 100 / 100)));
            }
        });

        final SeekBar duration = (SeekBar) findViewById(R.id.duration);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                text.setDuration(2000 * seekBar.getProgress() / 100);
            }
        });

        Spinner transitions = (Spinner) findViewById(R.id.transitions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transitions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transitions.setAdapter(adapter);
        transitions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence obj = (CharSequence) parent.getItemAtPosition(position);
                if (obj.equals("Fade")) {
                    AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
                    text.setShowAnimation(alpha);

                    alpha = new AlphaAnimation(1f, 0f);
                    text.setHideAnimation(new AlphaAnimation(1f, 0f));
                } else if (obj.equals("Rotate")) {
                    AnimationSet set = new AnimationSet(true);
                    set.addAnimation(new RotateAnimation(-90, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, -0.5f));
                    set.addAnimation(new AlphaAnimation(0, 1));
                    text.setShowAnimation(set);

                    set = new AnimationSet(true);
                    set.addAnimation(new RotateAnimation(0, -90, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, -0.5f));
                    set.addAnimation(new AlphaAnimation(1, 0));
                    text.setHideAnimation(set);
                } else if (obj.equals("Drop in")) {
                    TranslateAnimation anim = new TranslateAnimation(0f, 0f, -50f, 0f);
                    text.setShowAnimation(anim);

                    anim = new TranslateAnimation(0f, 0f, 0f, 50f);
                    text.setHideAnimation(anim);
                } else if (obj.equals("Rise up")) {
                    TranslateAnimation anim = new TranslateAnimation(0f, 0f, 50f, 0f);
                    anim.setInterpolator(new OvershootInterpolator());
                    text.setShowAnimation(anim);

                    anim = new TranslateAnimation(0f, 0f, 0f, -50f);
                    anim.setInterpolator(new OvershootInterpolator());
                    text.setHideAnimation(anim);
                } else if (obj.equals("Squash")) {
                    Animation scale = new ScaleAnimation(0f, 1f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.5f);
                    text.setShowAnimation(scale);

                    scale = new ScaleAnimation(1f, 0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.5f);
                    text.setHideAnimation(scale);
                } else if (obj.equals("Zoom")) {
                    AnimationSet set = new AnimationSet(true);
                    set.addAnimation(new ScaleAnimation(5f, 1f, 5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.5f));
                    set.addAnimation(new AlphaAnimation(0, 1));
                    text.setShowAnimation(set);

                    set = new AnimationSet(true);
                    set.addAnimation(new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.5f));
                    set.addAnimation(new AlphaAnimation(1, 0));
                    text.setHideAnimation(set);
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
