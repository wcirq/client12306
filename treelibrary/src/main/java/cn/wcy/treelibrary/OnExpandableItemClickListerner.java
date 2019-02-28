package cn.wcy.treelibrary;

import android.view.View;
import android.widget.AdapterView;

public interface OnExpandableItemClickListerner<T extends Node<T>> {
    void onExpandableItemClick(T node, AdapterView<?> parent, View view, int position);
}
