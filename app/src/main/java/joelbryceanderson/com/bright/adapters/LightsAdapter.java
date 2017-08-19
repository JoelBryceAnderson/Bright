package joelbryceanderson.com.bright.adapters;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import joelbryceanderson.com.bright.R;

/**
 * Created by JAnderson on 1/28/17.
 */

public class LightsAdapter extends RecyclerView.Adapter<LightsAdapter.ViewHolder> {

    private List<PHLight> mLights;
    private PHBridge mBridge;
    private Context mContext;
    private int mCurrentColor;
    private Boolean mDarkMode;
    private List<ViewHolder> mViewHolders = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mLinearLayout;

        TextView mTextView;
        Switch mLightSwitch;
        ImageView mImageView;
        SeekBar mBrightnessBar;

        FloatingActionButton mPercentageFab;
        FrameLayout mPercentageIndicator;
        TextView mPercentageText;


        public ViewHolder(View itemView) {
            super(itemView);

            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.whole_item_light);

            mTextView = (TextView) itemView.findViewById(R.id.light_name);
            mLightSwitch = (Switch) itemView.findViewById(R.id.light_toggle);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view_light);
            mBrightnessBar = (SeekBar) itemView.findViewById(R.id.brightness_bar);

            mPercentageFab = (FloatingActionButton)
                    itemView.findViewById(R.id.percentage_indicator_fab);
            mPercentageIndicator = (FrameLayout)
                    itemView.findViewById(R.id.percentage_indicator_whole);
            mPercentageText = (TextView) itemView.findViewById(R.id.percentage_indicator_text);

        }
    }

    public LightsAdapter(Context context, List<PHLight> lights, PHBridge bridge) {
        mLights = lights;
        mBridge = bridge;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_light, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        mViewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PHLight light = mLights.get(position);
        PHLightState lastState = light.getLastKnownLightState();

        if (lastState.isReachable()) {


        } else {
            setNotReachable(holder);
        }
    }

    private void setNotReachable(ViewHolder holder) {
        holder.mBrightnessBar.setVisibility(View.GONE);
        holder.mImageView.setImageResource(R.drawable.ic_not_reachable);
        holder.mImageView.setPadding(24,24,24,24);
    }

    public void toggleAll(boolean on) {
        for (int i = 0; i < mViewHolders.size(); ++i) {
            mViewHolders.get(i).mLightSwitch.setChecked(on);
        }
    }

    private void setLightColor(float x, float y, int position) {
        PHLight light = mLights.get(position);
        PHLightState lightState = light.getLastKnownLightState();

        lightState.setOn(true);
        lightState.setX(x);
        lightState.setY(y);

        int color = PHUtilities.colorFromXY(new float[]{x, y}, light.getModelNumber());

        mBridge.updateLightState(light, lightState);
        mViewHolders.get(position).mImageView.setColorFilter(color);
    }

    @Override
    public int getItemCount() {
        return mLights.size();
    }
}
