package priv.koishi.pmc.Bean.Interface;

/**
 * 可复制的列表数据类接口
 *
 * @author KOISHI
 * Date:2026-01-22
 * Time:18:34
 */
public interface CopyBean {

    /**
     * 创建并返回一个深拷贝
     *
     * @param <T> 泛型，必须实现 CopyBean 接口
     * @return 新的实例
     * @throws Exception 反射相关异常
     */
    <T extends CopyBean> T createCopy() throws Exception;

    /**
     * 更新 UUID
     */
    void updateUuid();

}
