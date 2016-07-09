package joelbryceanderson.com.bright.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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

import joelbryceanderson.com.bright.Fragments.GroupsFragment;
import joelbryceanderson.com.bright.LightGroup;
import joelbryceanderson.com.bright.R;

/**
 * Created by Joel Anderson on 2/11/16.
 * Recycler view adapter for groups
 */
public class RecyclerViewAdapterGroups extends RecyclerView.Adapter<RecyclerViewAdapterGroups.ViewHolder> {
    private List<LightGroup> lightList;
    private PHBridge mBridge;
    private Context mContext;
    private int currentColor;
    private GroupsFragment parent;
    private Boolean darkMode;
    private List<ViewHolder> list = new ArrayList<>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTextView;
        protected Switch mLightSwitch;
        protected LinearLayout mLinearLayout;
        protected ImageView mImageView;
        protected SeekBar mBrightnessBar;

        protected FloatingActionButton percentageIndicatorFab;
        protected FrameLayout percentageIndicatorWhole;
        protected TextView percentageIndicatorText;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.group_name);
            mLightSwitch = (Switch) v.findViewById(R.id.group_toggle);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.whole_item_group);
            mImageView = (ImageView) v.findViewById(R.id.image_view_group);
            mBrightnessBar = (SeekBar) v.findViewById(R.id.brightness_bar_group);

            percentageIndicatorFab = (FloatingActionButton)
                    v.findViewById(R.id.percentage_indicator_fab);
            percentageIndicatorWhole = (FrameLayout)
                    v.findViewById(R.id.percentage_indicator_whole);
            percentageIndicatorText = (TextView) v.findViewById(R.id.percentage_indicator_text);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapterGroups(List<LightGroup> myDataset, PHBridge bridge, GroupsFragment parent) {
        lightList = myDataset;
        this.parent = parent;
        mBridge = bridge;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapterGroups.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        final LightGroup thisGroup = lightList.get(holder.getAdapterPosition());
        list.add(holder);
        int totalBrightness = 0;
        int numLights = 0;
        for (PHLight light : thisGroup.getLights()) {
            numLights++;
            totalBrightness += light.getLastKnownLightState().getBrightness();
        }
        holder.percentageIndicatorFab.hide();
        holder.percentageIndicatorFab.setElevation(4);
        holder.percentageIndicatorWhole.setVisibility(View.GONE);

        holder.mBrightnessBar.setProgress(totalBrightness / numLights);
        holder.mLightSwitch.setChecked(false);
        holder.mBrightnessBar.setEnabled(false);
        holder.mTextView.setText(thisGroup.getName());
        holder.mLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PHLightState state = new PHLightState();
                if (isChecked) {
                    state.setOn(true);
                    holder.mBrightnessBar.setEnabled(true);
                } else {
                    state.setOn(false);
                    holder.mBrightnessBar.setEnabled(false);
                }
                mBridge.setLightStateForGroup(thisGroup.getIdentifier(), state);
            }
        });
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                holder.percentageIndicatorWhole.setVisibility(View.VISIBLE);
                holder.percentageIndicatorFab.show();
                holder.percentageIndicatorText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                holder.percentageIndicatorText.setVisibility(View.GONE);
                holder.percentageIndicatorFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        holder.percentageIndicatorWhole.setVisibility(View.GONE);
                        PHLightState state = new PHLightState();
                        state.setBrightness(seekBar.getProgress());
                        mBridge.setLightStateForGroup(thisGroup.getIdentifier(), state);
                    }
                });
            }
        });
        if (thisGroup.hasAnyColor()) {
            holder.mImageView.setImageResource(R.drawable.color_spectrum_tiny);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(mContext, holder.getAdapterPosition());
                }
            });
        }

        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mLightSwitch.toggle();
            }
        });
        holder.mLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder deleter = new AlertDialog.Builder(mContext);
                deleter.setTitle("Delete");
                deleter.setMessage("Would you like to delete this group?");
                deleter.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        parent.removeGroup(thisGroup.getName(), holder.getAdapterPosition());
                    }
                });
                deleter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                deleter.show();
                return false;
            }
        });
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            holder.percentageIndicatorText.setTextColor(Color.parseColor("#000000"));
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
                setColor(0.5134f, 0.4149f,position);
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
                            color.getBackground().setColorFilter(currentColor, PorterDuff.Mode.OVERLAY);
                        }
                }
                return true;
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (PHLight light : lightList.get(position).getLights()) {
                    if (light.supportsColor()) {
                        PHLightState lightState = new PHLightState();
                        float[] xy = PHUtilities.calculateXY(currentColor, light.getModelNumber());
                        lightState.setX(xy[0]);
                        lightState.setY(xy[1]);
                        lightState.setOn(true);
                        mBridge.updateLightState(light, lightState);
                        list.get(position).mLightSwitch.setChecked(true);
                    }
                }
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void setColor(float x, float y, int position) {
        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setX(x);
        lightState.setY(y);
        mBridge.setLightStateForGroup(lightList.get(position).getIdentifier(), lightState);
        list.get(position).mLightSwitch.setChecked(true);
    }

}