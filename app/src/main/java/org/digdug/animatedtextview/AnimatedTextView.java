package org.digdug.animatedtextview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
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

    @Nullable private Animation showAnimation = new AlphaAnimation(0f, 1.0f);
    @Nullable private Animation hideAnimation = new AlphaAnimation(1.0f, 0f);
    private Transformation showTransformation = new Transformation();
    private Transformation hideTransformation = new Transformation();
    @Nullable private Integer toGravity = null;
    private TransitionDirection direction = TransitionDirection.GRAVITY;

    public void setDirection(TransitionDirection direction) {
        this.direction = direction;
    }

    public TransitionDirection getDirection() {
        return direction;
    }

    public enum TransitionDirection {
        LEFT(1),
        RIGHT(-1),
        GRAVITY(1),
        RANDOM(1);

        public final int value;

        TransitionDirection(int val) {
            this.value = val;
        }
    }

    public AnimatedTextView(Context context) {
        super(context, null, R.style.AppTheme);
        init();
    }

    public AnimatedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.AppTheme);
        init();
    }

    public AnimatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources.Theme theme = context.getTheme();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedTextView, defStyleAttr, 0);

        int n = a.length();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.AnimatedTextView_duration:
                    setDuration(a.getInteger(attr, duration));
                    break;
                case R.styleable.AnimatedTextView_spacing:
                    setSpacing(a.getInteger(attr, spacing));
                    break;
                case R.styleable.AnimatedTextView_showAnimation: {
                    int res = a.getResourceId(attr, 0);
                    setShowAnimation(res);
                    break;
                }
                case R.styleable.AnimatedTextView_hideAnimation: {
                    int res = a.getResourceId(attr, 0);
                    setHideAnimation(res);
                    break;
                }
            }
        }
        a.recycle();

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
        if (animId == 0) {
            setShowAnimation(null);
            return;
        }

        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        setShowAnimation(anim);
    }

    public void setShowAnimation(final Animation animation) {
        if (animation != null && animation.getDuration() == 0) {
            animation.setDuration(duration);
        }
        this.showAnimation = animation;
    }

    public Animation getShowAnimation() {
        return showAnimation;
    }

    public void setHideAnimation(@AnimRes final int animId) {
        if (animId == 0) {
            setHideAnimation(null);
            return;
        }

        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        setHideAnimation(anim);
    }

    public void setHideAnimation(final Animation animation) {
        if (animation != null && animation.getDuration() == 0) {
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
        float fromPosition;
        float toPosition;

        Character from;
        Character to;

        TransitionType type;
        public Integer fromColor = null;
        public Float fromSize = null;
        public Integer toColor = null;
        public Float toSize = null;

        public String toString() {
            return "Transition from " + from + "(" + fromPosition + ") to " + to + " (" + toPosition + ")";
        }
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

    public Transition addTransitionFor(CharSequence fromChars, CharSequence toChars,
                                       int fromIndex, int toIndex,
                                       float fromPosition, float toPosition) {
        Character to = null;
        if (toIndex >= 0 && toIndex < toChars.length()) {
            to = toChars.charAt(toIndex);
        }

        Character from = null;
        if (fromIndex >= 0 && fromIndex < fromChars.length()) {
            from = fromChars.charAt(fromIndex);
        } else {
            fromPosition = toPosition;
        }

        Transition transition = addTransition(from, to, fromPosition, toPosition);
        // onDraw uses colors to determine if it should transition or not. Make sure we set one for now...
        // XXX - onDraw should be smarter here
        transition.fromColor = getForegroundColor(fromChars, fromIndex, getColor(Destination.FROM));
        transition.fromSize = getTextSize(fromChars, fromIndex, getTextSize(Destination.FROM));

        transition.toColor = getForegroundColor(toChars, toIndex, getColor(Destination.TO));
        transition.toSize = getTextSize(toChars, toIndex, getTextSize(Destination.TO));

        // Log.i(LOGTAG, "From " + fromPosition + ", " + toPosition);

        return transition;
    }

    private float adjustMeasuredText(Float fromSize, Character c) {
        if (c == null) {
            return 0;
        }
        paint.setTextSize(fromSize);
        return paint.measureText("" + c, 0, 1);
    }

    private int getForegroundColor(CharSequence chars, int position, int defaultColor) {
        if (chars instanceof Spanned) {
            Spanned spanned = (Spanned) chars;
            Object[] spans = spanned.getSpans(position, position+1, Object.class);
            for (Object span : spans) {
                if (span instanceof ForegroundColorSpan) {
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
        // This can be called during initialization, before this is created.
        if (transitions == null) {
            setText(text);
            return;
        }
        transitions.clear();

        CharSequence current = getText();
        toggleText(text, current);

        for (Transition transition : transitions) {
            // Log.i(LOGTAG, "" + transition);
        }

        if (showAnimation != null)
            showAnimation.startNow();
        hideAnimation.startNow();

        setText(text);
        postInvalidateOnAnimation();
    }

    @Override
    public void setGravity(int gravity) {
        toGravity = gravity;
        toggleText(getText());
        toGravity = null;
        super.setGravity(gravity);
    }

    private int getGravity(Destination to) {
        if (to == Destination.TO && toGravity != null) {
            return toGravity;
        }
        return getGravity();
    }

    private boolean isRightGravity(int gravity) {
        final int hgrav = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        return hgrav == Gravity.RIGHT;
    }

    private boolean isLeftGravity(int gravity) {
        final int hgrav = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        return hgrav == Gravity.LEFT;
    }

    private boolean isCenterGravity(int gravity) {
        final int hgrav = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        return hgrav == Gravity.CENTER_HORIZONTAL;
    }

    private float getWidth(CharSequence text, float defaultSize) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            float size = getTextSize(text, i, defaultSize);
            width += adjustMeasuredText(size, text.charAt(i));
        }
        return width;
    }

    private void toggleText(CharSequence to, CharSequence from) {
        final int toGrav = getGravity(Destination.TO);
        final int fromGrav = getGravity(Destination.FROM);
        final TransitionDirection direction = getTransitionDirection(toGrav, fromGrav);

        float toPosition = isRightGravity(toGrav) ? getWidth() - getPaddingRight() : getPaddingLeft();
        float fromPosition = isRightGravity(fromGrav) ? getWidth() - getPaddingRight() : getPaddingLeft();

        float defaultFromSize = getTextSize(Destination.FROM);
        float defaultToSize = getTextSize(Destination.TO);

        if (isCenterGravity(fromGrav)) {
            fromPosition = (getWidth() - getWidth(from, defaultFromSize))/2;
        }

        if (isCenterGravity(toGrav)) {
            toPosition = (getWidth() - getWidth(to, defaultToSize))/2;
        }

        // If the gravity is changing, we still want to always grab from the left or right of the string.
        // That means, we need to shift our from position
        if (toGrav != fromGrav) {
            if (isRightGravity(fromGrav) && direction == TransitionDirection.LEFT) {
                fromPosition -= getWidth(from, defaultFromSize);
            } else if (isLeftGravity(fromGrav) || isRightGravity(toGrav)) {
                fromPosition += getWidth(from, defaultFromSize);
                if (isCenterGravity(toGrav)) {
                    toPosition += getWidth(to, defaultToSize);
                }
            }
        }

        // We always use toGravity here so that we'll start from the same side, regardless of which direction
        // the text is running.
        int fromIndex  = direction == TransitionDirection.RIGHT ? from.length() - 1 : 0;
        int startIndex = direction == TransitionDirection.RIGHT ?   to.length() - 1 : 0;
        int endIndex   = direction == TransitionDirection.RIGHT ?                -1 : to.length();

        for (int toIndex = startIndex; toIndex != endIndex; toIndex += direction.value) {
            Transition transition = addTransitionFor(from, to, fromIndex, toIndex, fromPosition, toPosition);

            // Adjust out position based on the textsize of the from and to strings.
            fromPosition += direction.value * adjustMeasuredText(transition.fromSize, transition.from);
            toPosition   += direction.value * adjustMeasuredText(transition.toSize, transition.to);

            // If this is floating to the right, update its position to account for its size.
            if (direction == TransitionDirection.RIGHT) { transition.fromPosition = fromPosition; }
            if (direction == TransitionDirection.RIGHT) { transition.toPosition = toPosition; }

            // Iterate the from string.
            fromIndex    += direction.value;
        }

        while (fromIndex > -1 && fromIndex < from.length()) {
            Transition transition = addTransitionFor(from, to, fromIndex, -1, fromPosition, toPosition);
            fromPosition += direction.value * adjustMeasuredText(transition.fromSize, transition.from);
            if (direction == TransitionDirection.RIGHT) { transition.fromPosition = fromPosition; }
            fromIndex += direction.value;
        }
    }

    private TransitionDirection getTransitionDirection(int toGrav, int fromGrav) {
        if (this.direction == TransitionDirection.GRAVITY) {
            if (isRightGravity(toGrav)) {
                return TransitionDirection.RIGHT;
            } else if (isLeftGravity(toGrav)) {
                return TransitionDirection.LEFT;

            } else if (isRightGravity(fromGrav)) {
                return TransitionDirection.LEFT;
            } else if (isLeftGravity(fromGrav)) {
                return TransitionDirection.RIGHT;
            }

            return TransitionDirection.LEFT;
        }
        return this.direction;
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

    private Transition addTransition(Character from, Character to, float fromPosition, float toPosition) {
        Transition t = new Transition();
        t.from = from;
        t.to = to;
        t.fromPosition = fromPosition;
        // Log.i(LOGTAG, "Add trans " + from + " at pos = " + position);
        t.toPosition = toPosition;
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
            long showingTime = showAnimation != null ? showAnimation.getStartTime() + showAnimation.getDuration() : now;
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
                if (showAnimation != null) showAnimation.getTransformation(now, showTransformation);
                hideAnimation.getTransformation(now, hideTransformation);
            }

            // Invalidate if the showAnimation is still going
            if (showAnimation != null && !showAnimation.hasEnded() || !hideAnimation.hasEnded()) {
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
        if (showAnimation != null) {
            showAnimation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());
        }
        if (hideAnimation != null) {
            hideAnimation.initialize((int) size, (int) paint.getTextSize(), getWidth(), getHeight());
        }

        /*
        if (transition.from == transition.to &&
            transition.fromColor.equals(transition.toColor) &&
            transition.fromSize.equals(transition.toSize)) {
            // Log.i(LOGTAG, "Boring " + transition.from + " to " + transition.to);
            // If nothing is changing, just draw the letter in place
            showTransformation.clear();
            hideTransformation.clear();

            int save = canvas.save();
            canvas.translate(transition.toPosition, paint.getTextSize());
            canvas.drawText("" + transition.to, 0, 1, 0, 0, paint);
            canvas.restoreToCount(save);
        } else {
        */
            float dt = 1;
            float dt2 = 0;
            if (showAnimation != null && hideAnimation != null && now < showAnimation.getStartTime()) {
                // Log.i(LOGTAG, "Waiting " + transition.from + " to " + transition.to);
                // If this letter's showAnimation hasn't started yet, draw it at its startTime
                if (autoSlide) {
                    dt = 0;
                    dt2 = 0;
                }
                showAnimation.getTransformation(showAnimation.getStartTime(), showTransformation);
                hideAnimation.getTransformation(hideAnimation.getStartTime(), hideTransformation);
            } else if (showAnimation != null && hideAnimation != null && now < showTime) {
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
                if (showAnimation != null) showAnimation.getTransformation(showTime - 1, showTransformation);
                if (hideAnimation != null) hideAnimation.getTransformation(hideTime - 1, hideTransformation);
                dt = 1;
                dt2 = 1;
            }

            if (transition.from == transition.to) {
                drawMoving(canvas, transition, dt2);
            } else {
                if (transition.from != null) {
                    // Log.i(LOGTAG, "Draw " + transition.from);
                    drawShowing(canvas, transition, transition.from, transition.fromColor, hideTransformation, dt2);
                }

                if (transition.to != null) {
                    // Log.i(LOGTAG, "Draw " + transition.to);
                    drawShowing(canvas, transition, transition.to, transition.toColor, showTransformation, dt);
                }
            }
        // }
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
        canvas.translate(transition.fromPosition + dt * (transition.toPosition - transition.fromPosition), paint.getTextSize());
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

        canvas.translate(transition.fromPosition + dt * (transition.toPosition - transition.fromPosition), paint.getTextSize());
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
