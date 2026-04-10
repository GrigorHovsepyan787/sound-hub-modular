package com.example.projection;

import com.example.model.Band;

public interface BandPopularity {
    Band getBand();

    Long getTotalPlays();
}
