package com.ufp.redirect.repository;

import com.ufp.domainentry.domain.DomainEntry;
import com.ufp.domainentry.repository.DomainEntryRepository;

import java.util.stream.Stream;

public interface ExtendedDomainEntryRepository extends DomainEntryRepository {
    Stream<DomainEntry> findFirst250ByWorkingNull();
}


