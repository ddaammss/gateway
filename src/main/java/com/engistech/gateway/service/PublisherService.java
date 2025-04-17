package com.engistech.gateway.service;

import javax.annotation.processing.Generated;

import com.engistech.gateway.model.acn.AcnResponsePayload;
import com.engistech.gateway.model.acn.AcnVctResponsePayload;
import com.engistech.gateway.model.common.RetransmitPayload;
import com.engistech.gateway.model.dhc.DhcCommandPayload;
import com.engistech.gateway.model.dhc.DhcResponsePayload;
import com.engistech.gateway.model.provisioning.CustProvisioningPayload;
import com.engistech.gateway.model.provisioning.ProvisioningPayload;
import com.engistech.gateway.model.rsn.RsnResponsePayload;
import com.engistech.gateway.model.rsn.RsnVctResponsePayload;
import com.engistech.gateway.model.sos.SosResponsePayload;
import com.engistech.gateway.model.sos.SosVctResponsePayload;
import com.engistech.gateway.model.vls.VlsResponsePayload;
import com.engistech.gateway.model.vls.VlsStartPayload;
import com.engistech.gateway.model.vls.VlsStopPayload;
import com.engistech.gateway.model.vls.VlsVctResponsePayload;
import com.engistech.gateway.model.vls.VlsVoiceCallPayload;
import com.engistech.gateway.model.vls.VoicekillPayload;

@Generated(value="com.cmm.asyncapi.generator.template.spring", date="2024-11-01T09:28:18.049Z")
public interface PublisherService {

    String receiveAcnResponse(AcnResponsePayload acnResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveAcnVctResponse(AcnVctResponsePayload acnVctResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveAcnRetransmitCommandMessage(RetransmitPayload acnRetransmitCommandPayload, String deviceId, String ecuId, String appId);
        
    String receiveSosResponse(SosResponsePayload sosResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveSosVctResponse(SosVctResponsePayload sosVctResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveSosRetransmitCommandMessage(RetransmitPayload sosRetransmitCommandPayload, String deviceId, String ecuId, String appId);
        
    String receiveRsnResponse(RsnResponsePayload rsnResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveRsnVctResponse(RsnVctResponsePayload rsnVctResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveRsnRetransmitCommandMessage(RetransmitPayload rsnRetransmitCommandPayload, String deviceId, String ecuId, String appId);
            
    String receiveVlsStartCommandMessage(VlsStartPayload vlsStartCommandPayload, String deviceId, String ecuId, String appId);
        
    String receiveVlsStopCommandMessage(VlsStopPayload vlsStopCommandPayload, String deviceId, String ecuId, String appId);
    
    String receiveVlsVoiceCallCommandMessage(VlsVoiceCallPayload vlsVoiceCallCommandPayload, String deviceId, String ecuId, String appId);

    String receiveVlsVehicleReportResponse(VlsResponsePayload vlsResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveTrackingVctResponse(VlsVctResponsePayload vlsVctResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveProvisioningCommandMessage(ProvisioningPayload provisioningCommandPayload, String deviceId, String ecuId, String appId);
        
    String receiveCustProvisioningConfiguration(CustProvisioningPayload custProvisioningConfigurationPayload, String deviceId, String ecuId, String appId);
        
    String receiveDhcResponse(DhcResponsePayload dhcResponsePayload, String deviceId, String ecuId, String appId);
        
    String receiveDhcCommandMessage(DhcCommandPayload dhcCommandPayload, String deviceId, String ecuId, String appId);
        
    String receiveVoiceKillCommandMessage(VoicekillPayload voicekillCommandPayload, String deviceId, String ecuId, String appId);
}
