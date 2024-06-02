package com.example.scraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {
    @Autowired
    private WebScraperService webScraperService;
    @Autowired
    private AlbumService albumService;

    @GetMapping("/scrape")
    public List<Album> scrapeAlbums(@RequestParam int startPage, @RequestParam int endPage) throws IOException, ExecutionException, InterruptedException {
        List<Album> albums = webScraperService.scrapeAlbums(startPage, endPage);
        albumService.saveAlbums(albums);
        return albums;
    }

    @GetMapping("/search")
    public List<Album> searchAlbums(@RequestParam String keyword) {
        return albumService.searchAlbums(keyword);
    }
}
