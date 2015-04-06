# AnimatedTextView
A TextView implementation for Android that supports transitioning text changes. You can customize the transition animations using normal Android animations. 

![video of some possible transitions](https://raw.githubusercontent.com/wesj/AnimatedTextView/master/AnimatedTextView.mp4)

Add a view in xml using:
```xml
<org.digdug.widget.AnimatedTextView
  android:id="@+id/text"
  android:text="@string/hello_world"
  android:layout_width="match_parent"
  android:layout_height="wrap_content" />
```
This doesn't support setting animations via XML (yet). Nor does it smoothly change the width of the TextView during animations (which would cause unpredictable layout changes. Don't do it!) You should always have a fixed width on your TextView. In code you can get the view:
```java
AnimatedTextView text = (AnimatedTextView) findViewById(R.id.text);
text.setShowAnimation(myAnimation);
text.setHideAnimation(myAnimation);
text.setDuration(1000); // This will set the duration of both the show and hide transitions
text.setSpacing(100); // Change the delay between transitioning subsequent letter changes
```
