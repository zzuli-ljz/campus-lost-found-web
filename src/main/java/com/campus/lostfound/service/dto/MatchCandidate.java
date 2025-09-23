package com.campus.lostfound.service.dto;

import com.campus.lostfound.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCandidate {
    private Item candidate;
    private double score; // 0.0 ~ 1.0
    private List<String> overlapReasons; // 例如："地点相同"、"类别相同"、"时间接近"
}
