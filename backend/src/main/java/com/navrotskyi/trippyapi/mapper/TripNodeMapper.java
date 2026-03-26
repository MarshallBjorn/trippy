package com.navrotskyi.trippyapi.mapper;

import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.dto.TripNodeDto;

public class TripNodeMapper {

    public static TripNodeDto toDto(TripNode node) {
        TripNodeDto dto = new TripNodeDto();
        dto.setId(node.getId());
        dto.setEventId(node.getEvent().getId());
        dto.setReporterId(node.getReporter().getId());
        dto.setReporterName(node.getReporter().getName());
        dto.setStartTime(node.getStartTime());
        dto.setEndTime(node.getEndTime());
        dto.setName(node.getName());
        dto.setNote(node.getNote());
        dto.setPrice(node.getPrice());
        dto.setSeparate(node.isSeparate());
        return dto;
    }
}