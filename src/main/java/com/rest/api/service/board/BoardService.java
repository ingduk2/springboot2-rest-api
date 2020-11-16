package com.rest.api.service.board;

import com.rest.api.advice.exception.CForbiddenWordException;
import com.rest.api.advice.exception.CNotOwnerException;
import com.rest.api.advice.exception.CResourceNotExistException;
import com.rest.api.annotation.ForbiddenWordCheck;
import com.rest.api.common.CacheKey;
import com.rest.api.entity.User;
import com.rest.api.entity.board.Board;
import com.rest.api.entity.board.Post;
import com.rest.api.model.board.ParamPost;
import com.rest.api.repo.BoardJpaRepo;
import com.rest.api.repo.PostJpaRepo;
import com.rest.api.repo.UserJpaRepo;
import com.rest.api.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final UserJpaRepo userJpaRepo;
    private final BoardJpaRepo boardJpaRepo;
    private final PostJpaRepo postJpaRepo;
    private final CacheService cacheService;

    public Board insertBoard(String boardName) {
        return boardJpaRepo.save(new Board(boardName));
    }

    @Cacheable(value = CacheKey.BOARD, key = "#boardName", unless = "#result == null")
    public Board findBoard(String boardName) {
        return Optional.ofNullable(boardJpaRepo.findByName(boardName)).orElseThrow(CResourceNotExistException::new);
    }

    @Cacheable(value = CacheKey.POSTS, key = "#boardName", unless = "#result == null")
    public List<Post> findPosts(String boardName) {
        return postJpaRepo.findByBoard(findBoard(boardName));
    }

    @Cacheable(value = CacheKey.POST, key = "#postId", unless = "#result == null")
    public Post getPost(long postId) {
        return postJpaRepo.findById(postId).orElseThrow(CResourceNotExistException::new);
    }

    @ForbiddenWordCheck
    @CacheEvict(value = CacheKey.POSTS, key = "#boardName")
    public Post writePost(String uid, String boardName, ParamPost paramPost) {
        Board board = findBoard(boardName);
        Post post = new Post(userJpaRepo.findByUid(uid).orElseThrow(CResourceNotExistException::new)
                            , board
                            , paramPost.getAuthor()
                            , paramPost.getTitle()
                            , paramPost.getContent());
//        checkForbiddenWord(paramPost.getContent());
        return postJpaRepo.save(post);
    }

//    @CachePut(value = CacheKey.POST, key = "#postId")
    @ForbiddenWordCheck
    public Post updatePost(long postId, String uid, ParamPost paramPost) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid())) {
            throw new CNotOwnerException();
        }

//        checkForbiddenWord(paramPost.getContent());

        // dirty checking
        post.setUpdate(paramPost.getAuthor(), paramPost.getTitle(), paramPost.getContent());
        cacheService.deleteBoardCache(post.getPostId(), post.getBoard().getName());
        return post;
    }

    public boolean deletePost(long postId, String uid) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid())) {
            throw new CNotOwnerException();
        }
        postJpaRepo.delete(post);
        cacheService.deleteBoardCache(post.getPostId(), post.getBoard().getName());
        return true;
    }

    public void checkForbiddenWord(String word) {
        List<String> forbiddenWords = Arrays.asList("개새끼", "썅년", "시발", "씨발");
        Optional<String> forbiddenWord = forbiddenWords.stream().filter(word::contains).findFirst();
        if (forbiddenWord.isPresent()) {
            throw new CForbiddenWordException(forbiddenWord.get());
        }
    }
}
