package priv.koishi.pmc.Bean.AbstractBean;

import priv.koishi.pmc.Bean.Interface.CopyBean;

import static priv.koishi.pmc.Utils.CommonUtils.copyAllProperties;

/**
 * 可复制的列表数据基类
 *
 * @author KOISHI
 * Date:2026-01-22
 * Time:19:02
 */
public abstract class BaseCopyBean implements CopyBean {

    /**
     * 创建并返回一个深拷贝
     *
     * @param <T> 泛型，必须实现 CopyBean 接口
     * @return 新的实例
     * @throws Exception 反射相关异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CopyBean> T createCopy() throws Exception {
        T copy = (T) getClass().getDeclaredConstructor().newInstance();
        copyAllProperties(this, copy);
        copy.updateUuid();
        return copy;
    }

    /**
     * 更新 UUID
     */
    @Override
    public abstract void updateUuid();

}
