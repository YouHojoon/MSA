package com.thoughmechanix.organization.repository;

import com.thoughmechanix.organization.domain.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization, String> {

}
