/*******************************************************************
 * Copyright  (C) Newegg Corporation. All rights reserved.
 * <p/>
 * Author: Roder.Y.Hu (Roder.Y.Hu@newegg.com)
 * Create Date: 2016/4/15
 * Usage:
 * <p/>
 * RevisionHistory
 * Date    		Author    Description
 ********************************************************************/
package com.sample.mqtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HistoryMessageAdapter extends BaseAdapter{

    private Context context;
    private List<String> data = new ArrayList<>();

    public HistoryMessageAdapter(Context context, List<String> initHistories){
        this.context = context;
        data = initHistories;
    }

    public void updateMessage(List<String> newData){
        data = newData;
        notifyDataSetChanged();
    }

    public void addMessage(String messsage){
        data.add(messsage);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_message_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = ((ViewHolder) convertView.getTag());
        }

        holder.content.setText(getItem(position));

        return convertView;
    }

    class ViewHolder{
        private TextView content;

        ViewHolder(View view){
            content = (TextView)view.findViewById(R.id.adapterMessageItem_content);
        }

    }
}
