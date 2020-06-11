package com.github.threadpool.autoconfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 全局配置
 *
 * @author chengsp
 * @date 2020/5/29 14:54
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = GlobalConfig.CONFIG_PREFIX)
public class GlobalConfig {

    public static final String CONFIG_PREFIX = "spring.thread.pool";
    private Boolean enabled;
    /**
     * 核心线程大小
     */
    private Integer corePoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * 最大线程数
     */
    private Integer maximumPoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * 空闲线程等待工作的超时时间
     */
    private Long keepAliveTime = 0L;
    /**
     * 超时时间单位
     */
    private String timeUnit = "milliseconds";
    /**
     * 队列大小
     */
    private Integer queueSize = 10000;
    /**
     * 在执行饱和或关闭时调用处理策略
     */
    private String rejectStrategy;
    /**
     * 在执行饱和或关闭时调用处理策略的bean的名称,
     * rejectStrategy和rejectStrategyBeanName只能存在一个
     */
    private String rejectStrategyBeanName;
    /**
     * 线程工厂对应的bean的名称
     */
    private String threadFactoryBeanName;

}
