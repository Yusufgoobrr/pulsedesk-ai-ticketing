package org.pulsedesk.comment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentAnalysisService commentAnalysisService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService underTest;


    @Test
    void getAllComments() {
        // given
        Comment comment = new Comment("Hello", CommentSourceChannel.APP_REVIEW);
        comment.setId(1L);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(commentRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<CommentResponse> result = underTest.getAllComments(pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().getFirst().comment());
    }

    @Test
    void createNewComment() {
        // given
        NewCommentRequest request = new NewCommentRequest("Bug here", CommentSourceChannel.OTHER);

        when(commentRepository.save(any())).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(5L);
            return c;
        });

        // when
        Long id = underTest.createNewComment(request);

        // then
        assertThat(id).isEqualTo(5L);
        verify(commentRepository).save(any(Comment.class));
        verify(commentAnalysisService).analyzeAndCreateTicket(any(Comment.class));
    }
}
