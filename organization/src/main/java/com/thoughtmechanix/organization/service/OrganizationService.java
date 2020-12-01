package com.thoughmechanix.organization.service;

import brave.Span;
import brave.Tracer;
import com.thoughmechanix.organization.domain.Organization;
import com.thoughmechanix.organization.repository.OrganizationRepository;
import com.thoughmechanix.organization.source.SimpleSourceBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final SimpleSourceBean simpleSourceBean;
    private final Tracer tracer;
    public Organization getOrganization(String organizationId){
        Span newSpan=tracer.nextSpan().name("getOrgDBCall");
        try (Tracer.SpanInScope ws=tracer.withSpanInScope(newSpan.start())){
            Optional<Organization> organization = organizationRepository.findById(organizationId);
            if(!organization.isPresent())
                throw new NullPointerException("organizationId-"+organizationId);
            log.info(organization.get().toString());
            return organization.get();
        }finally {
            newSpan.tag("peer.service","mysql");
            newSpan.annotate("cr");
            newSpan.finish();
        }

    }
    public void saveOrg(Organization org){
        org.setId(UUID.randomUUID().toString());
        organizationRepository.save(org);
        simpleSourceBean.publishOrgChange("SAVE", org.getId());
    }
}
