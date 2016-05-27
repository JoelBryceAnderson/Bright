package joelbryceanderson.com.bright.Adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
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

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import joelbryceanderson.com.bright.Fragments.AlarmsFragment;
import joelbryceanderson.com.bright.Fragments.GroupsFragment;
import joelbryceanderson.com.bright.LightGroup;
import joelbryceanderson.com.bright.R;

/**
 * Created by JAnderson on 2/11/16.
 */
public class RecyclerViewAdapterAlarms extends RecyclerView.Adapter<RecyclerViewAdapterAlarms.ViewHolder> {
    private List<PHSchedule> scheduleList;
    private PHBridge mBridge;
    private Context mContext;
    private AlarmsFragment fragment;
    private int currentColor;
    private Boolean darkMode;
    private List<ViewHolder> list = new ArrayList<>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTextView;
        protected TextView mSubTextView;
        protected Switch mLightSwitch;
        protected LinearLayout mLinearLayout;
        protected ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.alarm_time);
            mLightSwitch = (Switch) v.findViewById(R.id.alarm_toggle);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.whole_item_alarm);
            mImageView = (ImageView) v.findViewById(R.id.image_view_alarm);
            mSubTextView = (TextView) v.findViewById(R.id.alarm_days);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapterAlarms(List<PHSchedule> myDataset,
                                     PHBridge bridge, AlarmsFragment fragment) {
        scheduleList = myDataset;
        this.fragment = fragment;
        mBridge = bridge;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapterAlarms.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final PHSchedule thisSchedule = scheduleList.get(position);
        list.add(holder);

        holder.mLightSwitch.setChecked(thisSchedule.getStatus().toString().equals("ENABLED"));
        holder.mLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    thisSchedule.setStatus(PHSchedule.PHScheduleStatus.ENABLED);
                    mBridge.updateSchedule(thisSchedule, new PHScheduleListener() {
                        @Override
                        public void onCreated(PHSchedule phSchedule) {

                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int i, String s) {

                        }

                        @Override
                        public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                        }
                    });
                } else {
                    thisSchedule.setStatus(PHSchedule.PHScheduleStatus.DISABLED);
                    mBridge.updateSchedule(thisSchedule, new PHScheduleListener() {
                        @Override
                        public void onCreated(PHSchedule phSchedule) {

                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int i, String s) {

                        }

                        @Override
                        public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                        }
                    });
                }
            }
        });

        //Set Time TextView
        String timeBuilder = "";
        Calendar calendar = Calendar.getInstance();
        if (thisSchedule.getDate() != null) {
            calendar.setTime(thisSchedule.getDate());

            Boolean AM = true;
            if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
                timeBuilder = timeBuilder.concat(Integer.toString(
                        calendar.get(Calendar.HOUR_OF_DAY) - 12));
                AM = false;
            } else {
                timeBuilder = timeBuilder.concat(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
            }
            timeBuilder = timeBuilder.concat(":");
            if (calendar.get(Calendar.MINUTE) < 10) {
                timeBuilder = timeBuilder.concat("0");
            }
            timeBuilder = timeBuilder.concat(Integer.toString(calendar.get(Calendar.MINUTE)));
            if (AM) {
                timeBuilder = timeBuilder.concat(" AM");
            } else {
                timeBuilder = timeBuilder.concat(" PM");
            }
            holder.mTextView.setText(timeBuilder);
        } else {
            holder.mTextView.setText("Alarm from other app");
        }

        //Set ImageView
        if (thisSchedule.getLightState() != null) {
            if (thisSchedule.getLightState().isOn()) {
                float xy[] = new float[]{thisSchedule.getLightState().getX(),
                        thisSchedule.getLightState().getY()};
                holder.mImageView.setColorFilter(PHUtilities.colorFromXY(xy, "LCT001"));
                holder.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createDialog(mContext, position);
                    }
                });
            }
        }

        //Set Recurring Days TextView
        int r = thisSchedule.getRecurringDays();
        String recurringDays = "";
        if (r == 127) {
            recurringDays = "EVERY DAY";
        } else if (r == 0) {
            recurringDays = "NO REPEAT";
        } else {
            if ((r & (1 << 0)) != 0) {
                recurringDays = recurringDays.concat("SUN ");
            }
            if ((r & (1 << 6)) != 0) {
                recurringDays = recurringDays.concat("MON ");
            }
            if ((r & (1 << 5)) != 0) {
                recurringDays = recurringDays.concat("TUE ");
            }
            if ((r & (1 << 4)) != 0) {
                recurringDays = recurringDays.concat("WED ");
            }
            if ((r & (1 << 3)) != 0) {
                recurringDays = recurringDays.concat("THU ");
            }
            if ((r & (1 << 2)) != 0) {
                recurringDays = recurringDays.concat("FRI ");
            }
            if ((r & (1 << 1)) != 0) {
                recurringDays = recurringDays.concat("SAT ");
            }
        }
        holder.mSubTextView.setText(recurringDays);

        //Delete item on long-press
        holder.mLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder deleter = new AlertDialog.Builder(mContext);
                deleter.setTitle("Delete");
                deleter.setMessage("Would you like to delete this group?");
                deleter.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.deleteAlarm(position);
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
            holder.mLinearLayout.setBackgroundColor(Color.parseColor("#000000"));
            holder.mTextView.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return scheduleList.size();
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
                float xy[] = PHUtilities.calculateXY(currentColor, "LCT001");
                setColor(xy[0], xy[1], position);
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
        lightState.setBrightness(250);
        scheduleList.get(position).setLightState(lightState);
        float xy[] = new float[]{x,y};
        list.get(position).mImageView.setColorFilter(PHUtilities.colorFromXY(xy, "LCT001"));
        mBridge.updateSchedule(scheduleList.get(position), new PHScheduleListener() {
            @Override
            public void onCreated(PHSchedule phSchedule) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                System.out.println(s);
            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }
}