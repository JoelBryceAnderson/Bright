package com.JoelBryceAnderson.bright;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JAnderson on 5/14/16.
 */
public class RecyclerGroupAdapterWearGroups extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private Context mContext;
    private List<Group> groupList;
    private MainActivity parent;
    private List<RecyclerView.ViewHolder> viewHolderList = new ArrayList<>();

    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        protected TextView mTextView;
        protected Switch mLightSwitch;
        protected LinearLayout mLinearLayout;
        protected ImageView mImageView;
        public ViewHolderItem(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.group_name);
            mLightSwitch = (Switch) v.findViewById(R.id.group_toggle);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.whole_item_group);
            mImageView = (ImageView) v.findViewById(R.id.image_view_group);
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        protected RelativeLayout wholeHeader;
        public ViewHolderHeader(View v) {
            super(v);
            wholeHeader = (RelativeLayout) v.findViewById(R.id.whole_header);
        }
    }

    public static class ViewHolderFooter extends RecyclerView.ViewHolder {
        protected RelativeLayout wholeFooter;
        public ViewHolderFooter(View v) {
            super(v);
            wholeFooter = (RelativeLayout) v.findViewById(R.id.whole_footer);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerGroupAdapterWearGroups(List<Group> myDataset, MainActivity parent) {
        groupList = myDataset;
        this.parent = parent;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_list, parent, false);
            mContext = parent.getContext();
            ViewHolderItem toReturn = new ViewHolderItem(v);
            viewHolderList.add(toReturn);
            return toReturn;
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_group_list, parent, false);
            mContext = parent.getContext();
            ViewHolderHeader toReturn = new ViewHolderHeader(v);
            viewHolderList.add(toReturn);
            return toReturn;
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_group_list, parent, false);
            mContext = parent.getContext();
            ViewHolderFooter toReturn = new ViewHolderFooter(v);
            viewHolderList.add(toReturn);
            return toReturn;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof ViewHolderItem) {
            final Group thisGroup = groupList.get(position);
            final ViewHolderItem VHitem = (ViewHolderItem) holder;
            VHitem.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, BrightnessActivity.class);
                    intent.putExtra("groupName", groupList.get(position).getName());
                    intent.putExtra("position", position);
                    parent.startActivityForResult(intent, 1);
                    parent.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });
            if (thisGroup.hasAnyColor()) {
                VHitem.mImageView.setImageResource(R.drawable.color_spectrum_tiny);
                VHitem.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ColorChangeActivity.class);
                        intent.putExtra("position", position);
                        parent.startActivityForResult(intent, 2);
                        parent.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }
            VHitem.mTextView.setText(thisGroup.getName());
            VHitem.mLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        parent.turnGroupOn(position);
                    } else {
                        parent.turnGroupOff(position);
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void turnOnSwitch(int position) {
        ((ViewHolderItem) viewHolderList.get(position)).mLightSwitch.setChecked(true);
    }
}
