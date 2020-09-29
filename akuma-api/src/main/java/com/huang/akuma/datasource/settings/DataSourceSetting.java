package com.huang.akuma.datasource.settings;

import lombok.Data;

/**
 * @author Huang.zh
 * @date 2020/9/28 14:40
 * @Description: 数据源信息封装
 */
@Data
public class DataSourceSetting {

    public DataSourceSetting(String url, String username, String password) {
        this(null,null,url,username,password);
    }

    public DataSourceSetting(String name, String url, String username, String password) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public DataSourceSetting(String id, String name, String url, String username, String password) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private String id;

    private String name;

    private String url;

    private String username;

    private String password;


}
