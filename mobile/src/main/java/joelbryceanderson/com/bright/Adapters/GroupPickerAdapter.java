package joelbryceanderson.com.bright.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;

import joelbryceanderson.com.bright.Activities.GroupPickerActivity;
import joelbryceanderson.com.bright.R;

public class GroupPickerAdapter extends RecyclerView.Adapter<GroupPickerAdapter.ViewHolder> {
    private List<PHLight> lightList;
    private Context mContext;
    private Boolean group;
    private Boolean[] checked;
    private List<PHLight> listToReturn = new ArrayList<>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView mTextView;
        protected CheckBox mCheckBox;
        protected LinearLayout mWholeItem;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.group_picker_name);
            mCheckBox = (CheckBox) v.findViewById(R.id.group_picker_radio);
            mWholeItem = (LinearLayout) v.findViewById(R.id.whole_item_group_picker);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupPickerAdapter(List<PHLight> myDataset, Boolean group) {
        lightList = myDataset;
        this.group = group;
        checked = new Boolean[lightList.size()];
        for (int i = 0; i < checked.length; ++i) {
            checked[i] = false;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupPickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_picker, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(lightList.get(position).getName());
        holder.mCheckBox.setTag(position);
        holder.mWholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mCheckBox.toggle();
            }
        });
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listToReturn.add(lightList.get(position));
                    checked[position] = true;
                    if (group) {
                        ((GroupPickerActivity) mContext).showFab();
                    }
                } else {
                    checked[position] = false;
                    if (listToReturn.contains(lightList.get(position))) {
                        listToReturn.remove(lightList.get(position));
                    }
                    if (listToReturn.isEmpty()) {
                        if (group) {
                            ((GroupPickerActivity) mContext).hideFab();
                        }
                    }
                }
            }
        });
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        Boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            holder.mTextView.setTextColor(Color.parseColor("#ffffff"));
            holder.mWholeItem.setBackgroundColor(Color.parseColor("#000000"));
        }
        holder.mCheckBox.setChecked(checked[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return lightList.size();
    }

    public List<PHLight> getListToReturn() {
        return listToReturn;
    }
}
