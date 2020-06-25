package com.aprendendotddspring.aprendendo.config;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



public class Beans {

    ModelMapper modelMapper;

    @Bean
    public ModelMapper modelMapper(){
        return this.modelMapper = new ModelMapper();
    }


}
