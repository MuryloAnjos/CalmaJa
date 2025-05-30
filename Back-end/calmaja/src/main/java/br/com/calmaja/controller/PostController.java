package br.com.calmaja.controller;

import br.com.calmaja.dto.AddCommentRequest;
import br.com.calmaja.dto.CommentResponse;
import br.com.calmaja.dto.CreatePostRequest;
import br.com.calmaja.dto.PostResponse;
import br.com.calmaja.model.Post;
import br.com.calmaja.model.Role;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.PostRepository;
import br.com.calmaja.repository.RoleRepository;
import br.com.calmaja.repository.UserRepository;
import br.com.calmaja.service.PostService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));
        PostResponse post = postService.createPost(request, user);
        return ResponseEntity.status(201).body(post);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
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


    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<List<PostResponse>> getPostsByUser(@PathVariable UUID userId){
        List<PostResponse> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<List<PostResponse>> getMyPosts(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User user = userRepository.findByUsernameWithFollowing(username)
                .orElseThrow(() -> new RuntimeException("User not found !"));

        List<PostResponse> posts = postService.getPostsByUser(user.getId());
        return ResponseEntity.ok(posts);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody CreatePostRequest request, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));
        PostResponse updatedPost = postService.updatePost(id, request, user);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));

        postService.deletePost(id, user);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("{postId}/comments")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
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

    @PutMapping("{postId}/verify")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<PostResponse> verifyPost(@PathVariable Long postId, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found !"));

        PostResponse postResponse = postService.verifyPost(postId, user);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("{postId}/upvote")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<Void> addUpvote(@PathVariable Long postId){
        postService.addUpvote(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{postId}/complaints")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Void> addComplaint(@PathVariable Long postId){
        postService.addComplaints(postId);
        return ResponseEntity.ok().build();
    }


}
