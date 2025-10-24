package com.josedjaykv.products_service.controllers;

import com.josedjaykv.products_service.model.dtos.MedianoRequest;
import com.josedjaykv.products_service.model.dtos.MedianoResponse;
import com.josedjaykv.products_service.services.MedianoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mediano")
@RequiredArgsConstructor
public class MedianosController {

    private final MedianoService medianoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addMediano(@RequestBody MedianoRequest medianoRequest){
        this.medianoService.addMediano(medianoRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MedianoResponse> getAllMedianos(){
        return this.medianoService.getAllMedianos();
    }
}
