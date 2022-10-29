package com.dev.springboot.webflux.client.app.handler;

import com.dev.springboot.webflux.client.app.models.Product;
import com.dev.springboot.webflux.client.app.service.ProductService;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    private final ProductService service;

    public ProductHandler(ProductService service) {
        this.service = service;
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(service.findAll(), Product.class);
    }

    public Mono<ServerResponse> detail(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
            service.findById(id)
                .flatMap(p -> ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono
            .flatMap(product -> {
                if (product.getCreateAt() == null) {
                    product.setCreateAt(new Date());
                }
                return service.saveProduct(product);
            })
            .flatMap(p -> ServerResponse
                .created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(p))
            .onErrorResume(error -> {
                WebClientResponseException errorResponse = (WebClientResponseException) error;
                if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(errorResponse.getResponseBodyAsByteArray());
                }
                return Mono.error(errorResponse);
            });
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");
        return errorHandler(
            productMono
                .flatMap(p -> service.editProduct(id, p))
                .flatMap(product -> ServerResponse
                    .created(URI.create("/api/client/".concat(id)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(product)
                )
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
            service
                .deleteProduct(id)
                .then(ServerResponse.noContent().build())
        );
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");

        return errorHandler(request
            .multipartData()
            .map(multipart -> multipart
                .toSingleValueMap()
                .get("file")
            )
            .cast(FilePart.class)
            .flatMap(file -> service
                .upload(file, id)
            )
            .flatMap(p -> ServerResponse
                .created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(p)
            ));
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No exist product: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());

                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
