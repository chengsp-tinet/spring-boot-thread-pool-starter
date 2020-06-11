package com.github.threadpool.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 自动配置线程池
 *
 * @author chengsp
 * @date 2020/5/29 16:16
 */
@Configurable
@Slf4j
@EnableConfigurationProperties(GlobalConfig.class)
@ConditionalOnProperty(prefix = GlobalConfig.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThreadPoolAutoConfiguration {
    @Autowired
    private GlobalConfig globalConfig;
    @Autowired
    private ApplicationContext applicationContext;

    private static final Map<String, TimeUnit> TIME_UNIT_MAP = new HashMap<>();
    private static final Map<String, RejectedExecutionHandler> REJECTED_EXECUTION_HANDLER_MAP = new HashMap<>();

    static {
        TIME_UNIT_MAP.put("nanoseconds", TimeUnit.NANOSECONDS);
        TIME_UNIT_MAP.put("microseconds", TimeUnit.MICROSECONDS);
        TIME_UNIT_MAP.put("milliseconds", TimeUnit.MILLISECONDS);
        TIME_UNIT_MAP.put("seconds", TimeUnit.SECONDS);
        TIME_UNIT_MAP.put("minutes", TimeUnit.MINUTES);
        TIME_UNIT_MAP.put("hours", TimeUnit.HOURS);
        TIME_UNIT_MAP.put("days", TimeUnit.DAYS);

        REJECTED_EXECUTION_HANDLER_MAP.put("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy());
        REJECTED_EXECUTION_HANDLER_MAP.put("AbortPolicy", new ThreadPoolExecutor.AbortPolicy());
        REJECTED_EXECUTION_HANDLER_MAP.put("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy());
        REJECTED_EXECUTION_HANDLER_MAP.put("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        Integer corePoolSize = globalConfig.getCorePoolSize();
        Integer maximumPoolSize = globalConfig.getMaximumPoolSize();
        Long keepAliveTime = globalConfig.getKeepAliveTime();
        String timeUnit = globalConfig.getTimeUnit();
        Integer queueSize = globalConfig.getQueueSize();
        String rejectStrategy = globalConfig.getRejectStrategy();
        String rejectStrategyBeanName = globalConfig.getRejectStrategyBeanName();
        String threadFactoryBeanName = globalConfig.getThreadFactoryBeanName();
        ThreadFactory threadFactory;
        RejectedExecutionHandler rejectedExecutionHandler;
        TimeUnit unit = TIME_UNIT_MAP.get(timeUnit);
        if (unit == null) {
            throw new RuntimeException("请为timeUnit指定正确的时间单位,该值只能为nanoseconds,microseconds, milliseconds ,seconds," +
                    " minutes ,hours ,days中其一");
        }
        if (threadFactoryBeanName == null || threadFactoryBeanName.length() == 0) {
            threadFactory = DEFAULT_THREAD_FACTORY;
        } else {
            Object bean = applicationContext.getBean(threadFactoryBeanName);
            if (!(bean instanceof ThreadFactory)) {
                throw new RuntimeException(threadFactoryBeanName + "对应的bean的类型不是java.util.concurrent.ThreadFactory的子类型," +
                        "请指定一个正确类型的bean名称");
            }
            threadFactory = (ThreadFactory) bean;
        }
        if (rejectStrategy == null || "".equals(rejectStrategy)) {
            if (rejectStrategyBeanName != null && rejectStrategyBeanName.length() > 0) {
                Object rejectStrategyBean = applicationContext.getBean(rejectStrategyBeanName);
                if (!(rejectStrategyBean instanceof RejectedExecutionHandler)) {
                    throw new RuntimeException(rejectStrategyBeanName + "对应的bean的类型不是java.util.concurrent.RejectedExecutionHandler的子类型," +
                            "请指定一个正确类型的bean名称");
                }
                rejectedExecutionHandler = ((RejectedExecutionHandler) rejectStrategyBean);
            } else {
                rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
            }
        } else {
            if (!REJECTED_EXECUTION_HANDLER_MAP.containsKey(rejectStrategy)) {
                throw new RuntimeException("不存在这样的预设的拒绝策略,请为rejectStrategy指定正确的值,该值只能为" +
                        "CallerRunsPolicy,AbortPolicy,DiscardPolicy,DiscardOldestPolicy中其一");
            }
            rejectedExecutionHandler = REJECTED_EXECUTION_HANDLER_MAP.get(rejectStrategy);

        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<>(queueSize), threadFactory, rejectedExecutionHandler);
        log.info("globalConfig:{}", globalConfig);
        log.info("线程池配置成功:{}", threadPoolExecutor);
        return threadPoolExecutor;
    }

        private static final ThreadFactory DEFAULT_THREAD_FACTORY = Thread::new;
}
