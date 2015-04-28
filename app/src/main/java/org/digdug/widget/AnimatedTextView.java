package org.digdug.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
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
    private int duration = 100;
    private int spacing = duration / 2;
    private boolean autoSlide = true;
    private CharSequence toString;

    private Animation showAnimation = new AlphaAnimation(0f, 1.0f);
    private Animation hideAnimation = new AlphaAnimation(1.0f, 0f);
    private Transformation showTransformation = new Transformation();
    private Transformation hideTransformation = new Transformation();

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

    @Override
    public void setTextSize(float size) {
        fromSize = getTextSize();
        final float scale = getResources().getDisplayMetrics().density;
        toSize = size * scale;

        toggleText(getText());

        fromSize = toSize = null;
        super.setTextSize(size);
    }

    private void init() {
        paint.setTextSize(getTextSize());
        paint.setColor(Color.BLACK);
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setDuration(int duration) {
        this.duration = duration;

        if (showAnimation != null) showAnimation.setDuration(duration);
        if (hideAnimation != null) hideAnimation.setDuration(duration);
    }

    public int getDuration() {
        return duration;
    }

    public int getSpacing() {
        return spacing;
    }

    public void setAnimations(@AnimRes int in, @AnimRes int out) {
        final Resources res = getResources();
        final Animation inAnim = AnimationUtils.loadAnimation(getContext(), in);
        final Animation outAnim = AnimationUtils.loadAnimation(getContext(), out);
        setAnimations(inAnim, outAnim);
    }

    private void setAnimations(Animation inAnim, Animation outAnim) {
        setShowAnimation(inAnim);
        setHideAnimation(outAnim);
    }

    public void setShowAnimation(@AnimRes final int animId) {
        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        setShowAnimation(anim);
    }

    public void setShowAnimation(final Animation animation) {
        if (animation.getDuration() == 0) {
            animation.setDuration(duration);
        }
        this.showAnimation = animation;
    }

    public Animation getShowAnimation() {
        return showAnimation;
    }

    public void setHideAnimation(@AnimRes final int animId) {
        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        setHideAnimation(anim);
    }

    public void setHideAnimation(final Animation animation) {
        Log.i(LOGTAG, "Set hide " + animation + " - " + animation.getDuration());
        if (animation.getDuration() == 0) {
            animation.setDuration(duration);
        }
        hideAnimation = animation;
    }

    public Animation getHideAnimation() {
        return hideAnimation;
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
        public Float fromSize = null;
        public Integer toColor = null;
        public Float toSize = null;
    }

    private enum Destination {
        FROM, TO;
    }
    private Integer fromColor = null;
    private Integer toColor = null;
    private int getColor(Destination dest) {
        if (dest == Destination.FROM && fromColor != null) {
            return fromColor;
        } else if (dest == Destination.TO && toColor != null) {
            return toColor;
        }
        return getCurrentTextColor();
    }

    private Float fromSize = null;
    private Float toSize = null;
    private float getTextSize(Destination dest) {
        if (dest.equals(Destination.FROM) && fromSize != null) {
            return fromSize;
        } else if (dest.equals(Destination.TO) && toSize != null) {
            return toSize;
        }
        return getTextSize();
    }

    public Transition addTransitionFor(int position, int currentI, float show, float hide) {
        Transition transition = null;
        CharSequence current = getText();
        char to = toString.charAt(position);

        if (currentI < 0 || currentI >= current.length()) {
            transition = addTransition(null, to, show, hide);
        } else {
            char from = current.charAt(currentI);
            transition = addTransition(from, to, show, hide);
        }
        // onDraw uses colors to determine if it should transition or not. Make sure we set one for now...
        // XXX - onDraw should be smarter here
        transition.fromColor = getForegroundColor(current, currentI, getColor(Destination.FROM));
        transition.fromSize = getTextSize(current, currentI, getTextSize(Destination.FROM));

        transition.toColor = getForegroundColor(toString, position, getColor(Destination.TO));
        transition.toSize = getTextSize(toString, position, getTextSize(Destination.TO));
        return transition;
    }

    private int getForegroundColor(CharSequence chars, int position, int defaultColor) {
        if (chars instanceof Spanned) {
            Spanned spanned = (Spanned) chars;
            Object[] spans = spanned.getSpans(position, position+1, Object.class);
            for (Object span : spans) {
                // Log.i(LOGTAG, "Span " + span);
                if (span instanceof ForegroundColorSpan) {
                    // Log.i(LOGTAG, "Get Foreground " + chars.subSequence(position, position+1) + ", " + ((ForegroundColorSpan) span).getForegroundColor())  ;
                    return ((ForegroundColorSpan) span).getForegroundColor();
                }
            }
        }
        return defaultColor;
    }

    private float getTextSize(CharSequence chars, int position, float defaultSize) {
        if (chars instanceof Spanned) {
            Spanned spanned = (Spanned) chars;
            Object[] spans = spanned.getSpans(position, position+1, Object.class);
            for (Object span : spans) {
                if (span instanceof TextAppearanceSpan) {
                    return ((TextAppearanceSpan) span).getTextSize();
                }
            }
        }
        return defaultSize;
    }

    public void toggleText(CharSequence text) {
        toString = text;
        transitions.clear();

        CharSequence current = getText();

        if ((getGravity() & Gravity.LEFT) == Gravity.LEFT) {
            toggleTextLeft(text, current);
        } else {
            toggleTextRight(text, current);
        }

        showAnimation.startNow();
        hideAnimation.startNow();

        setText(text);
        postInvalidateOnAnimation();
    }

    private void toggleTextRight(CharSequence text, CharSequence current) {
        float position = getWidth() - getPaddingRight();
        float endPosition = position;
        int currentI = current.length() - 1;

        for (int i = text.length() - 1; i >= 0; i--) {
            float startSize = 0;
            if (currentI >= 0) {
                startSize = paint.measureText(current, currentI, currentI + 1);
                position -= startSize;
            }

            float endSize = paint.measureText(text, i, i + 1);
            endPosition -= endSize;

            addTransitionFor(i, currentI, position, endPosition);
            currentI--;
        }

        for (int i = currentI; i >= 0; i--) {
            char c2 = current.charAt(i);
            paint.setTextSize(getTextSize(current, i, getTextSize(Destination.FROM)));
            float fromSize = paint.measureText(current, i, i + 1);

            paint.setTextSize(getTextSize(current, i, getTextSize(Destination.TO)));
            float toSize = paint.measureText(current, i, i + 1);

            Transition t = addTransition(c2, null, position - fromSize, endPosition - toSize);
            t.fromColor = getForegroundColor(current, i, getColor(Destination.FROM));
            t.toColor = getForegroundColor(current, i, getColor(Destination.TO));
            t.fromSize = getTextSize(current, i, getTextSize(Destination.FROM));
            t.toSize = getTextSize(current, i, getTextSize(Destination.TO));

            position -= fromSize;
            endPosition -= toSize;
        }
    }

    private void toggleTextLeft(CharSequence text, CharSequence current) {
        float position = getPaddingLeft();
        float endPosition = getPaddingLeft();

        for (int i = 0; i < text.length(); i++) {
            char c1 = text.charAt(i);
            Transition t = addTransitionFor(i, i, position, endPosition);

            if (i < current.length()) {
                paint.setTextSize(t.fromSize);
                float size = paint.measureText(current, i, i + 1);
                position += size;
            }

            paint.setTextSize(t.toSize);
            float size = paint.measureText(text, i, i + 1);
            endPosition += size;
        }

        for (int i = text.length(); i < current.length(); i++) {
            char c2 = current.charAt(i);
            Transition t = addTransition(c2, null, position, endPosition);
            t.fromColor = getForegroundColor(current, i, getColor(Destination.FROM));
            t.toColor = getForegroundColor(current, i, getColor(Destination.TO));
            t.fromSize = getTextSize(current, i, getTextSize(Destination.FROM));
            t.toSize = getTextSize(current, i, getTextSize(Destination.TO));

            paint.setTextSize(t.fromSize);
            float size = paint.measureText(current, i, i + 1);
            position += size;

            paint.setTextSize(t.toSize);
            size = paint.measureText(current, i, i + 1);
            endPosition += size;
        }
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
        // Log.i(LOGTAG, "Add trans " + from + " at pos = " + position);
        t.endPosition = endPosition;
        transitions.add(t);
        return t;
    }

    @Override
    public void onDraw(Canvas canvas) {
        CharSequence text = getText();

        int save = canvas.save();
        canvas.translate(0, getPaddingTop());
        if (transitions.size() > 0) {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long showingTime = showAnimation.getStartTime() + showAnimation.getDuration();
            long endingTime = hideAnimation.getStartTime() + hideAnimation.getDuration();
            int i = 0;

            int numMoved = 0;
            for (Transition transition : transitions) {
                if (drawTransition(canvas, transition, now - spacing * i, showingTime, endingTime)) {
                    numMoved++;
                }

                i++;
            }

            // If nothing moved, end the transitions
            if (numMoved == 0) {
                showAnimation.getTransformation(now, showTransformation);
                hideAnimation.getTransformation(now, hideTransformation);
            }

            // Invalidate if the showAnimation is still going
            if (!showAnimation.hasEnded() || !hideAnimation.hasEnded()) {
                postInvalidateOnAnimation();
            } else {
                transitions.clear();
            }
        } else {
            if ((getGravity() & Gravity.LEFT) == Gravity.LEFT) {
                float position = getPaddingLeft();
                for (int i = 0; i < text.length(); i++) {
                    canvas.drawText(text, i, i + 1, position, paint.getTextSize(), paint);
                    float size = paint.measureText(text, i, i + 1);
                    position += size;
                }
            } else {
                float position = getWidth() - getPaddingRight();
                for (int i = text.length() - 1; i >= 0; i--) {
                    float size = paint.measureText(text, i, i + 1);
                    canvas.drawText(text, i, i + 1, position - size, paint.getTextSize(), paint);
                    position -= size;
                }
            }
        }
        canvas.restoreToCount(save);
    }

    private boolean drawTransition(Canvas canvas, Transition transition, long now, long showTime, long hideTime) {
        boolean moved = false;
        float size = paint.measureText("" + transition.to, 0, 1);
        showAnimation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());
        hideAnimation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());

        if (transition.from == transition.to &&
            transition.fromColor.equals(transition.toColor) &&
            transition.fromSize.equals(transition.toSize)) {
            // Log.i(LOGTAG, "Boring " + transition.from + " to " + transition.to);
            // If nothing is changing, just draw the letter in place
            showTransformation.clear();
            hideTransformation.clear();

            int save = canvas.save();
            canvas.translate(transition.endPosition, paint.getTextSize());
            canvas.drawText("" + transition.to, 0, 1, 0, 0, paint);
            canvas.restoreToCount(save);
        } else {
            float dt = 1;
            float dt2 = 0;
            if (now < showAnimation.getStartTime()) {
                // Log.i(LOGTAG, "Waiting " + transition.from + " to " + transition.to);
                // If this letter's showAnimation hasn't started yet, draw it at its startTime
                if (autoSlide) {
                    dt = 0;
                    dt2 = 0;
                }
                showAnimation.getTransformation(showAnimation.getStartTime(), showTransformation);
                hideAnimation.getTransformation(hideAnimation.getStartTime(), hideTransformation);
            } else if (now < showTime) {
                // Log.i(LOGTAG, "Moving " + transition.from + " to " + transition.to);
                // This letter is moving, get its showTransformation set up correctly
                moved = true;
                showAnimation.getTransformation(now, showTransformation);
                hideAnimation.getTransformation(now, hideTransformation);
                if (autoSlide) {
                    dt = showAnimation.getInterpolator().getInterpolation((float) (now - showAnimation.getStartTime()) / (float) showAnimation.getDuration());
                    dt2 = hideAnimation.getInterpolator().getInterpolation((float) (now - hideAnimation.getStartTime()) / (float) hideAnimation.getDuration());
                }
            } else {
                // Log.i(LOGTAG, "Done " + transition.from + " to " + transition.to);
                // This letter is done. Keep it at the end of its showAnimation (so the showAnimation doesn't end).
                showAnimation.getTransformation(showTime - 1, showTransformation);
                hideAnimation.getTransformation(hideTime - 1, hideTransformation);
                dt = 1;
                dt2 = 1;
            }

            if (transition.from == transition.to) {
                drawMoving(canvas, transition, dt2);
            } else {
                if (transition.from != null) {
                    drawShowing(canvas, transition, transition.from, transition.fromColor, hideTransformation, dt2);
                }

                if (transition.to != null) {
                    drawShowing(canvas, transition, transition.to, transition.toColor, showTransformation, dt);
                }
            }
        }
        // Log.i(LOGTAG, "Moved " + moved);
        return moved;
    }

    private void drawMoving(Canvas canvas, Transition transition, float dt) {
        int prevColor = paint.getColor();
        float prevSize = paint.getTextSize();

        paint.setTextSize(transition.fromSize + dt * (transition.toSize - transition.fromSize));
        // This probably won't look great.
        paint.setColor(transition.fromColor + (int) (dt * (transition.toColor - transition.fromColor)));

        int save = canvas.save();
        canvas.translate(transition.startPosition + dt * (transition.endPosition - transition.startPosition), paint.getTextSize());
        canvas.drawText("" + transition.to, 0, 1,
                0, 0,
                paint);
        canvas.restoreToCount(save);

        paint.setColor(prevColor);
        paint.setTextSize(prevSize);
    }

    private void drawShowing(Canvas canvas, Transition transition, char c, int color, Transformation transform, float dt) {
        float alpha = 1.0f;
        int prevColor = paint.getColor();
        float prevSize = paint.getTextSize();

        int save = canvas.save();
        if (transform.getTransformationType() == Transformation.TYPE_ALPHA || transform.getTransformationType() == Transformation.TYPE_BOTH) {
            alpha = transform.getAlpha();
            // Log.i(LOGTAG, "Draw " + alpha + " -- " + c);
        }

        paint.setTextSize(transition.fromSize + dt * (transition.toSize - transition.fromSize));
        paint.setColor(color);
        paint.setAlpha((int) (alpha * 255));

        canvas.translate(transition.startPosition + dt * (transition.endPosition - transition.startPosition), paint.getTextSize());
        if (transform.getTransformationType() == Transformation.TYPE_MATRIX || transform.getTransformationType() == Transformation.TYPE_BOTH) {
            canvas.concat(transform.getMatrix());
        }

        canvas.drawText("" + c, 0, 1,
                0, 0,
                paint);
        canvas.restoreToCount(save);

        paint.setColor(prevColor);
        paint.setTextSize(prevSize);
    }

    float[] values = new float[9];
}
