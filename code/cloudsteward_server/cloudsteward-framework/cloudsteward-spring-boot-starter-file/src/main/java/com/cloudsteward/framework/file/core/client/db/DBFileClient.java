package com.cloudsteward.framework.file.core.client.db;

import cn.hutool.extra.spring.SpringUtil;
import com.cloudsteward.framework.file.core.client.AbstractFileClient;

/**
 * 基于 DB 存储的文件客户端的配置类
 *
 * @author 芋道源码
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private DBFileContentFrameworkDAO dao;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
    }

    @Override
    public String getDomain() {
        return config.getDomain();
    }

    @Override
    public String upload(byte[] content, String path, String type, String domain) {
        getDao().insert(getId(), path, content);
        // 拼接返回路径
        return super.formatFileUrl(domain, path);
    }

    @Override
    public void delete(String path) {
        getDao().delete(getId(), path);
    }

    @Override
    public byte[] getContent(String path) {
        return getDao().selectContent(getId(), path);
    }

    private DBFileContentFrameworkDAO getDao() {
        // 延迟获取，因为 SpringUtil 初始化太慢
        if (dao == null) {
            dao = SpringUtil.getBean(DBFileContentFrameworkDAO.class);
        }
        return dao;
    }

}
