package cn.wcy.treelibrary;

import android.view.View;
import android.widget.AdapterView;

public interface OnExpandableItemLongClickListener<T extends Node<T>> {
    void onExpandableItemLongClick(T node, AdapterView<?> parent, View view, int position);
}

