package com.instakill.feed.application;

import com.instakill.feed.domain.FeedRankingStrategy;
import com.instakill.feed.domain.FeedSort;
import com.instakill.post.domain.Post;
import com.instakill.post.domain.PostRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedServiceTest {

    private final PostRepository repository = Mockito.mock(PostRepository.class);

    @Test
    void listsFeedUsingStrategy() {
        FeedRankingStrategy strategy = Mockito.mock(FeedRankingStrategy.class);
        when(strategy.sort()).thenReturn(FeedSort.NEW);
        
        List<Post> expectedPosts = List.of(Mockito.mock(Post.class));
        when(strategy.fetch(eq(repository), eq(0), eq(5))).thenReturn(expectedPosts);

        FeedService service = new FeedService(repository, List.of(strategy));

        List<Post> result = service.listFeed(FeedSort.NEW, 0, 5);

        assertThat(result).isEqualTo(expectedPosts);
        verify(strategy).fetch(repository, 0, 5);
    }

    @Test
    void defaultsToNewStrategyWhenSortIsNull() {
        FeedRankingStrategy newStrategy = Mockito.mock(FeedRankingStrategy.class);
        when(newStrategy.sort()).thenReturn(FeedSort.NEW);
        
        List<Post> expectedPosts = List.of(Mockito.mock(Post.class));
        when(newStrategy.fetch(eq(repository), eq(0), eq(5))).thenReturn(expectedPosts);

        FeedService service = new FeedService(repository, List.of(newStrategy));

        List<Post> result = service.listFeed(null, 0, 5);

        assertThat(result).isEqualTo(expectedPosts);
        verify(newStrategy).fetch(repository, 0, 5);
    }

    @Test
    void throwsExceptionWhenStrategyNotFound() {
        FeedRankingStrategy strategy = Mockito.mock(FeedRankingStrategy.class);
        when(strategy.sort()).thenReturn(FeedSort.TRENDING);

        FeedService service = new FeedService(repository, List.of(strategy));

        assertThatThrownBy(() -> service.listFeed(FeedSort.NEW, 0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported feed sort");
    }
}

