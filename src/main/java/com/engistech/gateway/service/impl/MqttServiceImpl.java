package com.engistech.gateway.service.impl;

import com.engistech.gateway.config.ConvertUilts;
import com.engistech.gateway.entity.*;
import com.engistech.gateway.model.common.*;
import com.engistech.gateway.model.dhc.DhcInterval;
import com.engistech.gateway.model.vls.VlsCallSetting;
import com.engistech.gateway.repository.*;
import com.engistech.gateway.service.MqttService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttServiceImpl implements MqttService {

    private final CommonHeaderRepository commonHeaderRepository;
    private final VehicleReportRepository vehicleReportRepository;
    private final TblVehiclesRepository tblVehiclesRepository;
    private final GpsDataRepository gpsDataRepository;
    private final CellularInfoRepository cellularInfoRepository;
    private final VehiclePositionHistoryRepository vehiclePositionHistoryRepository;
    private final ErrorInfoRepository errorInfoRepository;
    private final DhcReportEntityRepository dhcReportEntityRepository;
    private final VlsReportSettingEntityRepository vlsReportSettingEntityRepository;
    private final VlsCallSettingEntityRepository vlsCallSettingEntityRepository;
    private final ProvisioningSettingEntityRepository provisioningSettingEntityRepository;
    private final ProvisioningServiceListEntityRepository provisioningServiceListEntityRepository;
    private final ProvisioningPhoneNumberEntityRepository provisioningPhoneNumberEntityRepository;
    private final TirePressureInfoEntityRepository tirePressureInfoEntityRepository;
    private final OdometerInfoEntityRepository odometerInfoEntityRepository;
    private final DmsOdometerEntityRepository dmsOdometerEntityRepository;
    private final WindowsInfoEntityRepository windowsInfoEntityRepository;
    private final ImpactDataEntityRepository impactDataEntityRepository;
    private final SeatStatusEntityRepository seatStatusEntityRepository;
    private final VctInfoEntityRepository vctInfoEntityRepository;
    private final DcmDtcListEntityRepository dcmDtcListEntityRepository;
    private final DhcIntervalEntityRepository dhcIntervalEntityRepository;
    private final ProvisioningFinalRepository provisioningFinalRepository;
    private final ProvisioningHistoryRepository provisioningHistoryRepository;
    private final DhcIntervalFinalRepository dhcIntervalFinalRepository;
    private final DhcIntervalHistoryRepository dhcIntervalHistoryRepository;
    private final EcallEndHistoryRepository ecallEndHistoryRepository;
    private final SvcMainLogRepository svcMainLogRepository;
    private final SvcDetailLogRepository svcDetailLogRepository;
    private final SvcHistoryRepository svcHistoryRepository;
    private final UserLogRepository userLogRepository;
    private final ProvisioningPresetEntityRepository provisioningPresetEntityRepository;

    @Override
    public <T> void insertPayload(String topic, T payload) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload.toString());

            CommonHeader header = objectMapper.convertValue(jsonNode.get("header"), CommonHeader.class);
            CommonHeaderEntity result = insertCommonHeader(header);

            if(result != null) {

                if(jsonNode.get("body") != null) {

                    if(!topic.contains("error")) {
                        insertBody(header.getMessage().getService().toString(), result, objectMapper, jsonNode.get("body"));
                    } 
                    else {
                        ErrorInfo errorInfo = objectMapper.convertValue(jsonNode.get("body"), ErrorInfo.class);
                        ErrorInfoEntity errorInfoEntity = new ErrorInfoEntity();
                        errorInfoEntity.setHeaderId(result.getId());
                        errorInfoEntity.setErrorCode(errorInfo.getCode());
                        errorInfoEntity.setErrorDesc(errorInfo.getDescription());

                        errorInfoRepository.save(errorInfoEntity);
                    }
                }
            }
        } 
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * INSERT common header
     *
     * @param header the header
     * @return the common header
     */
    public CommonHeaderEntity insertCommonHeader(CommonHeader header) {

        UserProperties userProperties = header.getUserProperties();
        DeviceInfo device = header.getDevice();
        MessageInfo message = header.getMessage();
        CommonHeaderEntity commonHeaderEntity = new CommonHeaderEntity();

        // userProperties
        commonHeaderEntity.setCorrelationId(userProperties.getCorrelationId());
        commonHeaderEntity.setSchemaVersion(userProperties.getSchemaVersion());
        commonHeaderEntity.setSessionId(userProperties.getSessionId());
        commonHeaderEntity.setUserAgent(userProperties.getUserAgent());

        // device
        commonHeaderEntity.setDcmSupplier(device.getDcmSupplier());
        commonHeaderEntity.setDcmFwVersion(device.getDcmfwVersion());
        commonHeaderEntity.setNadFwVersion(device.getNadfwVersion());
        commonHeaderEntity.setMcuSwVersion(device.getMcuswVersion());
        commonHeaderEntity.setMsisdn(device.getMsisdn());
        commonHeaderEntity.setImsi(device.getImsi());
        commonHeaderEntity.setImei(device.getImei());
        commonHeaderEntity.setVin(device.getVin());
        commonHeaderEntity.setIccid(device.getIccid());
        commonHeaderEntity.setEuiccid(device.getEuiccid());

        // message
        commonHeaderEntity.setMessageType(CommonConstant.MESSAGE_TYPE.convertValue(message.getType().toString()));
        commonHeaderEntity.setMessageService(CommonConstant.MESSAGE_SERVICE.convertValue(message.getService().toString()));
        commonHeaderEntity.setMessageOperation(CommonConstant.MESSAGE_OPERATION.convertValue(message.getOperation().toString()));

        String huLanguage = String.valueOf(header.getHuLanguage());
        commonHeaderEntity.setHuLanguage("null".equals(huLanguage) ? null : huLanguage);

        commonHeaderEntity.setTransmissionTimestampUtc(header.getTransmissionTimestampUTC());

        return commonHeaderRepository.save(commonHeaderEntity);
    }

    /**
     * Insert body.
     *
     * @param messageService     the message service
     * @param commonHeaderEntity the common header entity
     * @param objectMapper       the object mapper
     * @param body               the body
     */
    public void insertBody(String messageService, CommonHeaderEntity commonHeaderEntity, ObjectMapper objectMapper, JsonNode body) {

        if(body.get("vehicleReport") != null) {
            JsonNode vehicleReport = body.get("vehicleReport");

            VehicleReportEntity vehicleReportResult = insertVehicleReport(messageService, commonHeaderEntity, vehicleReport);

            if(vehicleReport.get("gpsData") != null) {
                insertGpsData(commonHeaderEntity.getVin(), commonHeaderEntity.getMessageService(), VehicleReportConstant.REPORT_DIV.VEHICLE_REPORT.getValue(), vehicleReportResult.getId(), vehicleReport.get("gpsData"), 0);
            }

            if(vehicleReport.get("lastValidGpsData") != null) {
                insertGpsData(commonHeaderEntity.getVin(), commonHeaderEntity.getMessageService(), VehicleReportConstant.REPORT_DIV.VEHICLE_REPORT.getValue(), vehicleReportResult.getId(), vehicleReport.get("lastValidGpsData"), 1);
            }

            if(vehicleReport.get("cellularInfo") != null) {
                insertCellularInfo(VehicleReportConstant.REPORT_DIV.VEHICLE_REPORT.getValue(), vehicleReportResult.getId(), vehicleReport.get("cellularInfo"));
            }

            if(vehicleReport.get("history") != null) {
                insertVehiclePositionHistory(vehicleReportResult.getId(), vehicleReport.get("history"));
            }
        } 
        else if(body.get("dhc") != null) {
            insertDhc(commonHeaderEntity, body.get("dhc"));
        } 
        else if(body.get("reportSetting") != null) { // Vls Start

            if(CommonConstant.MESSAGE_TYPE.REQUEST.toString().equals(commonHeaderEntity.getMessageType())) {
                insertVlsStart(commonHeaderEntity, body.get("reportSetting"));
            } 
            else {
                DhcInterval interval = objectMapper.convertValue(body.get("reportSetting").get("interval"), DhcInterval.class);
                DhcIntervalEntity intervalEntity = new DhcIntervalEntity();
                intervalEntity.setHeaderId(commonHeaderEntity.getId());
                intervalEntity.setIntervalValue(interval.getValue());

                dhcIntervalEntityRepository.save(intervalEntity);
            }
        } 
        else if(body.get("callSetting") != null) { // Voice Call
            VlsCallSetting vlsCallSetting = objectMapper.convertValue(body.get("callSetting"), VlsCallSetting.class);
            VlsCallSettingEntity vlsCallSettingEntity = new VlsCallSettingEntity();
            vlsCallSettingEntity.setHeaderId(commonHeaderEntity.getId());
            vlsCallSettingEntity.setHmi(VlsConstant.ON_OFF.convertValue(vlsCallSetting.getHmi().toString()));

            vlsCallSettingEntityRepository.save(vlsCallSettingEntity);
        } 
        else if(body.get("provisioning") != null) { // Provisioning
//            Provisioning provisioning = objectMapper.convertValue(body.get("provisioning"), Provisioning.class);
            insertProvisioning(commonHeaderEntity, body.get("provisioning"));
        } 
        else if(body.get("callTermination") != null) { // vct
            CallTermination callTermination = objectMapper.convertValue(body.get("callTermination"), CallTermination.class);
            VctInfoEntity vctInfoEntity = new VctInfoEntity();
            vctInfoEntity.setHeaderId(commonHeaderEntity.getId());
            vctInfoEntity.setTerminationDiv(VlsConstant.TERMINATION_DIV.convertValue(callTermination.toString()));

            vctInfoEntityRepository.save(vctInfoEntity);
        } 
        else if(body.get("interval") != null) { // dhc_interval
            DhcInterval interval = objectMapper.convertValue(body.get("interval"), DhcInterval.class);
            DhcIntervalEntity intervalEntity = new DhcIntervalEntity();
            intervalEntity.setHeaderId(commonHeaderEntity.getId());
            intervalEntity.setIntervalValue(interval.getValue());

            dhcIntervalEntityRepository.save(intervalEntity);
        }
    }

    /**
     * Insert vls start.
     *
     * @param commonHeaderEntity    the common header entity
     * @param vlsStartReportSetting the vls start report setting
     */
    public void insertVlsStart(CommonHeaderEntity commonHeaderEntity, JsonNode vlsStartReportSetting) {
        VlsReportSettingEntity vlsReportSettingEntity = new VlsReportSettingEntity();
        vlsReportSettingEntity.setHeaderId(commonHeaderEntity.getId());
        if(vlsStartReportSetting.get("priority") != null) vlsReportSettingEntity.setPriorityDiv(VlsConstant.PRIORITY.convertValue(vlsStartReportSetting.get("priority").asText()));
        if(vlsStartReportSetting.get("activateTimeLimit") != null) vlsReportSettingEntity.setActivateTimeLimit(VlsConstant.ON_OFF.convertValue(vlsStartReportSetting.get("activateTimeLimit").asText()));
        if(vlsStartReportSetting.get("timeLimit").get("unit") != null) vlsReportSettingEntity.setTimeLimitUnit(VlsConstant.TIME_LIMIT_UNIT.convertValue(vlsStartReportSetting.get("timeLimit").get("unit").asText()));
        if(vlsStartReportSetting.get("timeLimit").get("value") != null) vlsReportSettingEntity.setTimeLimitValue(vlsStartReportSetting.get("timeLimit").get("value").asInt());
        if(vlsStartReportSetting.get("ignitionONReport") != null) vlsReportSettingEntity.setIgnitionOnReport(VlsConstant.ON_OFF.convertValue(vlsStartReportSetting.get("ignitionONReport").asText()));
        if(vlsStartReportSetting.get("ignitionOFFReport") != null) vlsReportSettingEntity.setIgnitionOffReport(VlsConstant.ON_OFF.convertValue(vlsStartReportSetting.get("ignitionOFFReport").asText()));
        if(vlsStartReportSetting.get("activateTimeInterval") != null) vlsReportSettingEntity.setActivateTimeInterval(VlsConstant.ON_OFF.convertValue(vlsStartReportSetting.get("activateTimeInterval").asText()));
        if(vlsStartReportSetting.get("interval").get("unit") != null) vlsReportSettingEntity.setIntervalUnit(VlsConstant.INTERVAL.convertValue(vlsStartReportSetting.get("interval").get("unit").asText()));
        if(vlsStartReportSetting.get("interval").get("value") != null) vlsReportSettingEntity.setIntervalValue(vlsStartReportSetting.get("interval").get("value").asInt());
        if(vlsStartReportSetting.get("historyReport") != null) vlsReportSettingEntity.setHistoryReport(VlsConstant.YES_NO.convertValue(vlsStartReportSetting.get("historyReport").asText()));

        vlsReportSettingEntityRepository.save(vlsReportSettingEntity);
    }

    /**
     * Insert dhc.
     *
     * @param commonHeaderEntity the common header entity
     * @param dhcInfo            the dhc info
     */
    public void insertDhc(CommonHeaderEntity commonHeaderEntity, JsonNode dhcInfo) {

        DhcReportEntity dhcReportEntity = new DhcReportEntity();
        dhcReportEntity.setHeaderId(commonHeaderEntity.getId()); // header id
        dhcReportEntity.setIgnitionState(VehicleReportConstant.IGNITION_STATE.convertValue(dhcInfo.get("ignition").asText()));
        dhcReportEntity.setFuelLevel(dhcInfo.get("fuelLevel").asText());
        dhcReportEntity.setBatteryVoltate(dhcInfo.get("batteryVoltage").asInt());
        dhcReportEntity.setEventTimestampUtc(dhcInfo.get("eventTimestampUTC").asText());

        DhcReportEntity dhcReportEntityResult = dhcReportEntityRepository.save(dhcReportEntity);

        String dcmVin = commonHeaderEntity.getVin();

        if(dhcInfo.get("gpsData") != null) {
            insertGpsData(dcmVin, commonHeaderEntity.getMessageService(), VehicleReportConstant.REPORT_DIV.DHC_REPORT.getValue(), dhcReportEntityResult.getId(), dhcInfo.get("gpsData"), 0);
        }

        if(dhcInfo.get("lastValidGpsData") != null) {
            insertGpsData(dcmVin, commonHeaderEntity.getMessageService(), VehicleReportConstant.REPORT_DIV.DHC_REPORT.getValue(), dhcReportEntityResult.getId(), dhcInfo.get("lastValidGpsData"), 1);
        }

        if(dhcInfo.get("cellularInfo").get("MNC") != null) {
            insertCellularInfo(VehicleReportConstant.REPORT_DIV.DHC_REPORT.getValue(), dhcReportEntityResult.getId(), dhcInfo.get("cellularInfo"));
        }

        // tirePressure INSERT
        TirePressureInfoEntity tirePressureInfoEntity = new TirePressureInfoEntity();
        tirePressureInfoEntity.setDhcReportId(dhcReportEntityResult.getId());
        tirePressureInfoEntity.setTirePressureUnit(dhcInfo.get("tirePressure").get("unit").asText());
        tirePressureInfoEntity.setTire1Value(dhcInfo.get("tirePressure").get("tire1").get("value").asText());
        tirePressureInfoEntity.setTire2Value(dhcInfo.get("tirePressure").get("tire2").get("value").asText());
        tirePressureInfoEntity.setTire3Value(dhcInfo.get("tirePressure").get("tire3").get("value").asText());
        tirePressureInfoEntity.setTire4Value(dhcInfo.get("tirePressure").get("tire4").get("value").asText());
        tirePressureInfoEntity.setTire5Value(dhcInfo.get("tirePressure").get("tire5").get("value").asText());

        tirePressureInfoEntityRepository.save(tirePressureInfoEntity);

        // MQTT Table에 odometer INSERT
        // -------------------------------------------------------------------------------------------- //
        OdometerInfoEntity odometerInfoEntity = new OdometerInfoEntity();

        odometerInfoEntity.setDhcReportId(dhcReportEntityResult.getId());

        String odometerUnit = dhcInfo.path("odometer").path("unit").asText();
        String binaryValue = dhcInfo.path("odometer").path("value").asText();
        
        if((odometerUnit.equals("01") || odometerUnit.equals("10")) && !binaryValue.equalsIgnoreCase("UNKNOWN")) {

            long odometerValue = Long.parseLong(binaryValue, 2);

            // miles -> km로 변환
            if (odometerUnit.equals("10")) {
                odometerValue *= 1.60934;
            }

            odometerInfoEntity.setOdometerUnit("01");
            odometerInfoEntity.setOdometerValue(String.valueOf(odometerValue));
        }
        else {
            odometerInfoEntity.setOdometerUnit(odometerUnit);
            odometerInfoEntity.setOdometerValue(binaryValue);
        }

        odometerInfoEntityRepository.save(odometerInfoEntity);
        // -------------------------------------------------------------------------------------------- //

        // DMS Table에 odometer INSERT or UPDATE
        // -------------------------------------------------------------------------------------------- //
        // DMS에서 처리한 레코드 삭제
        dmsOdometerEntityRepository.deleteByDmsProcessed("Y");

        if((odometerUnit.equals("01") || odometerUnit.equals("10")) && !binaryValue.equalsIgnoreCase("UNKNOWN")) {

            // 동일 VIN에 대한 레코드 유무 확인
            Optional<DmsOdometerEntity> existingOdometerEntity = dmsOdometerEntityRepository.findById(dcmVin);
            DmsOdometerEntity dmsOdometerEntity = existingOdometerEntity.orElseGet(DmsOdometerEntity::new);

            long odometerValue = Long.parseLong(binaryValue, 2);

            // miles -> km로 변환
            if (odometerUnit.equals("10")) {
                odometerValue *= 1.60934;
            }

            if(existingOdometerEntity.isEmpty()) {
                dmsOdometerEntity.setVin(dcmVin);
            }
    
            dmsOdometerEntity.setOdometers(odometerValue);

            LocalDateTime curTime = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            dmsOdometerEntity.setIfDatetime(curTime.format(timeFormatter));

            dmsOdometerEntity.setDmsProcessed("N");

            dmsOdometerEntityRepository.save(dmsOdometerEntity);
        }
        // -------------------------------------------------------------------------------------------- //

        // windows INSERT
        WindowsInfoEntity windowsInfoEntity = new WindowsInfoEntity();
        windowsInfoEntity.setDhcReportId(dhcReportEntityResult.getId());
        windowsInfoEntity.setDriverSeat(dhcInfo.get("windows").get("driverSeat").asText());
        windowsInfoEntity.setPassengerSeat(dhcInfo.get("windows").get("passengerSeat").asText());
        windowsInfoEntity.setLeftRearSeat(dhcInfo.get("windows").get("leftRearSeat").asText());
        windowsInfoEntity.setRightRearSear(dhcInfo.get("windows").get("rightRearSeat").asText());
        windowsInfoEntity.setSlideRoof(dhcInfo.get("windows").get("slideRoof").asText());

        windowsInfoEntityRepository.save(windowsInfoEntity);

        JsonNode itemsNode = dhcInfo.path("dcmDTCList");

        Iterator<JsonNode> elements = itemsNode.elements();
        while (elements.hasNext()) {
            JsonNode dcmDtc = elements.next();

            DcmDtcListEntity dcmDtcListEntity = new DcmDtcListEntity();
            dcmDtcListEntity.setDhcReportId(dhcReportEntityResult.getId());
            dcmDtcListEntity.setDtcCode(dcmDtc.get("description").asText());
            dcmDtcListEntity.setFailureType(dcmDtc.get("failureType").asText());
            dcmDtcListEntity.setTestFailedStatus(dcmDtc.get("testFailed").asBoolean() ? 1 : 0);
            dcmDtcListEntity.setConfirmedDtcStatus(dcmDtc.get("confirmedDTC").asBoolean() ? 1 : 0);
            dcmDtcListEntity.setTestFailedSinceLastClearStatus(dcmDtc.get("testFailedSinceLastClear").asBoolean() ? 1 : 0);
            dcmDtcListEntity.setSsrTimeInformationUtc(dcmDtc.get("ssrTimeInformationUTC").asText());
            dcmDtcListEntity.setSsrVoltage(String.valueOf(dcmDtc.get("ssrVoltage")));

            dcmDtcListEntityRepository.save(dcmDtcListEntity);
        }

        insertProvisioning(commonHeaderEntity, dhcInfo.get("provisioning"));
    }

    /**
     * Insert provisioning.
     *
     * @param commonHeaderEntity the common header entity
     * @param provisioning       the provisioning
     */
    public void insertProvisioning(CommonHeaderEntity commonHeaderEntity, JsonNode provisioning) {
        ProvisioningSettingEntity provisioningSettingEntity = new ProvisioningSettingEntity();
        provisioningSettingEntity.setHeaderId(commonHeaderEntity.getId());
        provisioningSettingEntity.setBrand(provisioning.get("brand").asText());
        provisioningSettingEntity.setProvisioningLanguage(provisioning.get("provisioningLanguage").asText());
        provisioningSettingEntity.setCallbackStandbyTimer(provisioning.get("configuration").get("callbackStandByTimer").asInt());
        provisioningSettingEntity.setSosCancelTimer(provisioning.get("configuration").get("sosCancelTimer").asInt());

        ProvisioningSettingEntity provisioningSettingResult = provisioningSettingEntityRepository.save(provisioningSettingEntity);

        if(!provisioning.get("serviceFlags").isEmpty()) {
            JsonNode itemsNode = provisioning.path("serviceFlags");
            Iterator<JsonNode> elements = itemsNode.elements();
            while (elements.hasNext()) {
                JsonNode serviceFlag = elements.next();
                ProvisioningServiceListEntity provisioningServiceListEntity = new ProvisioningServiceListEntity();
                provisioningServiceListEntity.setProvisioningId(provisioningSettingResult.getId());
                provisioningServiceListEntity.setServiceType(ProvisioningConstant.SERVICE_TYPE.convertValue(serviceFlag.get("service").asText()));
                provisioningServiceListEntity.setServiceFlag(VlsConstant.ON_OFF.convertValue(serviceFlag.get("flagValue").asText()));

                provisioningServiceListEntityRepository.save(provisioningServiceListEntity);
            }
        }

        ProvisioningPhoneNumberEntity provisioningPhoneNumberEntity = new ProvisioningPhoneNumberEntity();
        if(!provisioning.get("configuration").path("phoneNumbers").isEmpty()) {
            JsonNode itemsNode = provisioning.get("configuration").path("phoneNumbers");
            Iterator<JsonNode> elements = itemsNode.elements();
            while (elements.hasNext()) {
                JsonNode phoneNumber = elements.next();
                ProvisioningPhoneNumberEntity phoneNumberEntity = new ProvisioningPhoneNumberEntity();
                phoneNumberEntity.setProvisioningId(provisioningSettingResult.getId());
                phoneNumberEntity.setServiceType(ProvisioningConstant.PHONE_NUMBER_SERVICE_TYPE.convertValue(phoneNumber.get("service").asText()));
                phoneNumberEntity.setPhoneNumberType(ProvisioningConstant.PHONE_NUMBER_TYPE.convertValue(phoneNumber.get("type").asText()));
                phoneNumberEntity.setPhoneNumber(phoneNumber.get("value").asText());

                provisioningPhoneNumberEntityRepository.save(phoneNumberEntity);
            }
        }
    }

    /**
     * Insert gps data.
     *
     * @param reportDiv Report 종류 구분 (0: vehicle_report, 1: dhc_report)
     * @param reportId  Report 식별 ID
     * @param gpsData   gps Data
     */
    public void insertGpsData(String vin, Integer messageService, int reportDiv, Integer reportId, JsonNode gpsData, int isLastValidDataType) {

        GpsDataEntity gpsDataEntity = new GpsDataEntity();
        JsonNode coordinate = gpsData.get("coordinate");
        JsonNode velocity = gpsData.get("velocity");

        gpsDataEntity.setReportId(reportId);

        if(!gpsData.path("heading").isMissingNode()) {
            gpsDataEntity.setHeadingDirection(Integer.parseInt(gpsData.path("heading").asText()));
        }

        gpsDataEntity.setGpsSystem(gpsData.path("system").asText());

        // GPS Data 변환
        // -------------------------------------------------------------------------------- //
        String gpsUnit = coordinate.path("unit").asText();
        String gpsLat = coordinate.path("lat").asText();
        String gpsLon = coordinate.path("lon").asText();

        if (gpsUnit.equalsIgnoreCase("MAS") && !gpsLat.equalsIgnoreCase("UNKNOWN") && !gpsLon.equalsIgnoreCase("UNKNOWN")) {
            gpsDataEntity.setCoordinateUnit("DEG");
            gpsDataEntity.setCoordinateLat(ConvertUilts.convertMasToLatitude(gpsLat));
            gpsDataEntity.setCoordinateLon(ConvertUilts.convertMasToLongitude(gpsLon));
        }
        else {
            gpsDataEntity.setCoordinateUnit(gpsUnit);
            gpsDataEntity.setCoordinateLat(gpsLat);
            gpsDataEntity.setCoordinateLon(gpsLon);
        }
        // -------------------------------------------------------------------------------- //

        // Velocity Data 변환
        // -------------------------------------------------------------------------------- //
        String velocityUnit = velocity.path("unit").asText();
        int velocityValue = velocity.path("value").asInt();

        if(velocityUnit.equalsIgnoreCase("MPH")) {
            gpsDataEntity.setVelocityUnit("KPH");
            velocityValue = (int) Math.round(velocityValue * 1.60934);
        }
        else {
            gpsDataEntity.setVelocityUnit(velocityUnit);
        }

        gpsDataEntity.setVelocityValue(velocityValue);
        // -------------------------------------------------------------------------------- //

        gpsDataEntity.setGnssAccuracy(Integer.parseInt(gpsData.path("accurate").asText()));
        gpsDataEntity.setDateTimeUtc(gpsData.path("dateTimeUTC").asText());
        gpsDataEntity.setReportDiv(reportDiv);
        gpsDataEntity.setIsLastValidData(isLastValidDataType);

        gpsDataRepository.save(gpsDataEntity);

        // GPS Data Service에 대해 발생 이력 저장
        if(isLastValidDataType == 0) {
            insertSvcHistory(vin, messageService);
            insertUserLog(vin, messageService);
        }
    }

    /**
     * Insert cellular info.
     *
     * @param reportDiv    Report 종류 구분 (0: vehicle_report, 1: dhc_report)
     * @param reportId     Report 식별 ID
     * @param cellularInfo cellular Info
     */
    public void insertCellularInfo(int reportDiv, Integer reportId, JsonNode cellularInfo) {
        CellularInfoEntity cellularInfoEntity = new CellularInfoEntity();

        cellularInfoEntity.setReportDiv(reportDiv);
        cellularInfoEntity.setReportId(reportId);
        cellularInfoEntity.setMnc(cellularInfo.get("MNC").asText());
        cellularInfoEntity.setRat(cellularInfo.get("RAT").asText());
        cellularInfoEntity.setTac(cellularInfo.get("TAC").asText());
        cellularInfoEntity.setCellId(cellularInfo.get("CellID").asText());
        cellularInfoEntity.setMcc(cellularInfo.get("MCC").asText());

        cellularInfoRepository.save(cellularInfoEntity);
    }

    /**
     * Insert vehicle report vehicle report entity.
     *
     * @param commonHeaderEntity 적재된 common header 정보
     * @param vehicleReport      vehicle Report
     * @return the vehicle report entity
     */
    public VehicleReportEntity insertVehicleReport(String messageService, CommonHeaderEntity commonHeaderEntity, JsonNode vehicleReport) {
        VehicleReportEntity vehicleReportEntity = new VehicleReportEntity();
        ObjectMapper objectMapper = new ObjectMapper();
        if(vehicleReport.get("bubInUse") != null) vehicleReportEntity.setBubInUse(VehicleReportConstant.BUB_IN_USE.convertValue(vehicleReport.get("bubInUse").asText()));
        if(vehicleReport.get("deltaVRangeLimit") != null) vehicleReportEntity.setDeltaVRangeLimit(Integer.parseInt(vehicleReport.get("deltaVRangeLimit").asText()));
        if(vehicleReport.get("eventTimestampUTC") != null) {
            vehicleReportEntity.setEventTimestampUtc(vehicleReport.get("eventTimestampUTC").asText());
        }
        if(vehicleReport.get("eventTrigger") != null) vehicleReportEntity.setEventTrigger(VehicleReportConstant.EVENT_TRIGGER.convertValue(messageService, vehicleReport.get("eventTrigger").asText()));
        if(vehicleReport.get("frontAirbag") != null) vehicleReportEntity.setFrontAirbag(VehicleReportConstant.STATE.convertValue(vehicleReport.get("frontAirbag").asText()));
        if(vehicleReport.get("fuelType") != null) vehicleReportEntity.setFuelType(vehicleReport.get("fuelType").asText());
        vehicleReportEntity.setHeaderId(commonHeaderEntity.getId());
        if(vehicleReport.get("ignition") != null) vehicleReportEntity.setIgnitionState(VehicleReportConstant.IGNITION_STATE.convertValue(vehicleReport.get("ignition").asText()));
        if(vehicleReport.get("multipleImpact") != null) vehicleReportEntity.setMultipleImpact(VehicleReportConstant.STATE.convertValue(vehicleReport.get("multipleImpact").asText()));
        if(vehicleReport.get("numberOfOccupants") != null) vehicleReportEntity.setNumberOfOccupants(vehicleReport.get("numberOfOccupants").asInt());
        if(vehicleReport.get("rearImpact") != null) vehicleReportEntity.setRearImpact(VehicleReportConstant.STATE.convertValue(vehicleReport.get("rearImpact").asText()));
        if(vehicleReport.get("rollover") != null) vehicleReportEntity.setRolloverState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("rollover").asText()));
        if(vehicleReport.get("sideAirbag") != null) vehicleReportEntity.setSideAirbag(VehicleReportConstant.STATE.convertValue(vehicleReport.get("sideAirbag").asText()));
        if(vehicleReport.get("sideImpactSensor") != null) vehicleReportEntity.setSideImpactSensor(VehicleReportConstant.SIDE_IMPACT_SENSOR.convertValue(vehicleReport.get("sideImpactSensor").asText()));

        VehicleReportEntity vehicleReportEntityResult = vehicleReportRepository.save(vehicleReportEntity);

        vehicleReport.get("firstImpact");

        // firstImpact INSERT
        if(vehicleReport.get("firstImpact") != null) {
            JsonNode firstImpactNode = vehicleReport.get("firstImpact");

            ImpactDataEntity impactDataEntity = new ImpactDataEntity();
            impactDataEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            impactDataEntity.setImpactType(VehicleReportConstant.IMPACT_TYPE.FIRST_IMPACT.getValue());
            impactDataEntity.setMaxDeltaVx(firstImpactNode.get("maxDeltaVX").asText());
            impactDataEntity.setMaxDeltaVy(firstImpactNode.get("maxDeltaVY").asText());

            impactDataEntityRepository.save(impactDataEntity);
        }

        // secondImpact INSERT
        if(vehicleReport.get("secondImpact") != null) {
            ImpactDataEntity impactDataEntity = new ImpactDataEntity();
            impactDataEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            impactDataEntity.setImpactType(VehicleReportConstant.IMPACT_TYPE.SECOND_IMPACT.getValue());
            impactDataEntity.setMaxDeltaVx(vehicleReport.get("secondImpact").get("maxDeltaVX").asText());
            impactDataEntity.setMaxDeltaVy(vehicleReport.get("secondImpact").get("maxDeltaVY").asText());

            impactDataEntityRepository.save(impactDataEntity);
        }

        // seat_status INSERT
        if(vehicleReport.get("driverSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.DRIVER.getValue());
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("driverSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("passengerSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.PASSENGER.getValue());
            seatStatusEntity.setOccupantState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("passengerSeat").get("occupant").asText()));
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("passengerSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("centerRearSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.CENTER_REAR.getValue());
            seatStatusEntity.setOccupantState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("centerRearSeat").get("occupant").asText()));
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("centerRearSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("leftRearSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.LEFT_REAR.getValue());
            seatStatusEntity.setOccupantState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("leftRearSeat").get("occupant").asText()));
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("leftRearSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("rightRearSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.RIGHT_REAR.getValue());
            seatStatusEntity.setOccupantState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("rightRearSeat").get("occupant").asText()));
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("rightRearSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("centerThirdRowSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.CENTER_THIRD_ROW.getValue());
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("centerThirdRowSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }

        if(vehicleReport.get("leftThirdRowSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.LEFT_THIRD_ROW.getValue());
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("leftThirdRowSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }
        if(vehicleReport.get("rightThirdRowSeat") != null) {
            SeatStatusEntity seatStatusEntity = new SeatStatusEntity();
            seatStatusEntity.setVehicleReportId(vehicleReportEntityResult.getId());
            seatStatusEntity.setSeatPositionType(VehicleReportConstant.SEAT_POSITION_TYPE.RIGHT_THIRD_ROW.getValue());
            seatStatusEntity.setBuckleState(VehicleReportConstant.STATE.convertValue(vehicleReport.get("rightThirdRowSeat").get("buckle").asText()));
            seatStatusEntityRepository.save(seatStatusEntity);
        }
        return vehicleReportEntityResult;
    }

    /**
     * Insert vehicle position history.
     *
     * @param reportId               Report 식별 ID
     * @param vehiclePositionHistory vehicle position history
     */
    public void insertVehiclePositionHistory(Integer reportId, JsonNode vehiclePositionHistory) {
        ObjectMapper objectMapper = new ObjectMapper();

        VehiclePositionHistoryEntity vehiclePositionHistoryEntity = new VehiclePositionHistoryEntity();
        vehiclePositionHistoryEntity.setVehicleReportId(reportId);
        JsonNode itemsNode = vehiclePositionHistory.path("historyEntry");

        Iterator<JsonNode> elements = itemsNode.elements();
        while (elements.hasNext()) {
            JsonNode item = elements.next();

            int value = item.path("value").asInt();

            vehiclePositionHistoryEntity.setEntryNo(Integer.parseInt(item.path("no").asText()));
            vehiclePositionHistoryEntity.setCoordinateUnit(item.path("coordinate").path("unit").asText());
            vehiclePositionHistoryEntity.setCoordinateLat(ConvertUilts.convertMasToLatitude(item.path("coordinate").path("lat").asText()));
            vehiclePositionHistoryEntity.setCoordinateLon(ConvertUilts.convertMasToLongitude(item.path("coordinate").path("lon").asText()));
            vehiclePositionHistoryEntity.setDateTimeUtc(item.path("dateTimeUTC").asText());

            vehiclePositionHistoryRepository.save(vehiclePositionHistoryEntity);
        }
    }

    // Provisioning 설정 정보 조회
    public ProvisioningFinalEntity getProvisioningFinalByVin(String vin) {
        return provisioningFinalRepository.findByVin(vin).orElse(null);
    }

    // Provisioning 설정 default 정보 조회
    public ProvisioningFinalEntity getProvisioningFinalByFlag(int flag) {
        return provisioningFinalRepository.findByDefaultFlag(flag).orElse(null);
    }

    // Provisioning Final 데이터 레코드 생성
    public ProvisioningFinalEntity insertProvisioningFinal(String vin) {

        ProvisioningFinalEntity entity = new ProvisioningFinalEntity();
        entity.setVin(vin);

        return provisioningFinalRepository.save(entity);
    }

    // Provisioning History 데이터 레코드 생성
    public ProvisioningHistoryEntity insertProvisioningHistory(String vin) {

        ProvisioningHistoryEntity entity = new ProvisioningHistoryEntity();
        entity.setVin(vin);

        return provisioningHistoryRepository.save(entity);
    }

    // DHC Interval 설정 정보 조회
    public DhcIntervalFinalEntity getDhcIntervalFinalByVin(String vin) {
        return dhcIntervalFinalRepository.findByVin(vin).orElse(null);
    }

    // DHC Interval Final 데이터 레코드 생성
    public DhcIntervalFinalEntity insertDhcIntervalFinal(String vin) {

        DhcIntervalFinalEntity entity = new DhcIntervalFinalEntity();
        entity.setVin(vin);

        return dhcIntervalFinalRepository.save(entity);
    }

    // DHC Interval History 데이터 레코드 생성
    public DhcIntervalHistoryEntity insertDhcIntervalHistory(String vin) {

        DhcIntervalHistoryEntity entity = new DhcIntervalHistoryEntity();
        entity.setVin(vin);

        return dhcIntervalHistoryRepository.save(entity);
    }

    // VCT Flag 확인
    public EcallEndHistoryEntity getEcallEnd(String sessionId, String callType) {
        List<EcallEndHistoryEntity> results = ecallEndHistoryRepository.findBySessionIdAndCallType(sessionId, callType);
        return results.isEmpty() ? null : results.get(0);
    }

    // 차량연동서비스 마스터 로그 저장
    public SvcMainLogEntity insertSvcMainLog(SvcMainLogEntity entity) {
        return svcMainLogRepository.save(entity);
    }

    // 차량연동서비스 마스터 로그 식별 ID 조회
    public Long getTeleSvcMainIdBySessionId(String sessionId) {
        List<Long> results = svcMainLogRepository.findTeleSvcMainIdBySessionId(sessionId);
        return results.isEmpty() ? null : results.get(0);
    }

    // 차량연동서비스 마스터 로그 상태 업데이트
    public void updateStatusAndTime(String statusCode, LocalDateTime statusTime, String sessionId) {
        svcMainLogRepository.updateStatusAndTimeBySessionId(statusCode, statusTime, sessionId);
    }

    // 차량연동서비스 상세 로그 저장
    public SvcDetailLogEntity insertSvcDetailLog(SvcDetailLogEntity entity) {
        return svcDetailLogRepository.save(entity);
    }

    // GPS Data 수신 이력 저장 (DCM -> MQTT)
    public SvcHistoryEntity insertSvcHistory(String vin, Integer messageService) {

        Optional<Integer> vehicleIdOpt = tblVehiclesRepository.findVehicleIdByVin(vin);

        if(!vehicleIdOpt.isPresent()) {
            log.error("해당 VIN({})에 대한 차량 정보가 없습니다.", vin);
            return null;
        }

        Integer vehicleId = vehicleIdOpt.get();

        SvcHistoryEntity svcHistoryEntity = new SvcHistoryEntity();
        svcHistoryEntity.setTelemetryServiceType(messageService.shortValue());
        svcHistoryEntity.setVehicleId(vehicleId);

        return svcHistoryRepository.save(svcHistoryEntity);
    }

    // GPS Data 송신 이력 저장 (MQTT -> CTI)
    public UserLogEntity insertUserLog(String vin, Integer messageService) {

        Optional<Integer> vehicleIdOpt = tblVehiclesRepository.findVehicleIdByVin(vin);

        if(!vehicleIdOpt.isPresent()) {
            log.error("해당 VIN({})에 대한 차량 정보가 없습니다.", vin);
            return null;
        }

        Integer vehicleId = vehicleIdOpt.get();

        UserLogEntity userLogEntity = new UserLogEntity();
        userLogEntity.setPktType(messageService.shortValue());
        userLogEntity.setVehicleId(vehicleId);
        userLogEntity.setReceivedType((short)1);

        return userLogRepository.save(userLogEntity);
    }

    public void insertDefaultPreset(String mode, String vin) {
        ProvisioningPresetEntity provisioningPresetEntity = provisioningPresetEntityRepository.findByDefaultFlag(1);

        if("P".equals(mode)) { // Provisioning
            ProvisioningFinalEntity finalEntity = new ProvisioningFinalEntity();
            BeanUtils.copyProperties(provisioningPresetEntity, finalEntity);
            finalEntity.setDefaultFlag(0);
            finalEntity.setVin(vin);

            provisioningFinalRepository.save(finalEntity);

        } else if("D".equals(mode)) { // DHC
            DhcIntervalFinalEntity finalEntity = new DhcIntervalFinalEntity();
            finalEntity.setIntervalValue(provisioningPresetEntity.getDhcIntervalValue());
            finalEntity.setVin(vin);

            dhcIntervalFinalRepository.save(finalEntity);
        }
    }
}
