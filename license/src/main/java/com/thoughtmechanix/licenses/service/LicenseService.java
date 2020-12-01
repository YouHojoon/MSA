package com.thoughtmechanix.licenses.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.client.OrganizationDiscoveryClient;
import com.thoughtmechanix.licenses.client.OrganizationFeginClient;
import com.thoughtmechanix.licenses.client.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.domain.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import com.thoughtmechanix.licenses.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseService {
    private final LicenseRepository licenseRepository;
    private final OrganizationRestTemplateClient organizationRestTemplateClient;
    private final OrganizationFeginClient organizationFeginClient;
    private final OrganizationDiscoveryClient organizationDiscoveryClient;

    public License getLicense(String organizationId, String licenseId, String clientType) {
        License license = licenseRepository.findByOrOrganizationIdAndLicenseId(organizationId, licenseId);
        Organization organization = retrieveOrgInfo(organizationId, clientType);
        license.setOrganizationName(organization.getName());
        license.setContactEmail(organization.getContactEmail());
        license.setContactPhone(organization.getContactPhone());
        license.setContactName(organization.getContactName());
        return license;
    }

    @HystrixCommand(commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "12000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),//10초 시간대 동안 연속 호출 횟수
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),//차단 후 회복 상태를 확인할 때까지 대기할 시간
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "30"),//차단하는 데 필요한 실패 비율
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),//서비스 호출 문제를 모니터할 시간 간격
            @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5")},//통계를 수집할 횟수 여기서는 3초길이의 버킷 5개
            threadPoolKey = "licenseListByOrganizationIdThreadPool",//스레드 풀 이름
            threadPoolProperties = {@HystrixProperty(name = "coreSize", value = "30"),//스레드풀 스레드 개수
                    @HystrixProperty(name = "maxQueueSize", value = "10")},//스래드풀 앞 단 큐
            fallbackMethod = "fallbackLicenseList")
    public List<License> getLicenseListByOrganizationId(String organizationId) {
        return licenseRepository.findByOrganizationId(organizationId);
    }

    public void saveLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
    }

    private Organization retrieveOrgInfo(String organizationId, String clientType) {

        Organization organization = null;
        OrganizationRestTemplateClient restTemplateClient = new OrganizationRestTemplateClient();
        switch (clientType) {
            case "fegin":
                /*
                    안되는데 이유를 모르겠음
                 */
                System.out.println("I am using the fegin client");
                organization = organizationFeginClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestTemplateClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestTemplateClient.getOrganization(organizationId);
        }
        return organization;
    }

    private List<License> fallbackLicenseList(String organizationId) {
        List<License> fallbackList = new ArrayList<>();
        License license = new License().builder().licenseId("000000-00-00000").organizationId(organizationId)
                .productName("license 정보가 없습니다.").build();
        fallbackList.add(license);
        return fallbackList;
    }

    private void randomlyRunLong() {
        Random random = new Random();
        int randomNum = random.nextInt(3) + 1;
        if (randomNum == 3) sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(13000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
