package io.virtualapp.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;

public class GroupAdapter extends BaseAdapter {

    private Context context;

    private List<String> list;

    public GroupAdapter(Context context, List<String> list) {

        this.context = context;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ivy_privacy_space_group_item_view, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.group_text = (TextView) convertView.findViewById(R.id.group_text);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.group_text.setText(list.get(position));
        return convertView;
    }

    static class ViewHolder {
        TextView group_text;
    }

}
