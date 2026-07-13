package com.hrms.server.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * MyBatis-Plus Mapper Bean 定义修复器
 *
 * 解决 Spring Boot 3.5.x + MyBatis-Plus 兼容性问题：
 * Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
 */
@Component
public class MapperBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (beanName.endsWith("Mapper")) {
                try {
                    var beanDefinition = beanFactory.getBeanDefinition(beanName);
                    if (beanDefinition != null) {
                        // 移除可能导致问题的属性
                        beanDefinition.removeAttribute("factoryBeanObjectType");
                    }
                } catch (Exception e) {
                    // 忽略无法处理的 bean
                }
            }
        }
    }
}
