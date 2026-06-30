package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.dto.RobotCheckDto;

public interface RobotCheckService {

    void verify(RobotCheckDto robotCheck, String visitorId, String clientIp);
}
