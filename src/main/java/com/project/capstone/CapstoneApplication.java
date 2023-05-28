package com.project.capstone;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.Collections;


@SpringBootApplication
public class CapstoneApplication {
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		ApplicationContext context = new SpringApplicationBuilder(CapstoneApplication.class)
				.properties("spring.config.name:application,opencv") // opencv.properties 파일 로드
				.build()
				.run(args);
	}
}
