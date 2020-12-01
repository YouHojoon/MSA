package com.thoughmechanix.organization.source;

import com.thoughmechanix.organization.channel.CustomChannels;
import com.thoughmechanix.organization.domain.Organization;
import com.thoughmechanix.organization.model.OrganizationChangeModel;
import com.thoughmechanix.organization.util.UserContext;
import com.thoughmechanix.organization.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SimpleSourceBean {
    private final CustomChannels custom;

    public void publishOrgChange(String action, String orgId){
        log.info("Sending Kafka message {} for Organization Id : {}",action,orgId);
        OrganizationChangeModel organizationChangeModel = new OrganizationChangeModel(
                Organization.class.getTypeName(), action, orgId, UserContextHolder.getContext().getCorrelationId()
        );
        custom.output().send(MessageBuilder.withPayload(organizationChangeModel).build());
    }
}
