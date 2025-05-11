package br.com.calmaja.controller;

import br.com.calmaja.dto.CreatePostRequest;
import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.UserRepository;
import br.com.calmaja.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));
        Post post = new Post();
        post.setCreatedBy(user);
        post.setContent(request.content());
        post.setTitle(request.title());
        Post post1 = postService.createPost(post, user);
        return ResponseEntity.status(201).body(post1);
    }
}
