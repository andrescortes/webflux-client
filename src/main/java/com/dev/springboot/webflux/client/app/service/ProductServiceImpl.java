package com.dev.springboot.webflux.client.app.service;

import com.dev.springboot.webflux.client.app.models.Product;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements
    ProductService {

    private final WebClient.Builder client;

    public ProductServiceImpl(Builder client) {
        this.client = client;
    }


    @Override
    public Flux<Product> findAll() {
        return client.build().get().accept(MediaType.APPLICATION_JSON)
            .exchangeToFlux(response -> response
                .bodyToFlux(Product.class));
        //.flatMapMany(response-> response.bodyToFlux(Product.class));
    }

    @Override
    public Mono<Product> findById(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return client.build().get().uri("/{id}", params)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Product.class);
        //.exchangeToMono(clientResponse -> clientResponse.bodyToMono(Product.class));
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        return client.build().post()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            //.body(BodyInserters.fromValue(product))
            .body(Mono.just(product), Product.class)
            .retrieve()
            .bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> editProduct(String id, Product product) {
        return client.build().put()
            .uri("/{id}", Collections.singletonMap("id", id))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            //.body(BodyInserters.fromValue(product))
            .body(Mono.just(product), Product.class)
            .retrieve()
            .bodyToMono(Product.class);
    }

    @Override
    public Mono<Void> deleteProduct(String id) {
        return client.build()
            .delete()
            .uri("/{id}", Collections.singletonMap("id", id))
            .retrieve()
            .bodyToMono(Void.class);
    }

    @Override
    public Mono<Product> upload(FilePart file, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
            h.setContentDispositionFormData("file", file.filename());
        });

        return client
            .build()
            .post()
            .uri("/upload/{id}", Collections.singletonMap("id", id))
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(parts.build())
            .retrieve()
            .bodyToMono(Product.class);
    }


}
