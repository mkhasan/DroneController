package com.drone.pi.dronecontroller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.media.RemotePlaybackClient;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Created by usrc on 17. 8. 31.
 */

interface ItemsAapdeterListener {
    public void onItemSelected(int position);
}

interface ViewHolderListener {
    public void onCancel(int position);
    public void onSelect(int position);
}


public class ItemsAdapter extends BaseAdapter implements ViewHolderListener {

    public enum Mode {
        HISTORY,
        LOCATION
    }

    static final String TAG = ItemsAdapter.class.getSimpleName();

    @NonNull
    private final LayoutInflater inflater;
    private Mode currentMode = Mode.HISTORY;
    private List<SearchHistory> history = null;
    private List<HashMap<String, String>> location = null;

    private ItemsAapdeterListener listener = null;

    public static class ViewHolder {

        @NonNull
        private final View view;
        @NonNull
        private final TextView textView;
        @NonNull
        protected final ImageView historyIcon;
        @NonNull
        private final ImageButton cancelBtn;

        public static ViewHolder fromConvertView(@NonNull View convertView) {
            return (ViewHolder) convertView.getTag();
        }

        ViewHolderListener listener = null;

        Bitmap imageHistoryIcon;
        Bitmap imageLocationIcon;

        public ViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            view = inflater.inflate(R.layout.list_item, parent, false);

            LinearLayout layout = view.findViewById(R.id.list_item_layout);
            ListView.LayoutParams param= (ListView.LayoutParams)layout.getLayoutParams();
            param.height = GlobalData.searchListItemWidth;
            param.height = GlobalData.searchListItemHeight;
            layout.setLayoutParams(param);

            historyIcon = view.findViewById(R.id.history_icon);
            LinearLayout.LayoutParams linParam = (LinearLayout.LayoutParams) historyIcon.getLayoutParams();
            linParam.height = GlobalData.searchListItemHeight;
            linParam.width = linParam.height;
            historyIcon.setLayoutParams(linParam);

            cancelBtn = (ImageButton) view.findViewById(R.id.cancel_btn);
            linParam = (LinearLayout.LayoutParams) cancelBtn.getLayoutParams();
            linParam.height = GlobalData.searchListItemHeight;
            linParam.width = linParam.height;
            cancelBtn.setLayoutParams(linParam);



            textView = (TextView) view.findViewById(R.id.list_item_text);
            view.setTag(this);

            imageHistoryIcon = BitmapFactory.decodeResource(view.getResources(), R.drawable.icon_history);
            imageLocationIcon = BitmapFactory.decodeResource(view.getResources(), R.drawable.icon_location);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                        listener.onSelect((int) view.getTag());
                }
            });
            /*
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("Search", "this");
                }
            });
            */



            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("Search", "btn clicked " + view.getTag());
                    if (listener != null)
                        listener.onCancel((int)view.getTag());

                }
            });

        }

        @NonNull
        public View getView() {
            return view;
        }

        public void bindHistory(@NonNull List<SearchHistory> list , int position) {


            String text = "";
            SearchHistory item = list.get(position);
            if (item != null)
                text = item.formattedAddress;

            if (text == null || text.equals(""))
                text = "Not found";

            textView.setText(text);
            textView.setTag(position);
            cancelBtn.setTag(position);

            historyIcon.setImageBitmap(imageHistoryIcon);
            //historyIcon.setVisibility(View.VISIBLE);


        }


        public void bindLocation(@NonNull List<HashMap<String, String>> list , int position) {


            String text = "";
            HashMap<String, String> item = list.get(position);
            if (item != null)
                text = item.get(LocationHandler.FORMATTED_ADDR);

            if (text == null || text.equals(""))
                text = "Not found";

            textView.setText(text);
            textView.setTag(position);
            cancelBtn.setTag(position);

            historyIcon.setImageBitmap(imageLocationIcon);
            //historyIcon.setVisibility(View.GONE);


        }
    }

    public ItemsAdapter(@NonNull LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public int getCount() {

        if (currentMode == Mode.HISTORY)
            return history.size();

        else
            return location.size();

    }


    @Override
    public Object getItem(int position) {
        if (currentMode == Mode.HISTORY)
            return (Object) history.get(position);
        else
            return (Object) location.get(position);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder(inflater, parent);
            holder.listener = this;
            convertView = holder.getView();
        } else {
            holder = ViewHolder.fromConvertView(convertView);
        }

        if (currentMode == Mode.HISTORY)
            holder.bindHistory(history, position);
        else
            holder.bindLocation(location, position);


        return convertView;
    }


    public void swapHistoryItems(List<SearchHistory> _history) {


        history = _history;
        if(currentMode == Mode.HISTORY)
            notifyDataSetChanged();
    }


    public void swapLocationItems(List<HashMap<String, String>> _location) {
        location = _location;
        if(currentMode == Mode.LOCATION)
            notifyDataSetChanged();
    }


    public Mode getMode() {
        return currentMode;
    }

    public void setMode(Mode _mode) {
        currentMode = _mode;
    }


    @Override
    public void onCancel(int  position) {
        Log.e(TAG, "onCancel()" + position);
        if(currentMode == Mode.HISTORY)
            history.remove(position);
        else
            location.remove(position);

        notifyDataSetChanged();
    }

    @Override
    public void onSelect(int position) {
        if (listener != null)
            listener.onItemSelected(position);
    }

    public void setListener(ItemsAapdeterListener _listener) {
        listener = _listener;
    }


}
