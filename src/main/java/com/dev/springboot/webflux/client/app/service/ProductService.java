package com.dev.springboot.webflux.client.app.service;

import com.dev.springboot.webflux.client.app.models.Product;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<Product> findAll();

    Mono<Product> findById(String id);

    Mono<Product> saveProduct(Product product);

    Mono<Product> editProduct(String id, Product product);

    Mono<Void> deleteProduct(String id);

    Mono<Product> upload(FilePart file, String id);

}
