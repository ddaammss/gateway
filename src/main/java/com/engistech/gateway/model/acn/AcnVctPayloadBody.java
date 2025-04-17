package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.CallTermination;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnVctPayloadBody {
    private CallTermination callTermination;
}