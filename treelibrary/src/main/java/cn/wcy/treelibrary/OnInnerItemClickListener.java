package cn.wcy.treelibrary;

import android.view.View;
import android.widget.AdapterView;

public interface OnInnerItemClickListener<T extends Node<T>> {
    void onClick(T node, AdapterView<?> parent, View view, int position);
}
