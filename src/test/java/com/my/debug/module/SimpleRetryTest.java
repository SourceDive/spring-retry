package com.my.debug.module;

import org.junit.Test;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.Assert.*;

/**
 * 简单的Spring Retry测试模块 (JDK 1.8版本)
 * 用于验证Spring Retry的基本功能
 */
public class SimpleRetryTest {

    /**
     * 测试基本的重试功能
     * 模拟一个会失败几次然后成功的方法
     */
    @Test
    public void testBasicRetry() throws Exception {
        // 创建重试模板
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 设置重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 计数器，用于模拟失败
        final int[] attemptCount = {0};
        
        // 执行重试操作 - 使用JDK 1.8的泛型语法
        String result = retryTemplate.execute(new RetryCallback<String>() {
            @Override
            public String doWithRetry(RetryContext context) throws Exception {
                attemptCount[0]++;
                System.out.println("尝试第 " + attemptCount[0] + " 次");
                
                // 前两次失败，第三次成功
                if (attemptCount[0] < 3) {
                    throw new RuntimeException("模拟失败，第 " + attemptCount[0] + " 次尝试");
                }
                
                return "成功！";
            }
        });
        
        // 验证结果
        assertEquals("成功！", result);
        assertEquals(3, attemptCount[0]); // 总共尝试了3次
        System.out.println("测试通过：重试功能正常工作");
    }
    
    /**
     * 测试重试次数耗尽的情况
     */
    @Test
    public void testRetryExhausted() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 设置重试策略：最多重试2次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        final int[] attemptCount = {0};
        
        try {
            retryTemplate.execute(new RetryCallback<String>() {
                @Override
                public String doWithRetry(RetryContext context) throws Exception {
                    attemptCount[0]++;
                    System.out.println("尝试第 " + attemptCount[0] + " 次（总是失败）");
                    throw new RuntimeException("总是失败");
                }
            });
            fail("应该抛出异常");
        } catch (Exception e) {
            // 验证重试次数
            assertEquals(2, attemptCount[0]);
            System.out.println("测试通过：重试次数耗尽，抛出异常");
        }
    }
    
    /**
     * 测试一次就成功的情况
     */
    @Test
    public void testSuccessOnFirstAttempt() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        final int[] attemptCount = {0};
        
        String result = retryTemplate.execute(new RetryCallback<String>() {
            @Override
            public String doWithRetry(RetryContext context) throws Exception {
                attemptCount[0]++;
                System.out.println("尝试第 " + attemptCount[0] + " 次（一次成功）");
                return "一次成功！";
            }
        });
        
        assertEquals("一次成功！", result);
        assertEquals(1, attemptCount[0]); // 只尝试了1次
        System.out.println("测试通过：一次成功，无需重试");
    }
    
    /**
     * 使用Lambda表达式的简化版本 (JDK 1.8特性)
     */
    @Test
    public void testRetryWithLambda() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        final int[] attemptCount = {0};
        
        // 使用Lambda表达式简化代码
        String result = retryTemplate.execute(context -> {
            attemptCount[0]++;
            System.out.println("Lambda尝试第 " + attemptCount[0] + " 次");
            
            if (attemptCount[0] < 3) {
                throw new RuntimeException("Lambda模拟失败");
            }
            
            return "Lambda成功！";
        });
        
        assertEquals("Lambda成功！", result);
        assertEquals(3, attemptCount[0]);
        System.out.println("测试通过：Lambda表达式重试功能正常工作");
    }
}