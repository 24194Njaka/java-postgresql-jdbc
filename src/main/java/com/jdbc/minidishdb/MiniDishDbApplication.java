package com.jdbc.minidishdb;

import com.jdbc.minidishdb.repository.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MiniDishDbApplication {

    public static void main(String[] args) {
        DataSource dataSource = new DataSource();
  

        SpringApplication.run(MiniDishDbApplication.class, args);
    }

    }


