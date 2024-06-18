package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CrawlTaskBuilder {
    private Clock clock;
    private PageParserFactory pageParserFactory;
    private String url;
    private Instant deadline;
    private Map<String, Integer> counts;
    private Set<String> visitedUrls;
    private int maxDepth;
    private List<Pattern> ignoredUrls;
    private CrawlTaskBuilder crawlTaskBuilder;

    public CrawlTaskBuilder setCrawlTaskBuilder(CrawlTaskBuilder crawlTaskBuilder) {
        this.crawlTaskBuilder = crawlTaskBuilder;
        return this;
    }

    public CrawlTaskBuilder setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public CrawlTaskBuilder setPageParserFactory(PageParserFactory pageParserFactory) {
        this.pageParserFactory = pageParserFactory;
        return this;
    }

    public CrawlTaskBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public CrawlTaskBuilder setDeadline(Instant deadline) {
        this.deadline = deadline;
        return this;
    }

    public CrawlTaskBuilder setCounts(Map<String, Integer> counts) {
        this.counts = counts;
        return this;
    }

    public CrawlTaskBuilder setVisitedUrls(Set<String> visitedUrls) {
        this.visitedUrls = visitedUrls;
        return this;
    }

    public CrawlTaskBuilder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public CrawlTaskBuilder setIgnoredUrls(List<Pattern> ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        return this;
    }

    public CrawlTask createCrawlTask() {
        return new CrawlTask(clock, pageParserFactory, url, deadline, counts, visitedUrls, maxDepth, ignoredUrls, crawlTaskBuilder);
    }

    public CrawlTaskBuilder() {
        
    }

    public CrawlTaskBuilder(CrawlTaskBuilder crawlTaskBuilder) {
        this.clock = crawlTaskBuilder.clock;
        this.pageParserFactory = crawlTaskBuilder.pageParserFactory;
        this.url = crawlTaskBuilder.url;
        this.deadline = crawlTaskBuilder.deadline;
        this.counts = crawlTaskBuilder.counts;
        this.visitedUrls = crawlTaskBuilder.visitedUrls;
        this.maxDepth = crawlTaskBuilder.maxDepth;
        this.ignoredUrls = crawlTaskBuilder.ignoredUrls;
        this.crawlTaskBuilder = this;
    }
}