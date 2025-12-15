package com.task.backend.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/task_management_system?useSSL=false&serverTimezone=UTC",
                        "root", "301477"
                )
                .globalConfig(builder -> {
                    builder.author("task")
                            .outputDir(System.getProperty("user.dir") + "/src/main/java")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent("com.task.backend")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"
                            ));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user", "sys_task", "sys_operation_record")
                            .entityBuilder().enableLombok()
                            .controllerBuilder().enableRestStyle()
                            // 新增：开启文件覆盖
                            .enableFileOverride();
                })
                .execute();
    }
}