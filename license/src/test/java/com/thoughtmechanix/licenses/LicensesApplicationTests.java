package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.domain.License;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LicensesApplicationTests {
    @Autowired
    LicenseRepository licenseRepository;
    @Test
    void contextLoads() {

    }

}
