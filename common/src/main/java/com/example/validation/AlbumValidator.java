package com.example.validation;

import com.example.dto.SaveAlbumDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AlbumValidator implements ConstraintValidator<ValidAlbum, SaveAlbumDto> {
    @Override
    public boolean isValid(SaveAlbumDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean hasArtist = dto.getArtistId() != null;
        boolean hasBand = dto.getBandId() != null;

        return hasArtist ^ hasBand;
    }
}