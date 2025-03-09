package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.PostNotFoundException;
import ru.yandex.practicum.catsgram.exception.UserNotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final AtomicLong postIdGenerator = new AtomicLong(0);
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAllPosts(int from, int size, String sort) {
        SortOrder sortOrder = SortOrder.from(sort);

        List<Post> postList = posts.values().stream()
                .sorted(Comparator.comparing(Post::getPostDate))
                .collect(Collectors.toList());

        if (sortOrder == SortOrder.DESCENDING) {
            Collections.reverse(postList);
        }

        if (from >= postList.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(from + size, postList.size());
        return postList.subList(from, toIndex);
    }

    public Optional<Post> findPostById(Long postId) {
        return Optional.ofNullable(posts.get(postId));
    }


    public Post createPost(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        if (post.getAuthorId() == null) {
            throw new ConditionsNotMetException("Автор не указан");
        }
        userService.findUserById(post.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("Автор с id = " + post.getAuthorId() + " не найден"));
        post.setId(postIdGenerator.incrementAndGet());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post updatePost(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!posts.containsKey(newPost.getId())) {
            throw new PostNotFoundException("Пост с id = " + newPost.getId() + " не найден.");
        }
        Post oldPost = posts.get(newPost.getId());
        if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        oldPost.setDescription(newPost.getDescription());
        return oldPost;
    }
}

