package org.digdug.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnimatedTextView extends TextView {
    private static final String LOGTAG = AnimatedTextView.class.getSimpleName();
    private Paint paint = new Paint();
    private Animation animation = new AlphaAnimation(0f, 1.0f);
    private Animation hideAnimation = new AlphaAnimation(1.0f, 0f);
    private int duration = 500;
    private int spacing = duration / 2;
    private Transformation transformation = new Transformation();
    private Transformation invertTransformation = new Transformation();
    private boolean autoSlide = false;

    public void setShowAnimation(final Animation animation) {
        if (animation.getDuration() == 0) {
            animation.setDuration(duration);
        }
        this.animation = animation;
    }

    public Animation getShowAnimation() {
        return animation;
    }

    public void setHideAnimation(final Animation animation) {
        if (animation.getDuration() == 0) {
            Log.i(LOGTAG, "Update duration " + duration);
            animation.setDuration(duration);
        }
        hideAnimation = animation;
    }

    public Animation getHideAnimation() {
        return hideAnimation;
    }

    public AnimatedTextView(Context context) {
        super(context);
        init();
    }

    public AnimatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        /*
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(new AlphaAnimation(0f, 1f));
        set.addAnimation(new RotateAnimation(-90f, 0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, -0.5f));
        set.setDuration(duration);
        animation = set;

        set = new AnimationSet(true);
        set.addAnimation(new AlphaAnimation(1f, 0f));
        set.addAnimation(new RotateAnimation(0f, 90f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, -0.5f));
        set.setDuration(duration);
        hideAnimation = set;

        animation.setDuration(duration);
        hideAnimation.setDuration(duration);
        */
        paint.setTextSize(getTextSize());
        paint.setColor(Color.BLACK);
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        if (animation != null) animation.setDuration(duration);
        if (hideAnimation != null) hideAnimation.setDuration(duration);
    }

    public int getDuration() {
        return duration;
    }

    public int getSpacing() {
        return spacing;
    }

    private enum TransitionType {
        MOVING, SHOWING
    }

    private static class Transition {
        float startPosition;
        float endPosition;
        long startTime;

        Character from;
        Character to;
        TransitionType type;
        public Integer fromColor = null;
        public Integer toColor = null;
    }

    public void toggleText(CharSequence text) {
        transitions.clear();

        CharSequence current = getText();
        float position = 0;
        float endPosition = 0;
        for (int i = 0; i < text.length(); i++) {
            char c1 = text.charAt(i);
            Transition transition = null;
            if (current.length() <= i) {
                transition = addTransition(null, c1, position, endPosition);
            } else {
                char c2 = current.charAt(i);
                if (c1 != c2) {
                    transition = addTransition(c2, c1, position, endPosition);
                } else {
                    transition = addTransition(c1, c1, position, endPosition);
                }
                float size = paint.measureText(current, i, i + 1);
                position += size;

                if (current instanceof Spanned) {
                    Spanned spanned = (Spanned) current;
                    Object[] spans = spanned.getSpans(i, i+1, Object.class);
                    for (Object span : spans) {
                        if (span instanceof ForegroundColorSpan) {
                            transition.fromColor = ((ForegroundColorSpan) span).getForegroundColor();
                        }
                    }
                }
            }

            if (text instanceof Spanned) {
                Spanned spanned = (Spanned) text;
                Object[] spans = spanned.getSpans(i, i+1, Object.class);
                for (Object span : spans) {
                    if (span instanceof ForegroundColorSpan) {
                        transition.toColor = ((ForegroundColorSpan) span).getForegroundColor();
                    }
                }
            }

            float size = paint.measureText(text, i, i + 1);
            endPosition += size;
        }

        for (int i = text.length(); i < current.length(); i++) {
            char c2 = current.charAt(i);
            addTransition(c2, null, position, position);
            float size = paint.measureText(current, i, i + 1);
            position += size;
        }

        animation.startNow();
        hideAnimation.startNow();

        setText(text);
        postInvalidateOnAnimation();
    }

    @Override
    public void postInvalidateOnAnimation() {
        if (Build.VERSION.SDK_INT >= 16) {
            super.postInvalidateOnAnimation();
        } else {
            postInvalidateDelayed(30);
        }
    }

    List<Transition> transitions = new ArrayList<Transition>();

    private Transition addTransition(Character from, Character to, float position, float endPosition) {
        Transition t = new Transition();
        t.from = from;
        t.to = to;
        t.startPosition = position;
        t.endPosition = endPosition;
        transitions.add(t);
        return t;
    }

    @Override
    public void onDraw(Canvas canvas) {
        CharSequence text = getText();
        float position = 0;

        if (transitions.size() > 0) {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long t  = animation.getStartTime() + animation.getDuration();
            long t2 = animation.getStartTime() + animation.getDuration();
            // Log.i(LOGTAG, "Times " + animation.getStartTime() + " + " + animation.getDuration());
            int i = 0;
            int numMoved = 0;
            for (Transition transition : transitions) {
                int prevColor = paint.getColor();
                float size = paint.measureText("" + transition.to, 0, 1);
                animation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());
                hideAnimation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());

                if (transition.from == transition.to && transition.fromColor == transition.toColor) {
                    // If nothing is changing, just draw the letter in place
                    transformation.clear();
                    invertTransformation.clear();

                    int save = canvas.save();
                    // float dt = animation.getInterpolator().getInterpolation((float) (now - animation.getStartTime()) / (float) animation.getDuration());
                    canvas.translate(transition.endPosition, paint.getTextSize());
                    canvas.drawText("" + transition.to, 0, 1, 0, 0, paint);
                    canvas.restoreToCount(save);
                } else {
                    float dt = 1;
                    float dt2 = 0;
                    // If this letter's animation hasn't started yet, draw it at its startTime
                    if (now - spacing * i < animation.getStartTime()) {
                        if (autoSlide) {
                            dt = 0;
                            dt2 = 0;
                        }
                        animation.getTransformation(animation.getStartTime(), transformation);
                        hideAnimation.getTransformation(hideAnimation.getStartTime(), invertTransformation);
                    } else if (now - spacing * i < t) {
                        // This letter is moving, get its transformation set up correctly
                        numMoved++;
                        animation.getTransformation(now - spacing * i, transformation);
                        hideAnimation.getTransformation(now - spacing * i, invertTransformation);
                        if (autoSlide) {
                            dt = animation.getInterpolator().getInterpolation((float) (now - spacing * i - animation.getStartTime()) / (float) animation.getDuration());
                            dt2 = hideAnimation.getInterpolator().getInterpolation((float) (now - spacing * i - hideAnimation.getStartTime()) / (float) hideAnimation.getDuration());
                        }
                    } else {
                        // This letter is done. Keep it at the end of its animation (so the animation doesn't end).
                        animation.getTransformation(t - 1, transformation);
                        hideAnimation.getTransformation(t2 - 1, invertTransformation);
                    }

                    if (transition.from != null) {
                        if (transition.fromColor != null) {
                            paint.setColor(transition.fromColor);
                        }
                        drawHiding(canvas, transition, dt2);
                        paint.setColor(prevColor);
                    }

                    if (transition.to != null) {
                        if (transition.toColor != null) {
                            paint.setColor(transition.toColor);
                        }
                        drawShowing(canvas, transition, dt);
                        paint.setColor(prevColor);
                    }

                    i++;
                }

                position += size;
            }

            // Log.i(LOGTAG, "Moved " + numMoved);
            if (numMoved == 0) {
                animation.getTransformation(now, transformation);
                hideAnimation.getTransformation(now, invertTransformation);
            }

            if (!animation.hasEnded() || !hideAnimation.hasEnded()) {
                postInvalidateOnAnimation();
            } else {
                transitions.clear();
            }
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            canvas.drawText(text, i, i + 1, position, paint.getTextSize(), paint);
            float size = paint.measureText(text, i, i + 1);
            position += size;
        }
    }

    private void drawShowing(Canvas canvas, Transition transition, float dt) {
        float alpha = 1.0f;
        if (transformation.getTransformationType() == Transformation.TYPE_ALPHA || transformation.getTransformationType() == Transformation.TYPE_BOTH) {
            alpha = transformation.getAlpha();
        }
        paint.setAlpha((int) (alpha * 255));

        int save = canvas.save();
        canvas.translate(transition.startPosition + dt * (transition.endPosition - transition.startPosition), paint.getTextSize());
        if (transformation.getTransformationType() == Transformation.TYPE_MATRIX || transformation.getTransformationType() == Transformation.TYPE_BOTH) {
            canvas.concat(transformation.getMatrix());
        }

        canvas.drawText("" + transition.to, 0, 1,
                0, 0,
                paint);
        canvas.restoreToCount(save);
    }

    float[] values = new float[9];
    private void drawHiding(Canvas canvas, Transition transition, float dt) {
        float alpha = 1.0f;
        if (invertTransformation.getTransformationType() == Transformation.TYPE_ALPHA ||
                invertTransformation.getTransformationType() == Transformation.TYPE_BOTH) {
            alpha = invertTransformation.getAlpha();
        }
        paint.setAlpha((int)(alpha * 255));

        int save = canvas.save();
        canvas.translate(transition.startPosition + dt * (transition.endPosition - transition.startPosition), paint.getTextSize());
        if (invertTransformation.getTransformationType() == Transformation.TYPE_MATRIX ||
                invertTransformation.getTransformationType() == Transformation.TYPE_BOTH) {
            canvas.concat(invertTransformation.getMatrix());
        }
        canvas.drawText("" + transition.from, 0, 1,
                0, 0,
                paint);
        canvas.restoreToCount(save);
    }
}
