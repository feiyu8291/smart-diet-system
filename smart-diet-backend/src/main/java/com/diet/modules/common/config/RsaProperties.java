package com.diet.modules.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RSA 密钥配置属性类
 *
 * @author FeiYu
 * @date 2026-06-20
 **/
@Data
@Component
public class RsaProperties {

    private String privateKey;

    @Value("${rsa.private_key}")
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
