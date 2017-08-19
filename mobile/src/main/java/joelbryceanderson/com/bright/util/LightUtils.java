package joelbryceanderson.com.bright.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import joelbryceanderson.com.bright.R;

/**
 * Created by JAnderson on 1/28/17.
 */

public class LightUtils {

    public static Dialog createDialog(final Context mContext, final int position, final boolean darkMode){
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.color_picker_sliders);

        final LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.color_picker_background);
        if (darkMode) {
            linearLayout.setBackgroundColor(Color.parseColor("#263238"));
        }

        ImageView presetOne = (ImageView) dialog.findViewById(R.id.preset_one);
        ImageView presetTwo = (ImageView) dialog.findViewById(R.id.preset_two);
        ImageView presetThree = (ImageView) dialog.findViewById(R.id.preset_three);
        ImageView presetFour = (ImageView) dialog.findViewById(R.id.preset_four);

        presetOne.setOnClickListener(setLightColor(dialog, position, 0.5134f, 0.4149f));
        presetTwo.setOnClickListener(setLightColor(dialog, position, 0.4596f, 0.4105f));
        presetThree.setOnClickListener(setLightColor(dialog, position, 0.4449f, 0.4066f));
        presetFour.setOnClickListener(setLightColor(dialog, position, 0.3693f, 0.3695f));

        ImageView color = (ImageView) dialog.findViewById(R.id.color);
        ImageView colorSpectrum = (ImageView) dialog.findViewById(R.id.color_spectrum);
        Bitmap bitmap = ((BitmapDrawable) colorSpectrum.getDrawable()).getBitmap();

        colorSpectrum.setOnTouchListener(selectColor(bitmap, colorSpectrum));
        color.setOnClickListener(setSelectedColor());

        return dialog;
    }

    private static View.OnClickListener setSelectedColor() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*PHLightState lightState = new PHLightState();
                lightState.setOn(true);
                String modelNo = lightList.get(position).getModelNumber();
                float[] xy = PHUtilities.calculateXY(currentColor, modelNo);
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                mBridge.updateLightState(lightList.get(position), lightState);
                list.get(position).mImageView.setColorFilter(currentColor);
                dialog.cancel();*/
            }
        };
    }

    private static View.OnTouchListener selectColor(final Bitmap bitmap, final ImageView iv) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        Matrix inverse = new Matrix();
                        iv.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[] {motionEvent.getX(), motionEvent.getY()};
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
                        if (pixel != 0) {
                            /*currentColor = Color.argb(255,
                                    Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                            String modelNo = lightList.get(position).getModelNumber();
                            currentColor = PHUtilities.colorFromXY(
                                    PHUtilities.calculateXY(currentColor, modelNo), modelNo);
                            color.getBackground().setColorFilter(currentColor, PorterDuff.Mode.OVERLAY);*/
                        }
                }
                return true;
            }
        };
    }

    private static View.OnClickListener setLightColor(final Dialog dialog, final int position,
                                               final float x, final float y) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setColor(0.4596f, 0.4105f, position);
                dialog.cancel();
            }
        };
    }
}
