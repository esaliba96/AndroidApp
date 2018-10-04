package com.Poly.Kenner_Saliba.gradesniffer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
  private ArrayList<String> objects;

  public CustomSpinnerAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
    super(context, textViewResourceId, objects);
    this.objects = objects;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    return getCustomView(position, convertView, parent);
  }

  private View getCustomView(final int position, View convertView, ViewGroup parent) {
    View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_design, parent, false);
    final TextView label = (TextView) row.findViewById(R.id.tv_spinnervalue);
    label.setText(objects.get(position));
    return row;
  }
}