package br.com.calmaja.service;

import br.com.calmaja.dto.CreatePostRequest;
import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(Post post, User user){

        return postRepository.save(post);
    }
}
