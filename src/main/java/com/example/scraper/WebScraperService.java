package com.example.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

@Service
public class WebScraperService {

    @Value("${BASE_URL}")
    private String baseUrl;

    @Value("${GALLERY_URL}")
    private String galleryBaseUrl;

    private static final int THREAD_POOL_SIZE = 15;

    public List<Album> scrapeAlbums(int startPage, int endPage) throws IOException, ExecutionException, InterruptedException {
        List<Album> albums = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            Document doc = Jsoup.connect(baseUrl + i).get();
            Elements galleryLinks = doc.select("tbody tr td a");

            for (Element link : galleryLinks) {
                String galleryHref = link.attr("href");
                if (!galleryHref.startsWith("http")) {
                    galleryHref = galleryBaseUrl + galleryHref;
                }

                if (!processedLinks.contains(galleryHref)) {
                    processedLinks.add(galleryHref);
                    System.out.println("Original Gallery URL: " + galleryHref);

                    String finalGalleryHref = galleryHref;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            String redirectedUrl = getRedirectedUrl(finalGalleryHref);
                            System.out.println("Redirected Gallery URL: " + redirectedUrl);
                            scrapeGallery(redirectedUrl, albums);
                        } catch (IOException e) {
                            System.err.println("Error fetching redirected URL for: " + finalGalleryHref);
                            e.printStackTrace();
                        }
                    }, executorService);

                    futures.add(future);
                }
            }
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        executorService.shutdown();
        return albums;
    }

    private void scrapeGallery(String url, List<Album> albums) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements fileElements = doc.select(".grid-images_box-txt p:first-child");

        synchronized (albums) {
            for (Element fileElement : fileElements) {
                String fileName = fileElement.text();
                albums.add(new Album(fileName, url));
            }
        }
    }

    private String getRedirectedUrl(String url) throws IOException {
        Connection.Response response = Jsoup.connect(url).followRedirects(true).execute();
        return response.url().toString();
    }
}