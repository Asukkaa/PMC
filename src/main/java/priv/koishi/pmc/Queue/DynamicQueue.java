package priv.koishi.pmc.Queue;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author KOISHI
 * Date:2025-05-08
 * Time:19:08
 */
public class DynamicQueue<E> {

    /**
     * 底层容器
     */
    private final ConcurrentLinkedDeque<E> delegate = new ConcurrentLinkedDeque<>();

    /**
     * 动态容量，默认无上限
     */
    private volatile int maxSize = Integer.MAX_VALUE;

    /**
     * 添加元素时自动淘汰旧数据
     */
    public boolean add(E element) {
        if (element == null) {
            return false;
        }
        // 新数据放在最前
        delegate.addFirst(element);
        adjustSize();
        return true;
    }

    /**
     * 批量添加元素，保持原集合顺序
     *
     * @param elements 覜添加的元素集合
     */
    public void addAll(Collection<? extends E> elements) {
        if (CollectionUtils.isNotEmpty(elements)) {
            List<? extends E> list = new ArrayList<>(elements);
            // 确保顺序一致
            Collections.reverse(list);
            for (E element : elements) {
                if (element != null) {
                    // 每个元素添加到队头
                    delegate.addFirst(element);
                }
            }
            adjustSize();
        }
    }

    /**
     * 从另一个 DynamicQueue 中添加所有元素，保持原顺序
     *
     * @param otherQueue 要添加的 DynamicQueue
     */
    public void addAll(DynamicQueue<? extends E> otherQueue) {
        if (otherQueue != null && !otherQueue.delegate.isEmpty()) {
            Iterator<? extends E> it = otherQueue.delegate.descendingIterator();
            while (it.hasNext()) {
                delegate.addFirst(it.next());
            }
            adjustSize();
        }
    }

    /**
     * 调整delegate的大小以确保不超过预设的最大容量maxSize。
     */
    private void adjustSize() {
        // 循环检查delegate的当前大小，若超过maxSize则移除末尾元素
        while (delegate.size() > maxSize) {
            delegate.pollLast();
        }
    }

    /**
     * 获取当前快照（线程安全）
     */
    public List<E> getSnapshot() {
        List<E> snapshot = new ArrayList<>();
        // 按添加顺序遍历
        Iterator<E> it = delegate.descendingIterator();
        while (it.hasNext()) {
            snapshot.add(it.next());
        }
        return snapshot;
    }

    /**
     * 设置新容量（可运行时修改）
     */
    public void setMaxSize(int newSize) {
        this.maxSize = newSize;
        adjustSize();
    }

    /**
     * 获取当前大小
     */
    public int size() {
        return delegate.size();
    }

}