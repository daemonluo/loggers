package com.daemon.loggers;

import java.io.Serializable;
import java.util.Map;

public interface Event extends Serializable {
    /**
     * @return 事件名称
     */
    String getName();

    /**
     * @return 产生事件的客户端
     */
    String getClient();

    /**
     * @return 产生事件的平台
     */
    String getPlatform();

    /**
     * @return 事件属性
     */
    Map<String, String> getProperties();

    /**
     * @return 事件生成的时间
     */
    long getTime();
}
