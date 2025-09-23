package com.campus.lostfound.service.dto;

import com.campus.lostfound.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserItemCandidates {
    private Item item;
    private List<MatchCandidate> candidates;
}
