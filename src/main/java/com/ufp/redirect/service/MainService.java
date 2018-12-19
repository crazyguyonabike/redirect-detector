package com.ufp.redirect.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ufp.domainentry.domain.DomainEntry;
import com.ufp.redirect.repository.ExtendedDomainEntryRepository;

import java.util.List;
import java.util.stream.*;
import java.util.function.*;
import java.util.concurrent.CompletableFuture;

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

    @Transactional
    public void run() {
        long count = 0;
        do {
            count = this.extendedDomainEntryRepository.findFirst250ByWorkingNull().
                map(domainEntry -> CompletableFuture.supplyAsync(() -> blacklistService.getRedirects(domainEntry), executor).thenAccept(extendedDomainEntryRepository::save)).
                map(CompletableFuture::join).
                count();
            logger.debug(String.format("Got count of %d", count));
        } while (count == 250);
    }
}
