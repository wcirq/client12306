package cn.wcy.treelibrary;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TreeAdapter<T extends Node<T>> extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private List<T> totalNodes = new ArrayList<>();
    private List<T> showNodes = new ArrayList<>();
    private List<T> firstLevelNodes = new ArrayList<>();
    private SparseIntArray addedChildNodeIds = new SparseIntArray();
    private OnInnerItemClickListener<T> listener;
    private OnInnerItemLongClickListener<T> longListener;
    private OnExpandableItemClickListerner<T> expandableListener;
    private OnExpandableItemLongClickListener<T> expandableLongClickListener;

    public TreeAdapter(ListView lv, List<T> nodes) {
        setNodes(nodes);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
    }

    public void setOnInnerItemClickListener(OnInnerItemClickListener<T> listener) {
        this.listener = listener;
    }

    public void setOnInnerItemLongClickListener(OnInnerItemLongClickListener<T> listener) {
        longListener = listener;
    }

    public void setOnExpandableItemClickListerner(OnExpandableItemClickListerner<T> listerner) {
        expandableListener = listerner;
    }

    public void setOnExpandableItemLongClickListener(OnExpandableItemLongClickListener<T> listerner) {
        expandableLongClickListener = listerner;
    }

    public void setNodes(List<T> nodes) {
        if (nodes != null) {
            totalNodes = nodes;
            //过滤出显示的节点
            init();
            super.notifyDataSetChanged();
        }
    }

    private void init() {
        showNodes.clear();
        initNodes();
        addedChildNodeIds.clear();
        showNodes.addAll(firstLevelNodes);
        filterShowAndSortNodes();
    }

    @Override
    public void notifyDataSetChanged() {
        init();
        super.notifyDataSetChanged();
    }

    private void initNodes() {
        firstLevelNodes.clear();
        //先循环一次，获取最小的level
        Integer level = null;
        for (T node : totalNodes) {
            if (level == null || level > node.level) {
                level = node.level;
            }
        }
        for (T node : totalNodes) {
            //过滤出最外层
            if (node.level == level) {
                firstLevelNodes.add(node);
            }
            //清空之前添加的
            if (node.hasChild()) {
                node.childNodes.clear();
            }
            //给节点添加子节点并排序
            for (T t : totalNodes) {
                if (node.id == t.id && node != t) {
                    throw new IllegalArgumentException("id cannot be duplicated");
                }
                if (node.id == t.pId && node.level != t.level) {
                    node.addChild(t);
                }
            }
            if (node.hasChild()) {
                Collections.sort(node.childNodes);
            }
        }
        Collections.sort(firstLevelNodes);
    }

    private void filterShowAndSortNodes() {
        for (int i = 0; i < showNodes.size(); i++) {
            T node = showNodes.get(i);
            int value = addedChildNodeIds.get(node.id);
            if (value == 0 && node.isExpand && node.hasChild()) {
                List<T> list = new ArrayList<>(showNodes);
                list.addAll(i + 1, node.childNodes);
                showNodes = list;
                addedChildNodeIds.put(node.id, 1);
                filterShowAndSortNodes();
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return showNodes.size();
    }

    @Override
    public T getItem(int position) {
        return showNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder<T> holder;
        if (convertView == null) {
            holder = getHolder(position);
        } else {
            holder = (Holder<T>) convertView.getTag();
        }
        T node = showNodes.get(position);
        holder.setData(node, position);
        return holder.convertView;
    }

    public abstract static class Holder<T> {
        private View convertView;

        protected Holder() {
            convertView = createConvertView();
            convertView.setTag(this);
        }

        public View getConvertView() {
            return convertView;
        }

        /**
         * 设置数据
         * @param node 节点数据
         * @param position 条目位置
         */
        protected abstract void setData(T node, int position);

        /**
         * 创建界面
         * @return 返回布局
         */
        protected abstract View createConvertView();
    }

    protected abstract Holder<T> getHolder(int position);

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        T node = getItem(position);
        if (node.hasChild()) {
            node.isExpand = !node.isExpand;
            if (!node.isExpand) {
                fold(node.childNodes);
            }
            showNodes.clear();
            addedChildNodeIds.clear();
            showNodes.addAll(firstLevelNodes);
            filterShowAndSortNodes();
            TreeAdapter.super.notifyDataSetChanged();
            if (expandableListener != null) {
                expandableListener.onExpandableItemClick(node, parent, view, position);
            }
        } else if (listener != null) {
            listener.onClick(node, parent, view, position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        T node = getItem(position);
        if (node.hasChild()) {
            if (expandableLongClickListener != null) {
                expandableLongClickListener.onExpandableItemLongClick(node, parent, view, position);
            }
        } else if (longListener != null) {
            longListener.onLongClick(node, parent, view, position);
        }
        return true;
    }

    //递归收起节点及子节点
    private void fold(List<T> list) {
        for (T t : list) {
            t.isExpand = false;
            if (t.hasChild()) {
                fold(t.childNodes);
            }
        }
    }
}
