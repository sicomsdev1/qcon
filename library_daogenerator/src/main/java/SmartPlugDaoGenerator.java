import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.ToOne;

/**
 * Created by wizardkyn on 2015. 3. 10..
 */
public class SmartPlugDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(29, "smartplugdb");
        addSettingVo(schema);
        addPhoneVo(schema);
        addAll(schema);
        new DaoGenerator().generateAll(schema, "../app/src/main/java");
    }

    private static void addAll(Schema schema) {
        Entity groupVo = schema.addEntity("DbGroupVo");
        groupVo.setTableName("tb_group");
        Property placeIdGroupVo = groupVo.addStringProperty("placeId").getProperty();
        Property groupIdGroupVo = groupVo.addLongProperty("groupId").primaryKey().getProperty();
        groupVo.addStringProperty("groupName").notNull();
        groupVo.addStringProperty("superId").notNull();
        groupVo.addStringProperty("groupImg");

        Entity groupSettingVo = schema.addEntity("DbGroupSettingVo");
        groupSettingVo.setTableName("tb_group_setting");
        Property groupIdGroupSettingVo = groupSettingVo.addLongProperty("groupId").getProperty();
        Property setIdGroupSettingVo = groupSettingVo.addStringProperty("setId").primaryKey().getProperty();
        groupSettingVo.addStringProperty("setVal").notNull();
        groupSettingVo.addToOne(groupVo, groupIdGroupSettingVo);

        ToMany groupToSetting = groupVo.addToMany(groupSettingVo, groupIdGroupSettingVo);
        groupToSetting.setName("groupSettingList");

        Index idxGroupSettingVo = new Index();
        idxGroupSettingVo.addProperty(groupIdGroupSettingVo);
        idxGroupSettingVo.addProperty(setIdGroupSettingVo);
        idxGroupSettingVo.makeUnique();
        groupSettingVo.addIndex(idxGroupSettingVo);

        Entity placeVo = schema.addEntity("DbPlaceVo");
        placeVo.setTableName("tb_place");
        Property placeIdPlaceVo = placeVo.addStringProperty("placeId").primaryKey().getProperty();
        placeVo.addStringProperty("placeName").notNull();
        placeVo.addStringProperty("placeImg");
        placeVo.addStringProperty("address");
        placeVo.addStringProperty("coordinate");
        placeVo.addStringProperty("auth");
        placeVo.addIntProperty("plugCount");
        placeVo.addIntProperty("memberCount");

        groupVo.addToOne(placeVo, placeIdGroupVo);

        ToMany placeToGroup = placeVo.addToMany(groupVo, placeIdGroupVo);
        placeToGroup.setName("groupList");
        placeToGroup.orderDesc(groupIdGroupVo);

        Index idxGroupVo = new Index();
        idxGroupVo.addProperty(placeIdGroupVo);
        idxGroupVo.addProperty(groupIdGroupVo);
        idxGroupVo.makeUnique();
        groupVo.addIndex(idxGroupVo);

        Entity placeSettingVo = schema.addEntity("DbPlaceSettingVo");
        placeSettingVo.setTableName("tb_place_setting");
        placeSettingVo.addIdProperty().autoincrement().primaryKey();
        Property placeIdPlaceSettingVo = placeSettingVo.addStringProperty("placeId").notNull().getProperty();
        Property setIdPlaceSettingVo = placeSettingVo.addStringProperty("setId").notNull().getProperty();
        placeSettingVo.addStringProperty("setVal").notNull();
        placeSettingVo.addToOne(placeVo, placeIdPlaceSettingVo);

        ToMany placeToSetting = placeVo.addToMany(placeSettingVo, placeIdPlaceSettingVo);
        placeToSetting.setName("placeSettingList");

        Index idxPlaceSettingVo = new Index();
        idxPlaceSettingVo.addProperty(placeIdPlaceSettingVo);
        idxPlaceSettingVo.addProperty(setIdPlaceSettingVo);
        idxPlaceSettingVo.makeUnique();
        placeSettingVo.addIndex(idxPlaceSettingVo);

        Entity userVo = schema.addEntity("DbUserVo");
        userVo.setTableName("tb_user");
        Property placeIdUserVo = userVo.addStringProperty("placeId").getProperty();
        Property userIdPlugVo = userVo.addStringProperty("userId").primaryKey().getProperty();
        userVo.addStringProperty("userName").notNull();
        userVo.addStringProperty("profileImg");
        userVo.addIntProperty("auth").notNull();
        userVo.addToOne(placeVo, placeIdUserVo);

        ToMany placeToUser = placeVo.addToMany(userVo, placeIdUserVo);
        placeToUser.setName("userList");

        Index idxUserVo = new Index();
        idxUserVo.addProperty(placeIdUserVo);
        idxUserVo.addProperty(userIdPlugVo);
        idxUserVo.makeUnique();
        userVo.addIndex(idxUserVo);

        Entity plugVo = schema.addEntity("DbPlugVo");
        plugVo.setTableName("tb_plug");
        Property placeIdPlugVo = plugVo.addStringProperty("placeId").getProperty();
        Property plugIdPlugVo = plugVo.addStringProperty("plugId").primaryKey().getProperty();
        plugVo.addStringProperty("plugName").notNull();
        plugVo.addStringProperty("plugType");
        plugVo.addStringProperty("plugImg");
        Property bssIdPlugVo = plugVo.addStringProperty("bssId").getProperty();
        Property routerIpPlugVo = plugVo.addStringProperty("routerIp").getProperty();
        Property gatewayIpPlugVo = plugVo.addStringProperty("gatewayIp").getProperty();
        Property uuidPlugVo = plugVo.addStringProperty("uuid").getProperty();
        plugVo.addToOne(placeVo, placeIdPlugVo);

        Index idxPlugVo = new Index();
        idxPlugVo.addProperty(placeIdPlugVo);
        idxPlugVo.addProperty(plugIdPlugVo);
        idxPlugVo.makeUnique();
        plugVo.addIndex(idxPlugVo);

        ToMany placeToPlug = placeVo.addToMany(plugVo, placeIdPlugVo);
        placeToPlug.setName("plugList");

        Entity lastDataVo = schema.addEntity("DbLastDataVo");
        lastDataVo.setTableName("tb_last_data");
        Property plugIdLastDataVo = lastDataVo.addStringProperty("plugId").primaryKey().getProperty();
        lastDataVo.addDateProperty("recTime");
        lastDataVo.addFloatProperty("wh");
        lastDataVo.addFloatProperty("w");
        lastDataVo.addStringProperty("onOff");
        lastDataVo.addStringProperty("ledOnOff");
        lastDataVo.addToOne(plugVo, plugIdLastDataVo);

        plugVo.addToOne(lastDataVo, plugIdPlugVo);

        Entity cutOffVo = schema.addEntity("DbCutOffVo");
        cutOffVo.setTableName("tb_cutoff");
        Property plugIdCutOffVo = cutOffVo.addStringProperty("plugId").notNull().getProperty();
        Property cutSeqCutOffVo = cutOffVo.addLongProperty("cutSeq").primaryKey().autoincrement().getProperty();
        cutOffVo.addStringProperty("setWatt").notNull();
        cutOffVo.addStringProperty("setMin").notNull();
        cutOffVo.addStringProperty("useYn").notNull();
        cutOffVo.addToOne(plugVo, plugIdCutOffVo);

        ToMany plugToCutoff = plugVo.addToMany(cutOffVo, plugIdCutOffVo);
        plugToCutoff.setName("cutOffList");

        Index idxCutOffVo = new Index();
        idxCutOffVo.addProperty(plugIdCutOffVo);
        idxCutOffVo.addProperty(cutSeqCutOffVo);
        idxCutOffVo.makeUnique();
        cutOffVo.addIndex(idxCutOffVo);


        Entity scheduleVo = schema.addEntity("DbScheduleVo");
        scheduleVo.setTableName("tb_schedule");
        Property plugIdScheduleVo = scheduleVo.addStringProperty("plugId").notNull().getProperty();
        Property schSeqScheduleVo = scheduleVo.addLongProperty("schSeq").primaryKey().autoincrement().getProperty();
        scheduleVo.addStringProperty("startTime").notNull();
        scheduleVo.addStringProperty("endTime").notNull();
        scheduleVo.addStringProperty("startUseYn").notNull();
        scheduleVo.addStringProperty("endUseYn").notNull();

        scheduleVo.addToOne(plugVo, plugIdScheduleVo);

        ToMany plugToSchedule = plugVo.addToMany(scheduleVo, plugIdScheduleVo);
        plugToSchedule.setName("scheduleList");

        Index idxSchedule = new Index();
        idxSchedule.addProperty(plugIdScheduleVo);
        idxSchedule.addProperty(schSeqScheduleVo);
        idxSchedule.makeUnique();
        scheduleVo.addIndex(idxSchedule);

        Entity apVo = schema.addEntity("DbApVo");
        apVo.setTableName("tb_ap");
        Property bssIdApVo = apVo.addStringProperty("bssId").primaryKey().getProperty();
        apVo.addStringProperty("ssId").notNull();
        apVo.addStringProperty("password").notNull();
        apVo.addToOne(plugVo, bssIdApVo);

        Entity routerVo = schema.addEntity("DbRouterVo");
        routerVo.setTableName("tb_router");
        Property routerIpRouterVo = routerVo.addStringProperty("routerIp").primaryKey().getProperty();
        routerVo.addStringProperty("ssId").notNull();
        routerVo.addStringProperty("password").notNull();
        routerVo.addToOne(plugVo, routerIpRouterVo);

        Entity gatewayVo = schema.addEntity("DbGatewayVo");
        gatewayVo.setTableName("tb_gateway");
        Property gatewayIpGatewayVo = gatewayVo.addStringProperty("gatewayIp").primaryKey().getProperty();
        gatewayVo.addStringProperty("ssId").notNull();
        gatewayVo.addToOne(plugVo, gatewayIpGatewayVo);

        Entity bluetoothVo = schema.addEntity("DbBluetoothVo");
        bluetoothVo.setTableName("tb_bluetooth");
        Property uuidBluetoothVo = bluetoothVo.addStringProperty("uuid").primaryKey().getProperty();
        bluetoothVo.addStringProperty("password").notNull();
        bluetoothVo.addToOne(plugVo, uuidBluetoothVo);

        Entity groupPlugMappingVo = schema.addEntity("DbGroupPlugMappingVo");
        groupPlugMappingVo.setTableName("tb_group_plug_mapping");
        groupPlugMappingVo.addIdProperty().autoincrement().primaryKey();
        Property groupIdGroupPlugMappingVo = groupPlugMappingVo.addLongProperty("groupId").notNull().getProperty();
        Property plugIdGroupPlugMappingVo = groupPlugMappingVo.addStringProperty("plugId").notNull().getProperty();
        groupPlugMappingVo.addFloatProperty("wh").notNull();
        groupPlugMappingVo.addToOne(groupVo, groupIdGroupPlugMappingVo);
        groupPlugMappingVo.addToOne(plugVo, plugIdGroupPlugMappingVo);

        Index idxGroupPlugMapping = new Index();
        idxGroupPlugMapping.addProperty(groupIdGroupPlugMappingVo);
        idxGroupPlugMapping.addProperty(plugIdGroupPlugMappingVo);
        idxGroupPlugMapping.makeUnique();
        groupPlugMappingVo.addIndex(idxGroupPlugMapping);

        Entity groupUserMappingVo = schema.addEntity("DbGroupUserMappingVo");
        groupUserMappingVo.setTableName("tb_group_user_mapping");
        groupUserMappingVo.addIdProperty().autoincrement().primaryKey();
        Property groupIdGroupUserMappingVo = groupUserMappingVo.addLongProperty("groupId").notNull().getProperty();
        Property userIdGroupUserMappingVo = groupUserMappingVo.addStringProperty("userId").notNull().getProperty();
        groupUserMappingVo.addStringProperty("auth").notNull();
        groupUserMappingVo.addToOne(groupVo, groupIdGroupUserMappingVo);
        groupUserMappingVo.addToOne(userVo, userIdGroupUserMappingVo);

        Index idxGroupUserMapping = new Index();
        idxGroupUserMapping.addProperty(groupIdGroupUserMappingVo);
        idxGroupUserMapping.addProperty(userIdGroupUserMappingVo);
        idxGroupUserMapping.makeUnique();
        groupUserMappingVo.addIndex(idxGroupUserMapping);

    }

    private static void addPlugGroupVo(Schema schema) {
        Entity entity = schema.addEntity("DbPlugGroupVo");
        entity.setTableName("tb_group");
        entity.addIntProperty("groupSeq").primaryKey();
        entity.addStringProperty("groupName").notNull();
    }
    private static void addPlugVo(Schema schema) {
        Entity entity = schema.addEntity("DbPlugVo");
        entity.setTableName("tb_plug");
        entity.addStringProperty("plugId").primaryKey();
        entity.addStringProperty("plugName").notNull();
        entity.addStringProperty("plugType");
        entity.addStringProperty("protocol");
        entity.addStringProperty("serialNo");
        entity.addStringProperty("ipAddress");
    }
    private static void addCutOffVo(Schema schema) {
        Entity entity = schema.addEntity("DbCutOffVo");
        entity.setTableName("tb_cutoff");
        entity.addStringProperty("plugId").primaryKey();
        entity.addStringProperty("setWatt").notNull();
        entity.addStringProperty("setMin").notNull();
        entity.addStringProperty("useYn").notNull();
    }

    private static void addPhoneVo(Schema schema) {
        Entity entity = schema.addEntity("DbPhoneVo");
        entity.setTableName("tb_phone");
        entity.addStringProperty("userId").primaryKey();
        entity.addStringProperty("userName").notNull();
        entity.addStringProperty("profileImg");
    }

    private static void addSettingVo(Schema schema) {
        Entity entity = schema.addEntity("DbSettingVo");
        entity.setTableName("tb_setting");
        entity.addStringProperty("setId").primaryKey();
        entity.addStringProperty("setVal").notNull();
    }

    private static void addScheduleVo(Schema schema) {
        Entity entity = schema.addEntity("DbScheduleVo");
        entity.setTableName("tb_schedule");

        Property schSeq = entity.addLongProperty("schSeq").primaryKey().autoincrement().getProperty();
        Property plugId = entity.addStringProperty("plugId").notNull().getProperty();

        entity.addStringProperty("startTime").notNull();
        entity.addStringProperty("endTime").notNull();
        entity.addStringProperty("monYn").notNull();
        entity.addStringProperty("tueYn").notNull();
        entity.addStringProperty("wedYn").notNull();
        entity.addStringProperty("thuYn").notNull();
        entity.addStringProperty("friYn").notNull();
        entity.addStringProperty("satYn").notNull();
        entity.addStringProperty("sunYn").notNull();
        entity.addStringProperty("useYn").notNull();

        Index dtIdx = new Index();
        dtIdx.addProperty(schSeq);
        dtIdx.addProperty(plugId);
        dtIdx.makeUnique();
        entity.addIndex(dtIdx);
    }

    private static void addLastDataVo(Schema schema) {
        Entity lastDataVo = schema.addEntity("DbLastDataVo");
        lastDataVo.setTableName("tb_last_data");
        Property recTime = lastDataVo.addDateProperty("recTime").notNull().getProperty();
        Property plugIdLastDataVo = lastDataVo.addStringProperty("plugId").notNull().getProperty();
        lastDataVo.addFloatProperty("usage").notNull();
        lastDataVo.addStringProperty("onOff").notNull();
        lastDataVo.addStringProperty("ledOnOff").notNull();

        Index dtIdx = new Index();
        dtIdx.addProperty(recTime);
        dtIdx.addProperty(plugIdLastDataVo);
        dtIdx.makeUnique();
        lastDataVo.addIndex(dtIdx);
    }

    private static void addPlugToGroupVo(Schema schema) {
        Entity entity = schema.addEntity("DbPlugToGroupVo");
        entity.setTableName("tb_plug_group");
        Property groupSeq = entity.addIntProperty("groupSeq").notNull().getProperty();
        Property plugId = entity.addStringProperty("plugId").notNull().getProperty();

        Index dtIdx = new Index();
        dtIdx.addProperty(groupSeq);
        dtIdx.addProperty(plugId);
        dtIdx.makeUnique();
        entity.addIndex(dtIdx);
    }
}
