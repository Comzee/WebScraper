package com.example.scraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;

    public List<Album> searchAlbums(String keyword) {
        return albumRepository.findByNameContainingIgnoreCase(keyword);
    }

    public void saveAlbums(List<Album> albums) {
        albumRepository.saveAll(albums);
    }
}
