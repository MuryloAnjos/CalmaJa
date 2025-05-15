package br.com.calmaja.controller;

import br.com.calmaja.dto.AddCommentRequest;
import br.com.calmaja.dto.CommentResponse;
import br.com.calmaja.dto.CreatePostRequest;
import br.com.calmaja.dto.PostResponse;
import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.PostRepository;
import br.com.calmaja.repository.UserRepository;
import br.com.calmaja.service.PostService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));
        PostResponse post = postService.createPost(request, user);
        return ResponseEntity.status(201).body(post);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPosts(@RequestParam(required = false) String content,
                                               @RequestParam(required = false) String search,
                                               @RequestParam(required = false) boolean isVerified,
                                               @RequestParam(required = false, defaultValue = "false") boolean onlyFollowing,
                                               Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found !"));
        List<PostResponse> posts = postService.getPosts(content, search, isVerified, onlyFollowing, user);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody CreatePostRequest request, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));
        PostResponse updatedPost = postService.updatePost(id, request, user);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));

        postService.deletePost(id, user);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId,
                                                      @RequestBody AddCommentRequest request,
                                                      Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));

        postService.addComment(postId, request, user);
        return ResponseEntity.status(201).build();
    }
}
