package priv.koishi.pmc.Queue;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态容量队列
 *
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
     * 近似计数器
     */
    private final AtomicInteger approximateSize = new AtomicInteger(0);

    /**
     * 添加元素时自动淘汰旧数据（将会忽略空元素）
     *
     * @param element 添加的元素
     */
    public void add(E element) {
        if (element != null) {
            // 新数据放在最前
            delegate.addFirst(element);
            approximateSize.incrementAndGet();
            adjustSize();
        }
    }

    /**
     * 批量添加元素，保持原集合顺序（将会忽略空元素）
     *
     * @param elements 添加的元素集合
     */
    public void addAll(Collection<? extends E> elements) {
        if (CollectionUtils.isNotEmpty(elements)) {
            int validCount = 0;
            for (E element : elements) {
                if (element != null) {
                    // 每个元素添加到队头
                    delegate.addFirst(element);
                    validCount++;
                }
            }
            approximateSize.addAndGet(validCount);
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
            int validCount = 0;
            while (it.hasNext()) {
                E element = it.next();
                if (element != null) {
                    delegate.addFirst(element);
                    validCount++;
                }
            }
            approximateSize.addAndGet(validCount);
            adjustSize();
        }
    }

    /**
     * 调整delegate的大小以确保不超过预设的最大容量maxSize。
     */
    private void adjustSize() {
        if (approximateSize.get() > maxSize) {
            synchronized (this) {
                int actualSize = approximateSize.get();
                if (actualSize > maxSize) {
                    int elementsToRemove = actualSize - maxSize;
                    int removedCount = 0;
                    for (int i = 0; i < elementsToRemove; i++) {
                        if (delegate.pollLast() == null) {
                            break;
                        }
                        removedCount++;
                    }
                    if (removedCount > 0) {
                        approximateSize.addAndGet(-removedCount);
                    }
                }
            }
        }
    }

    /**
     * 获取当前快照（线程安全）
     *
     * @return 快照
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
     * 设置新容量
     *
     * @param newSize 新容量
     * @throws IllegalArgumentException 容量不能小于0
     */
    public void setMaxSize(int newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException("容量不能小于0");
        }
        this.maxSize = newSize;
        synchronized (this) {
            adjustSize();
        }
    }

    /**
     * 获取当前大小
     *
     * @return 当前队列大小
     */
    public int size() {
        return delegate.size();
    }

}