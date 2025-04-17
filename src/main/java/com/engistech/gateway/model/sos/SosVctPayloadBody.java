package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.CallTermination;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosVctPayloadBody {
    private CallTermination callTermination;
}