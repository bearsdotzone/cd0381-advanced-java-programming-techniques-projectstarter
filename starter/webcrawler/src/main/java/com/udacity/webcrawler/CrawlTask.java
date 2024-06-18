package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class CrawlTask extends RecursiveAction {
    private final String url;
    private final Instant deadline;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    private final Clock clock;
    private final PageParserFactory pageParserFactory;
    private final CrawlTaskBuilder crawlTaskBuilder;

    CrawlTask(Clock clock, PageParserFactory pageParserFactory, String url, Instant deadline, Map<String, Integer> counts, Set<String> visitedUrls, int maxDepth, List<Pattern> ignoredUrls, CrawlTaskBuilder crawlTaskBuilder) {
        this.url = url;
        this.deadline = deadline;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.clock = clock;
        this.pageParserFactory = pageParserFactory;
        this.crawlTaskBuilder = crawlTaskBuilder;
    }

    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant()
                                  .isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url)
                       .matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }

        synchronized (visitedUrls) {
            if (visitedUrls.contains(url)) {
                return;
            }
            visitedUrls.add(url);
        }

        PageParser.Result result = pageParserFactory.get(url)
                                                    .parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts()
                                                  .entrySet()) {
            counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : v + e.getValue());
        }

        for (String link : result.getLinks()) {
            invokeAll(new CrawlTaskBuilder(crawlTaskBuilder).setUrl(link)
                                                            .setMaxDepth(maxDepth - 1)
                                                            .createCrawlTask());
        }
    }
}
