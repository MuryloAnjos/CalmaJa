package br.com.calmaja.service;

import br.com.calmaja.dto.*;
import br.com.calmaja.model.Comment;
import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.CommentRepository;
import br.com.calmaja.repository.PostRepository;
import br.com.calmaja.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public PostResponse createPost(CreatePostRequest request, User user){
        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCreatedBy(user);
        post = postRepository.save(post);
        return mapToPostResponse(post);
    }

    public List<PostResponse> getPostsByUser(UUID userId){
        User user = userRepository.findByIdWithFetch(userId)
                .orElseThrow(() -> new RuntimeException("User not Found !"));

        List<Post> posts = postRepository.findByCreatedByInWithFetch(List.of(user));

        return posts.stream().map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getPosts(String content, String search, boolean isVerified, boolean onlyfollowing, User user){

        List<Post> posts;

        if(onlyfollowing){
            User userWithFollowing = userRepository.findByUsernameWithFollowing(user.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));
            List<User> following = new ArrayList<>(userWithFollowing.getFollowing());
            posts = postRepository.findByCreatedByInWithFetch(following);
        }else{
            posts = postRepository.findAllWithFetch();
        }

        if(content != null && !content.isEmpty()){
            posts = postRepository.findByContent(content);
        }

        if(isVerified){
            posts = postRepository.findByIsVerified(isVerified);
        }

        if(search != null && !search.isEmpty()){
            posts = posts.stream()
                    .filter(post -> post.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                            post.getContent().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        System.out.println("Posts Encontrados" + posts.size());
        return posts.stream().map(this::mapToPostResponse)
                .collect(Collectors.toList());

    }
    public PostResponse updatePost(Long id, CreatePostRequest request, User user){
        Post post = postRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new RuntimeException("Post not Found !"));

        if(!post.getCreatedBy().getId().equals(user.getId())){
            throw new RuntimeException("This post does not belong to the authenticated user");
        }

        post.setTitle(request.title() != null ? request.title() : post.getTitle() );
        post.setContent(request.title() != null ? request.content() : post.getContent() );

        post = postRepository.save(post);
        return mapToPostResponse(post);

    }

    public void deletePost(Long id, User user){
        Post post = postRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new RuntimeException("Post not Found !"));

        if(!post.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("This post does not belong to the authenticated user");
        }
        postRepository.delete(post);
    }

    public CommentResponse addComment(Long idPost, AddCommentRequest request, User user){
        Post post = postRepository.findByIdWithFetch(idPost)
                .orElseThrow(() -> new RuntimeException("Post not Found !"));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                new UserResponse(comment.getUser().getId(), comment.getUser().getUsername(), comment.getUser().getEmail())
        );

    }

    public PostResponse verifyPost(Long idPost, User user){
        Post post = postRepository.findByIdWithFetch(idPost)
                .orElseThrow(() -> new RuntimeException("Post not Found !"));

        post.setIsVerified(true);
        postRepository.save(post);
        return mapToPostResponse(post);
    }



    private PostResponse mapToPostResponse(Post post){
        List<CommentResponse> commentResponses = post.getComments() != null ?
                post.getComments().stream()
                        .map(comment -> new CommentResponse(
                                comment.getId(),
                                comment.getContent(),
                                comment.getCreatedAt(),
                                new UserResponse(comment.getUser().getId(), comment.getUser().getUsername(), comment.getUser().getEmail())
                        )).collect(Collectors.toList()) : List.of();


        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUpvotes(),
                post.getCreatedAt(),
                new UserResponse(post.getCreatedBy().getId(), post.getCreatedBy().getUsername(), post.getCreatedBy().getEmail()),
                commentResponses,
                post.isVerified()
        );

    }
}
