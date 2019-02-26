package com.ufp.redirect.repository;

import com.ufp.domainentry.domain.DomainEntry;
import com.ufp.domainentry.repository.DomainEntryRepository;

import java.util.List;

public interface ExtendedDomainEntryRepository extends DomainEntryRepository {
    List<DomainEntry> findFirst250ByWorkingNull();
}


