package com.my.debug.module;

import org.junit.Test;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Spring Retry 使用示例 (JDK 1.8版本)
 * 展示如何在实际项目中使用Spring Retry
 */
public class RetryExample {

    /**
     * 模拟一个不稳定的服务调用
     * 这个方法可能会失败，需要重试
     */
    public String callUnstableService(int attemptNumber) throws Exception {
        System.out.println("调用不稳定服务，第 " + attemptNumber + " 次");
        
        // 模拟网络延迟或服务不稳定
        if (Math.random() < 0.7) { // 70%的概率失败
            throw new RuntimeException("服务调用失败");
        }
        
        return "服务调用成功！";
    }
    
    /**
     * 使用Spring Retry包装不稳定的服务调用
     */
    @Test
    public void testRetryWithUnstableService() throws Exception {
        // 创建重试模板
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 配置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5); // 最多重试5次
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 计数器
        final int[] attemptCount = {0};
        
        // 执行重试操作 - 使用泛型
        String result = retryTemplate.execute(new RetryCallback<String>() {
            @Override
            public String doWithRetry(RetryContext context) throws Exception {
                attemptCount[0]++;
                return callUnstableService(attemptCount[0]);
            }
        });
        
        System.out.println("最终结果: " + result);
        System.out.println("总共尝试了 " + attemptCount[0] + " 次");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(attemptCount[0] >= 1 && attemptCount[0] <= 5);
    }
    
    /**
     * 使用Lambda表达式的简化版本
     */
    @Test
    public void testRetryWithLambda() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        final int[] attemptCount = {0};
        
        // 使用Lambda表达式
        String result = retryTemplate.execute(context -> {
            attemptCount[0]++;
            return callUnstableService(attemptCount[0]);
        });
        
        System.out.println("Lambda结果: " + result);
        System.out.println("Lambda尝试次数: " + attemptCount[0]);
        
        assertNotNull(result);
    }
    
    /**
     * 演示如何配置不同的重试策略
     */
    @Test
    public void testDifferentRetryStrategies() throws Exception {
        System.out.println("=== 测试不同的重试策略 ===");
        
        // 策略1：只重试1次
        testRetryStrategy(1, "只重试1次");
        
        // 策略2：重试3次
        testRetryStrategy(3, "重试3次");
        
        // 策略3：重试10次（几乎总是会成功）
        testRetryStrategy(10, "重试10次");
    }
    
    private void testRetryStrategy(int maxAttempts, String strategyName) throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        final int[] attemptCount = {0};
        
        try {
            String result = retryTemplate.execute(context -> {
                attemptCount[0]++;
                return callUnstableService(attemptCount[0]);
            });
            
            System.out.println(strategyName + " - 成功！结果: " + result + "，尝试次数: " + attemptCount[0]);
        } catch (Exception e) {
            System.out.println(strategyName + " - 失败！尝试次数: " + attemptCount[0] + "，错误: " + e.getMessage());
        }
    }
    
    /**
     * 使用函数式接口的高级示例
     */
    @Test
    public void testRetryWithFunctionalInterface() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 使用Supplier函数式接口
        Supplier<String> serviceCall = () -> {
            if (Math.random() < 0.6) {
                throw new RuntimeException("服务调用失败");
            }
            return "服务调用成功！";
        };
        
        String result = retryTemplate.execute(context -> serviceCall.get());
        
        System.out.println("函数式接口结果: " + result);
        assertNotNull(result);
    }
}