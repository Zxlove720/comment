package com.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class CommentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentServerApplication.class, args);
    }

}
