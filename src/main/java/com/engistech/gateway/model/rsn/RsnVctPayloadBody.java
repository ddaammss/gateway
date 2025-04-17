package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.CallTermination;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnVctPayloadBody {
    private CallTermination callTermination;
}