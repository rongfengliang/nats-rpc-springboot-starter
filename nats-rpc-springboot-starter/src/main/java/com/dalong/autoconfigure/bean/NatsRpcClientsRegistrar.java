package com.dalong.autoconfigure.bean;

import com.dalong.client.RpcClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;

public class NatsRpcClientsRegistrar implements ImportBeanDefinitionRegistrar {

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableNatsRpcClients.class.getName());
        String[] basePackages = (String[]) attributes.get("basePackages");

        if (basePackages.length == 0) {
            basePackages = new String[]{importingClassMetadata.getClassName().substring(0,
                    importingClassMetadata.getClassName().lastIndexOf("."))};
        }

        ClassPathScanningCandidateComponentProvider  scanner = getScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcClient.class,true));

        for (String basePackage : basePackages) {
            scanner.findCandidateComponents(basePackage).forEach(beanDef -> {
                try {
                    Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                    BeanDefinitionBuilder builder =
                            BeanDefinitionBuilder.genericBeanDefinition(NatsRpcClientFactoryBean.class);
                    builder.addConstructorArgValue(clazz);
                    builder.addConstructorArgReference("connection");
                    builder.addConstructorArgReference("objectMapper");
                    String beanName = clazz.getSimpleName();
                    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
