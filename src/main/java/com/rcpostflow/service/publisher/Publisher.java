package com.rcpostflow.service.publisher;

public interface Publisher {

    PublishChannel channel();

    PublishResult publish(PublishRequest request);
}
