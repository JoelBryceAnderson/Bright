package joelbryceanderson.com.bright;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ColorChangeActivity extends Activity {

    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private int currentColor;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_change);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(stub1 -> {
            final ImageView spectrum = (ImageView) stub1.findViewById(R.id.large_spectrum);
            final ImageView color = (ImageView) stub1.findViewById(R.id.color_select);
            position = getIntent().getIntExtra("position", 1);
            currentColor = 0;
            final Bitmap bitmap = ((BitmapDrawable) spectrum.getDrawable()).getBitmap();
            spectrum.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        Matrix inverse = new Matrix();
                        spectrum.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[] {event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int currentX = (int) touchPoint[0];
                        int currentY = (int) touchPoint[1];

                        if (currentX < 0) {
                            currentX = 0;
                        }
                        if (currentX > bitmap.getWidth() - 1) {
                            currentX = bitmap.getWidth() - 1;
                        }

                        if (currentY < 0) {
                            currentY = 0;
                        }
                        if (currentY > bitmap.getHeight() - 1) {
                            currentY = bitmap.getHeight() - 1;
                        }

                        int pixel = bitmap.getPixel(currentX, currentY);
                        currentColor = Color.argb(255,
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                        color.getBackground().setColorFilter(currentColor, PorterDuff.Mode.OVERLAY);
                }
                return true;
            });
            color.setOnClickListener(v -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("color", currentColor);
                returnIntent.putExtra("position", position);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            });

            // Obtain the DismissOverlayView element
            mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
            mDismissOverlay.setIntroText(R.string.long_press_intro);
            mDismissOverlay.showIntroIfNecessary();

            // Configure a gesture detector
            mDetector = new GestureDetector(ColorChangeActivity.this,
                    new GestureDetector.SimpleOnGestureListener() {
                        public void onLongPress(MotionEvent ev) {
                            mDismissOverlay.show();
                        }
                    });
            spectrum.setOnLongClickListener(v -> {
                mDismissOverlay.show();
                return true;
            });
            color.setOnLongClickListener(v -> {
                mDismissOverlay.show();
                return true;
            });
        });
    }

    // Capture long presses
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

}
