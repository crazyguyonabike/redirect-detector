package com.ufp.redirect.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ufp.domainentry.domain.DomainEntry;
import com.ufp.redirect.repository.ExtendedDomainEntryRepository;

import java.util.List;
import java.util.stream.*;
import java.util.function.*;
import java.util.concurrent.CompletableFuture;

import java.time.LocalDateTime;

import org.apache.log4j.Logger;

@Service
public class MainService {
    private static Logger logger = Logger.getLogger(MainService.class);

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private ExtendedDomainEntryRepository extendedDomainEntryRepository;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    public Long run() {
        long count = 0, totalCount = 0;

        do {
            Iterable<DomainEntry> domainEntries = getDomainEntryList();
            count = StreamSupport.stream(domainEntries.spliterator(), false).
                map(domainEntry -> CompletableFuture.supplyAsync(() -> blacklistService.getRedirects(domainEntry), executor).thenAccept(extendedDomainEntryRepository::save)).
                map(CompletableFuture::join).
                count();
            totalCount += count;
        } while (count == 250);

        return totalCount;
    }

    public Iterable<DomainEntry> getDomainEntryList() {
        List<DomainEntry> domainEntryList = this.extendedDomainEntryRepository.findFirst250ByWorkingNull();
        LocalDateTime now = LocalDateTime.now();
        domainEntryList.forEach(d -> {
                    d.setWorking(now);
                });
        return extendedDomainEntryRepository.saveAll(domainEntryList);
    }
}
