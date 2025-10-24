package com.josedjaykv.products_service.services;

import com.josedjaykv.products_service.model.dtos.MedianoRequest;
import com.josedjaykv.products_service.model.dtos.MedianoResponse;
import com.josedjaykv.products_service.model.entities.Medianos;
import com.josedjaykv.products_service.repositories.MedianosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedianoService {

    private final MedianosRepository medianosRepository;

    public void addMediano(MedianoRequest medianoRequest){
        var mediano = Medianos.builder()
                .sku(medianoRequest.getSku())
                .name(medianoRequest.getName())
                .height(medianoRequest.getHeight())
                .weight(medianoRequest.getWeight())
                .description(medianoRequest.getDescription())
                .price(medianoRequest.getPrice())
                .status(medianoRequest.getStatus())
                .build();

        medianosRepository.save(mediano);

        log.info("Mediano added: {}", mediano);
    }

    public List<MedianoResponse> getAllMedianos(){
        var  medianos = medianosRepository.findAll();

        return medianos.stream().map(this::mapToMedianoResponse).toList();
    }

    private MedianoResponse mapToMedianoResponse(Medianos medianos) {
        return MedianoResponse.builder()
                .id(medianos.getId())
                .sku(medianos.getSku())
                .name(medianos.getName())
                .height(medianos.getHeight())
                .weight(medianos.getWeight())
                .description(medianos.getDescription())
                .price(medianos.getPrice())
                .status(medianos.getStatus())
                .build();
    }

}
