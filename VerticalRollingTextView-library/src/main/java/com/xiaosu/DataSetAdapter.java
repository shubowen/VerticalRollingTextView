package com.xiaosu;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：疏博文 创建于 2016-05-13 11:48
 * 邮箱：shubowen123@sina.cn
 * 描述：VerticalRollingTextView的数据适配器
 */
public abstract class DataSetAdapter<T> {

    private List<T> data = new ArrayList<>();

    public DataSetAdapter() {
    }

    public DataSetAdapter(List<T> data) {
        this.data = data;
    }

    /**
     * @param index 当前角标
     * @return 待显示的字符串
     */
    final public CharSequence getText(int index) {
        return text(data.get(index));
    }

    protected abstract CharSequence text(T t);

    public int getItemCount() {
        return null == data || data.isEmpty() ? 0 : data.size();
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public boolean isEmpty() {
        return null == data || data.isEmpty();
    }
}
