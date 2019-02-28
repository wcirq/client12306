package cn.wcy.treelibrary;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Node<T extends Node> implements Comparable<Node> {
    /** 自己的id */
    public int id;
    /** 上一层id */
    public int pId;
    /** 层级 */
    public int level;
    /** 是否展开 */
    public boolean isExpand;
    /** 子节点 */
    public List<T> childNodes;

    public Node() {
    }

    public Node(int id, int pId, int level, boolean isExpand) {
        this.id = id;
        this.pId = pId;
        this.level = level;
        this.isExpand = isExpand;
    }

    @Override
    public int compareTo(@NonNull Node o) {
        return id - o.id;
    }

    public boolean hasChild() {
        return childNodes != null && !childNodes.isEmpty();
    }

    public void addChild(T node) {
        if (childNodes == null) {
            childNodes = new ArrayList<>();
        }
        childNodes.add(node);
    }
}
