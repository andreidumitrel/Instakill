package com.instakill.feed.application;

import com.instakill.feed.domain.FeedRankingStrategy;
import com.instakill.feed.domain.FeedSort;
import com.instakill.post.domain.Post;
import com.instakill.post.domain.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final PostRepository posts;
    private final Map<FeedSort, FeedRankingStrategy> strategies;

    public FeedService(PostRepository posts, List<FeedRankingStrategy> strategyList) {
        this.posts = posts;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FeedRankingStrategy::sort, Function.identity()));
    }

    public List<Post> listFeed(FeedSort sort, int page, int size) {
        if (sort == null) {
            sort = FeedSort.NEW;
        }
        
        FeedRankingStrategy strategy = strategies.get(sort);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported feed sort: " + sort);
        }
        
        return strategy.fetch(posts, page, size);
    }
}
