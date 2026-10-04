package com.evops.config;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {
    @Bean
    public Realm localRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("bootstrap", "bootstrap", "system");
        return realm;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chain = new DefaultShiroFilterChainDefinition();
        chain.addPathDefinition("/api/health", "anon");
        chain.addPathDefinition("/error", "anon");
        chain.addPathDefinition("/**", "authc");
        return chain;
    }

    /**
     * Shiro 自动配置的 defaultAdvisorAutoProxyCreator 默认使用 JDK 接口代理，
     * 会导致按具体类注入 Service（如 ArtifactService）失败。
     * 这里将其 proxyTargetClass 置为 true，切换为 CGLIB 类代理（与 Spring Boot 默认行为一致）。
     */
    @Bean
    public static BeanFactoryPostProcessor shiroAutoProxyClassModeCustomizer() {
        return beanFactory -> {
            String apcBeanName = "defaultAdvisorAutoProxyCreator";
            if (beanFactory.containsBeanDefinition(apcBeanName)) {
                BeanDefinition definition = beanFactory.getBeanDefinition(apcBeanName);
                definition.getPropertyValues().add("proxyTargetClass", true);
            }
        };
    }
}
