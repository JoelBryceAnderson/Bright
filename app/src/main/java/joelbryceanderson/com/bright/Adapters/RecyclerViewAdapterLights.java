package joelbryceanderson.com.bright.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.List;

import joelbryceanderson.com.bright.Fragments.LightsFragment;
import joelbryceanderson.com.bright.R;

public class RecyclerViewAdapterLights extends RecyclerView.Adapter<RecyclerViewAdapterLights.ViewHolder> {
    private List<PHLight> lightList;
    private PHBridge mBridge;
    private Context mContext;
    private int currentColor;
    private Boolean darkMode;
    private List<ViewHolder> list = new ArrayList<>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView mTextView;
        protected Switch mLightSwitch;
        protected ImageView mImageView;
        protected LinearLayout mLinearLayout;
        protected SeekBar mBrightnessBar;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.light_name);
            mLightSwitch = (Switch) v.findViewById(R.id.light_toggle);
            mImageView = (ImageView) v.findViewById(R.id.image_view_light);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.whole_item_light);
            mBrightnessBar = (SeekBar) v.findViewById(R.id.brightness_bar);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapterLights(List<PHLight> myDataset, PHBridge bridge) {
        lightList = myDataset;
        mBridge = bridge;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapterLights.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_light, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        list.add(holder);
        holder.mTextView.setText(lightList.get(position).getName());
        holder.mLightSwitch.setChecked(lightList.get(position).getLastKnownLightState().isOn());
        holder.mLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    holder.mBrightnessBar.setEnabled(true);
                } else {
                    holder.mBrightnessBar.setEnabled(false);
                }
                PHLightState lightState = new PHLightState();
                lightState.setOn(isChecked);
                mBridge.updateLightState(lightList.get(position), lightState);
            }
        });
        if (lightList.get(position).supportsColor()) {
            PHLightState state = lightList.get(position).getLastKnownLightState();
            int color = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "LCT001");
            holder.mImageView.setColorFilter(color);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(mContext, position);
                }
            });
        }
        if (!lightList.get(position).getLastKnownLightState().isOn()) {
            holder.mBrightnessBar.setEnabled(false);
        }
        holder.mBrightnessBar.setProgress(lightList.get(position)
                .getLastKnownLightState().getBrightness());
        holder.mBrightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0) {
                    PHLightState lightState = new PHLightState();
                    lightState.setBrightness(progress);
                    mBridge.updateLightState(lightList.get(position), lightState);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            holder.mLinearLayout.setBackgroundColor(Color.parseColor("#000000"));
            holder.mTextView.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return lightList.size();
    }

    public void createDialog(final Context mContext, final int position){
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.color_picker_sliders);

        final LinearLayout linearLayout = (LinearLayout)
                dialog.findViewById(R.id.color_picker_background);
        if (darkMode) {
            linearLayout.setBackgroundColor(Color.parseColor("#263238"));
        }
        final ImageView color = (ImageView) dialog.findViewById(R.id.color);
        final ImageView colorSpectrum = (ImageView) dialog.findViewById(R.id.color_spectrum);

        ImageView presetOne = (ImageView) dialog.findViewById(R.id.preset_one);
        ImageView presetTwo = (ImageView) dialog.findViewById(R.id.preset_two);
        ImageView presetThree = (ImageView) dialog.findViewById(R.id.preset_three);
        ImageView presetFour = (ImageView) dialog.findViewById(R.id.preset_four);

        presetOne.setColorFilter(PHUtilities.colorFromXY(new float[]{0.5134f, 0.4149f}, "LCT001"));
        presetTwo.setColorFilter(PHUtilities.colorFromXY(new float[]{0.4596f, 0.4105f}, "LCT001"));
        presetThree.setColorFilter(PHUtilities.colorFromXY(new float[]{0.4449f, 0.4066f}, "LCT001"));
        presetFour.setColorFilter(PHUtilities.colorFromXY(new float[]{0.3693f, 0.3695f}, "LCT001"));

        presetOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.5134f, 0.4149f, position);
                dialog.cancel();
            }
        });

        presetTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.4596f, 0.4105f, position);
                dialog.cancel();
            }
        });


        presetThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.4449f, 0.4066f,position);
                dialog.cancel();
            }
        });


        presetFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.3693f, 0.3695f, position);
                dialog.cancel();
            }
        });


        final Bitmap bitmap = ((BitmapDrawable) colorSpectrum.getDrawable()).getBitmap();

        colorSpectrum.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        Matrix inverse = new Matrix();
                        colorSpectrum.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[] {event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int currentX = Integer.valueOf((int)touchPoint[0]);
                        int currentY = Integer.valueOf((int)touchPoint[1]);

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
                        String modelNo = lightList.get(position).getModelNumber();
                        currentColor = PHUtilities.colorFromXY(
                                PHUtilities.calculateXY(currentColor, modelNo), modelNo);
                        color.setColorFilter(currentColor);
                }
                return true;
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHLightState lightState = new PHLightState();
                lightState.setOn(true);
                String modelNo = lightList.get(position).getModelNumber();
                float[] xy = PHUtilities.calculateXY(currentColor, modelNo);
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                mBridge.updateLightState(lightList.get(position), lightState);
                list.get(position).mImageView.setColorFilter(currentColor);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void toggleAll(boolean on) {
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).mLightSwitch.setChecked(on);
        }
    }

    private void setColor(float x, float y, int position) {
        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setX(x);
        lightState.setY(y);
        mBridge.updateLightState(lightList.get(position), lightState);
        list.get(position).mImageView.setColorFilter(
                PHUtilities.colorFromXY(new float[]{x, y}, "LCT001"));
    }
}
