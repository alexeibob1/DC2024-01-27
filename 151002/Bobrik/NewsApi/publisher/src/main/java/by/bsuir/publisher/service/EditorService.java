package by.bsuir.publisher.service;

import by.bsuir.publisher.dao.EditorRepository;
import by.bsuir.publisher.model.entity.Editor;
import by.bsuir.publisher.model.request.EditorRequestTo;
import by.bsuir.publisher.model.response.EditorResponseTo;
import by.bsuir.publisher.service.exceptions.ResourceNotFoundException;
import by.bsuir.publisher.service.mapper.EditorMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@CacheConfig(cacheNames = "editorsCache")
@RequiredArgsConstructor
public class EditorService implements RestService<EditorRequestTo, EditorResponseTo> {
    private final EditorRepository editorRepository;

    private final EditorMapper editorMapper;

    @Cacheable(cacheNames = "editors")
    @Override
    public List<EditorResponseTo> findAll() {
        return editorMapper.getListResponseTo(editorRepository.findAll());
    }

    @Cacheable(cacheNames = "editors", key = "#id", unless = "#result == null")
    @Override
    public EditorResponseTo findById(Long id) {
        return editorMapper.getResponseTo(editorRepository
                .findById(id)
                .orElseThrow(() -> editorNotFoundException(id)));
    }

    @CacheEvict(cacheNames = "editors", allEntries = true)
    @Override
    public EditorResponseTo create(EditorRequestTo editorTo) {
        return editorMapper.getResponseTo(editorRepository.save(editorMapper.getEditor(editorTo)));
    }

    @CacheEvict(cacheNames = "editors", allEntries = true)
    @Override
    public EditorResponseTo update(EditorRequestTo editorTo) {
        editorRepository
                .findById(editorMapper.getEditor(editorTo).getId())
                .orElseThrow(() -> editorNotFoundException(editorMapper.getEditor(editorTo).getId()));
        return editorMapper.getResponseTo(editorRepository.save(editorMapper.getEditor(editorTo)));
    }

    @Caching(evict = { @CacheEvict(cacheNames = "editors", key = "#id"),
            @CacheEvict(cacheNames = "editors", allEntries = true) })
    @Override
    public void removeById(Long id) {
        Editor editor = editorRepository
                .findById(id)
                .orElseThrow(() -> editorNotFoundException(id));
        editorRepository.delete(editor);
    }

    private static ResourceNotFoundException editorNotFoundException(Long id) {
        return new ResourceNotFoundException("Failed to find editor with id = " + id, HttpStatus.NOT_FOUND.value() * 100 + 23);
    }
}
