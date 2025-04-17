package com.engistech.gateway.model.common;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Configuration {

    private List<PhoneNumber> phoneNumbers;

    @Min(0)
    @Max(180)
    private int callbackStandByTimer;

    @Min(0)
    @Max(10)
    private int sosCancelTimer;

    public Configuration() {
        this.phoneNumbers = new ArrayList<>();
    }
}