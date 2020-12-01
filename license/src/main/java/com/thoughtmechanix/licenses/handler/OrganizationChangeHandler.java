package com.thoughtmechanix.licenses.handler;

import com.thoughtmechanix.licenses.channel.CustomChannels;
import com.thoughtmechanix.licenses.model.OrganizationChangeModel;
import com.thoughtmechanix.licenses.repository.OrganizationRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CustomChannels.class)
@Slf4j
public class OrganizationChangeHandler {
    @Autowired
    private OrganizationRedisRepository redisRepository;

    @StreamListener("inboundOrgChanges")
    public void loggerSink(OrganizationChangeModel organizationChangeModel){
        switch (organizationChangeModel.getAction()){
            case "UPDATE":
                log.info("Received a UPDATE event from the organization service for organization id {}",
                        organizationChangeModel.getOrganizationId());
                redisRepository.deleteOrganization(organizationChangeModel.getOrganizationId());
                break;
            case "DELETE":
                log.info("Received a DELETE event from the organization service for organization id {}",
                        organizationChangeModel.getOrganizationId());
                redisRepository.deleteOrganization(organizationChangeModel.getOrganizationId());
                break;
            default:
                log.info("Received an UNKNOWN event from the organization service of type {}", organizationChangeModel.getType());
                break;
        }
    }
}
