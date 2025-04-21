package com.ramonlimas.service.insights;

import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;

public interface Insight {
    InsightType getType();
    InsightResultDTO process(String userId, boolean includeTitles);
}