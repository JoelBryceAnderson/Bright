package joelbryceanderson.com.bright.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
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
import java.util.Locale;

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
        protected LinearLayout mLinearLayout;

        protected TextView mTextView;
        protected Switch mLightSwitch;
        protected ImageView mImageView;
        protected SeekBar mBrightnessBar;

        protected FloatingActionButton percentageIndicatorFab;
        protected FrameLayout percentageIndicatorWhole;
        protected TextView percentageIndicatorText;
        public ViewHolder(View v) {
            super(v);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.whole_item_light);

            mTextView = (TextView) v.findViewById(R.id.light_name);
            mLightSwitch = (Switch) v.findViewById(R.id.light_toggle);
            mImageView = (ImageView) v.findViewById(R.id.image_view_light);
            mBrightnessBar = (SeekBar) v.findViewById(R.id.brightness_bar);

            percentageIndicatorFab = (FloatingActionButton)
                    v.findViewById(R.id.percentage_indicator_fab);
            percentageIndicatorWhole = (FrameLayout)
                    v.findViewById(R.id.percentage_indicator_whole);
            percentageIndicatorText = (TextView) v.findViewById(R.id.percentage_indicator_text);
        }
    }

    // Provide a suitable constructor (depends on the kind of data set)
    public RecyclerViewAdapterLights(List<PHLight> myDataSet, PHBridge bridge) {
        lightList = myDataSet;
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
        holder.mTextView.setText(lightList.get(holder.getAdapterPosition()).getName());
        if (lightList.get(holder.getAdapterPosition()).getLastKnownLightState().isOn()) {
            holder.mLightSwitch.setOnCheckedChangeListener(null);
            holder.mLightSwitch.setChecked(true);
        }
        holder.mLightSwitch.setOnCheckedChangeListener(onSwitched(holder, position));
        if (lightList.get(holder.getAdapterPosition()).supportsColor()) {
            PHLightState state = lightList.get(holder.getAdapterPosition()).getLastKnownLightState();
            if (state.isReachable()) {
                int color = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, "LCT001");
                holder.mImageView.setColorFilter(color);
                holder.mImageView.setOnClickListener(v -> createDialog(mContext, holder.getAdapterPosition(), holder));
            }
        } else {
            holder.mImageView.setColorFilter(null);
            holder.mImageView.setOnClickListener(null);
        }
        if (!lightList.get(holder.getAdapterPosition()).getLastKnownLightState().isOn()) {
            holder.mBrightnessBar.setEnabled(false);
        }
        holder.mBrightnessBar.setOnSeekBarChangeListener(null);
        holder.mBrightnessBar.setProgress(lightList.get(holder.getAdapterPosition())
                .getLastKnownLightState().getBrightness());
        holder.mBrightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentage = (progress * 100.0f) / 250;
                int intPercentage = (int) percentage;
                if (intPercentage == 0) {
                    intPercentage += 1;
                }
                holder.percentageIndicatorText
                        .setText(String.format(Locale.getDefault(), "%d%%", intPercentage));

                int floatingPosition = (int) (seekBar.getX()
                        + seekBar.getThumbOffset() / 2
                        + (seekBar).getThumb().getBounds().exactCenterX());
                holder.percentageIndicatorWhole.setX(floatingPosition
                        + holder.mLinearLayout.getPaddingLeft() * 3);

                if (progress > 0) {
                    PHLightState lightState = new PHLightState();
                    lightState.setBrightness(progress);
                    mBridge.updateLightState(lightList.get(holder.getAdapterPosition()), lightState);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                holder.percentageIndicatorWhole.setVisibility(View.VISIBLE);
                holder.percentageIndicatorFab.show();
                holder.percentageIndicatorText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                holder.percentageIndicatorText.setVisibility(View.GONE);
                holder.percentageIndicatorFab.hide(
                        new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        holder.percentageIndicatorWhole.setVisibility(View.GONE);
                    }
                });
            }
        });
        holder.mBrightnessBar.setEnabled(holder.mLightSwitch.isChecked());

        holder.mLinearLayout.setOnClickListener(v -> holder.mLightSwitch.toggle());

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            holder.percentageIndicatorText.setTextColor(Color.parseColor("#000000"));
            holder.mLinearLayout.setBackgroundColor(Color.parseColor("#000000"));
            holder.mTextView.setTextColor(Color.parseColor("#ffffff"));
        }
        if (!lightList.get(holder.getAdapterPosition()).getLastKnownLightState().isReachable()) {
            holder.mBrightnessBar.setVisibility(View.GONE);
            holder.mImageView.setImageResource(R.drawable.ic_not_reachable);
            holder.mImageView.setPadding(24,24,24,24);
        }
    }

    private CompoundButton.OnCheckedChangeListener onSwitched(ViewHolder holder, int position) {
        return (buttonView, isChecked) -> {
            if (lightList.get(position).getLastKnownLightState().isReachable()) {
                if (isChecked) {
                    holder.mBrightnessBar.setEnabled(true);
                } else {
                    holder.mBrightnessBar.setEnabled(false);
                }
                PHLightState lightState = new PHLightState();
                lightState.setOn(isChecked);
                mBridge.updateLightState(lightList.get(position), lightState);
            } else {
                holder.mLightSwitch.setChecked(!isChecked);
                Snackbar snackbar = Snackbar.make(holder.mLinearLayout,
                        "Light is not connected.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        };
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return lightList.size();
    }

    public void createDialog(final Context mContext, final int position, ViewHolder holder){
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

        presetOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.5134f, 0.4149f, position, holder);
                dialog.cancel();
            }
        });

        presetTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.4596f, 0.4105f, position, holder);
                dialog.cancel();
            }
        });


        presetThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.4449f, 0.4066f,position, holder);
                dialog.cancel();
            }
        });


        presetFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(0.3693f, 0.3695f, position, holder);
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
                            currentColor = Color.argb(255,
                                    Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                            String modelNo = lightList.get(position).getModelNumber();
                            currentColor = PHUtilities.colorFromXY(
                                    PHUtilities.calculateXY(currentColor, modelNo), modelNo);
                            color.getBackground().setColorFilter(currentColor, PorterDuff.Mode.OVERLAY);
                        }
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
                holder.mImageView.setColorFilter(currentColor);
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

    private void setColor(float x, float y, int position, ViewHolder holder) {
        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setX(x);
        lightState.setY(y);
        mBridge.updateLightState(lightList.get(position), lightState);
        holder.mImageView.setColorFilter(
                PHUtilities.colorFromXY(new float[]{x, y}, "LCT001"));
    }
}
