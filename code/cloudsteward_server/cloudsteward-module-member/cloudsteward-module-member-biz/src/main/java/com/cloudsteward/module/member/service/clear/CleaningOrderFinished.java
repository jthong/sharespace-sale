package com.cloudsteward.module.member.service.clear;

import com.cloudsteward.module.member.dal.dataobject.clearinfo.ClearInfoDO;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public final class CleaningOrderFinished {
    private final Long roomId;
    private final List<ClearInfoDO> tasks;
    private final Instant finishedAt;

    public CleaningOrderFinished(ClearInfoDO task, Instant finishedAt) {
        this(task.getRoomId(), Collections.singletonList(task), finishedAt);
    }

    public CleaningOrderFinished(Long roomId, List<ClearInfoDO> tasks, Instant finishedAt) {
        this.roomId = roomId;
        this.tasks = Collections.unmodifiableList(new ArrayList<>(tasks));
        this.finishedAt = finishedAt;
    }
}
