package com.task.backend;

import org.mybatis.spring.annotation.MapperScan;                        // MyBatis 的 Mapper 扫描注解
import org.springframework.boot.SpringApplication;                      // Spring Boot 应用启动器
import org.springframework.boot.autoconfigure.SpringBootApplication;    // Spring Boot 核心注解

@SpringBootApplication
@MapperScan("com.task.backend.mapper")  // 添加这行
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
