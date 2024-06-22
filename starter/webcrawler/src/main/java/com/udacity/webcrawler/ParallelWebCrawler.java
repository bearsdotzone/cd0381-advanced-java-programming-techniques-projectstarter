package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
    private final Clock clock;
    private final Duration timeout;
    private final int popularWordCount;
    private final ForkJoinPool pool;
    @Inject
    private PageParserFactory pageParserFactory;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    @Inject
    ParallelWebCrawler(
            Clock clock,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @TargetParallelism int threadCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls) {
        this.clock = clock;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant()
                                .plus(timeout);
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        Set<String> visitedUrls = new ConcurrentSkipListSet<>();

        CrawlTaskBuilder crawlTaskBuilder = new CrawlTaskBuilder().setClock(clock)
                                                                  .setCounts(counts)
                                                                  .setDeadline(deadline)
                                                                  .setIgnoredUrls(ignoredUrls)
                                                                  .setVisitedUrls(visitedUrls)
                                                                  .setMaxDepth(maxDepth)
                                                                  .setPageParserFactory(pageParserFactory);

        crawlTaskBuilder = crawlTaskBuilder.setCrawlTaskBuilder(crawlTaskBuilder);
        CrawlTaskBuilder finalCrawlTaskBuilder = crawlTaskBuilder;

        pool.submit(() -> {
            List<CrawlTask> crawlTasks = new ArrayList<>();
            for (String url : startingUrls) {
                crawlTasks.add(new CrawlTaskBuilder((finalCrawlTaskBuilder)).setUrl(url)
                                                                            .createCrawlTask());
            }
            ForkJoinTask.invokeAll(crawlTasks.toArray(new CrawlTask[0]));
        });

        pool.awaitQuiescence(timeout.toSecondsPart(), TimeUnit.SECONDS);

        if (counts.isEmpty()) {
            return new CrawlResult.Builder()
                    .setWordCounts(counts)
                    .setUrlsVisited(visitedUrls.size())
                    .build();
        }

        return new CrawlResult.Builder()
                .setWordCounts(WordCounts.sort(counts, popularWordCount))
                .setUrlsVisited(visitedUrls.size())
                .build();

    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime()
                      .availableProcessors();
    }

}
