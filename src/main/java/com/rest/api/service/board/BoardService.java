package com.rest.api.service.board;

import com.rest.api.advice.exception.CNotOwnerException;
import com.rest.api.advice.exception.CResourceNotExistException;
import com.rest.api.entity.User;
import com.rest.api.entity.board.Board;
import com.rest.api.entity.board.Post;
import com.rest.api.model.board.ParamPost;
import com.rest.api.repo.BoardJpaRepo;
import com.rest.api.repo.PostJpaRepo;
import com.rest.api.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final UserJpaRepo userJpaRepo;
    private final BoardJpaRepo boardJpaRepo;
    private final PostJpaRepo postJpaRepo;

    public Board insertBoard(String boardName) {
        return boardJpaRepo.save(new Board(boardName));
    }

    public Board findBoard(String boardName) {
        return Optional.ofNullable(boardJpaRepo.findByName(boardName)).orElseThrow(CResourceNotExistException::new);
    }

    public List<Post> findPosts(String boardName) {
        return postJpaRepo.findByBoard(findBoard(boardName));
    }

    public Post getPost(long postId) {
        return postJpaRepo.findById(postId).orElseThrow(CResourceNotExistException::new);
    }

    public Post writePost(String uid, String boardName, ParamPost paramPost) {
        Board board = findBoard(boardName);
        Post post = new Post(userJpaRepo.findByUid(uid).orElseThrow(CResourceNotExistException::new)
                            , board
                            , paramPost.getAuthor()
                            , paramPost.getTitle()
                            , paramPost.getContent());
        return postJpaRepo.save(post);
    }

    public Post updatePost(long postId, String uid, ParamPost paramPost) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid())) {
            throw new CNotOwnerException();
        }
        // dirty checking
        post.setUpdate(paramPost.getAuthor(), paramPost.getTitle(), paramPost.getContent());
        return post;
    }

    public boolean deletePost(long postId, String uid) {
        Post post = getPost(postId);
        User user = post.getUser();
        if (!uid.equals(user.getUid())) {
            throw new CNotOwnerException();
        }
        postJpaRepo.delete(post);
        return true;
    }
}