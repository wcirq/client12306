package cn.wcy.treelibrary;

import android.view.View;
import android.widget.AdapterView;

public interface OnInnerItemLongClickListener<T extends Node<T>> {
    void onLongClick(T node, AdapterView<?> parent, View view, int position);
}
