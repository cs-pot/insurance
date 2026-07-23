package com.cspot.insurahub.claim.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "attachment.storage")
public class ReceiptStorageProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(10);
}
