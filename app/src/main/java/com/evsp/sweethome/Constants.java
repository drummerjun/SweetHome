package com.evsp.sweethome;

public class Constants {
    public static final String SERVER_API_KEY = "AIzaSyAXem-yD3NrvnKq3gGSO1r39GqRpG7y0go";
    public static final String ANDROID_API_KEY = "AIzaSyAfjb4HVnoaoPhV5Ox8DeulrialW-48wcM";
    public static final String PROJECT_ID = "upheld-world-847";
    public static final String SENDER_ID = "419289965058";

    public static final String PREF_KEY = "pref_home";
    public static final String KEY_SERVER = "home_server";
    public static final String KEY_PORT = "home_port";
    public static final String KEY_ID = "gateway_id";
    public static final String KEY_TTS = "tts_enabled";
    public static final String ALERT_UNREAD = "alert_unread";

    public static final String PROPERTY_REG_ID = "regid";
    public static final String PROPERTY_APP_VERSION = "appver";

    public static final String ACTION_LOGIN_FEEDBACK = "com.evsp.sweethome.login";
    public static final String ACTION_READ_MESSAGE = "com.evsp.sweethome.incoming";
    public static final String ACTION_SEND_MESSAGE = "com.evsp.sweethome.outgoing";
    public static final String ACTION_SEE_DETAILS = "com.evsp.sweethome.details";
    public static final String ACTION_ALERT_LOG = "com.evsp.sweethome.alerts";
    public static final String ACTION_SECURITY_SETTINGS = "com.evsp.sweethome.lock";
    public static final String ACTION_VOICE_BIND = "com.evsp.sweethome.bind";
    public static final String ACTION_USB_MESSAGE = "com.evsp.sweethome.USB_MESSAGE";
    public static final String ACTION_GOTO_SYSTEM = "com.evsp.sweethome.system";
    public static final String MESSAGE = "message_body";
    public static final String ERROR = "error_msg";
    public static final String SAVED = "saved";
    public static final String STATUS = "status";
    public static final String ON_READY = "_ready";
    public static final String ON_START = "_start";
    public static final String ON_PARTIAL = "_partial";
    public static final String ON_RESULT = "_result";
    public static final String USB_CONNECTION = "usb_connected";

    public static final String MESSAGE_CONNECTED = "_CONNECTION_ESTABLISHED_";
    public static final String MESSAGE_ERROR = "_CONNECTION_ERROR_";
    public static final String MESSAGE_IN_USE = "Port already in use";
    public static final String MESSAGE_NO_DEVICE = "No Device";

    public static final String JSON_CMD = "cmd";
    public static final String JSON_ENGLISH = "english";
    public static final String JSON_DEVICES = "devices";
    public static final String JSON_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_TYPE = "type";
    public static final String JSON_ATTRIBUTES = "attr";
    public static final String JSON_POWER = "power";
    public static final String JSON_BATT_LEVEL = "battery";
    public static final String JSON_WATT = "active_power";
    public static final String JSON_LOAD = "load";
    public static final String JSON_OVERLOAD = "overload";
    public static final String JSON_TEMP = "temperature";
    public static final String JSON_HUMIDITY = "humidity";
    public static final String JSON_FLOOD = "flood";
    public static final String JSON_FIRE = "fire";
    public static final String JSON_SMOKE_BATTERY = "batt";
    //public static final String JSON_TRIGGER = "trigger";
    public static final String JSON_MOTION = "motion";
    public static final String JSON_MAGNETIC = "magnetic";
    public static final String JSON_TAMPER = "tamper";
    public static final String JSON_SWITCH_VALUE = "switch";
    public static final String JSON_SECURITY = "security";
    public static final String JSON_RESULT = "rc";

    public static final String DEVICE_PLUG = "power";
    public static final String DEVICE_THERMO = "thermostat";
    public static final String DEVICE_MAGNETIC = "magnetic";
    public static final String DEVICE_WALL_SWITCH = "switch";
    public static final String DEVICE_FLOOD = "flood";
    public static final String DEVICE_REMOTE = "remote";
    public static final String DEVICE_KEYPAD = "keypad";
    public static final String DEVICE_MOTION = "pir";
    public static final String DEVICE_SIREN = "siren";
    public static final String DEVICE_SMOKE = "smoke";

    public static final String SECURITY_ARM = "arm";
    public static final String SECURITY_PARM1 = "parm1";
    public static final String SECURITY_PARM2 = "parm2";
    public static final String SECURITY_DISARM = "parm3";

    public static final String VALUE_DOWN_SHORT = "DownShort";
    public static final String VALUE_DOWN_LONG = "DownLong";
    public static final String VALUE_UP_SHORT = "UpShort";
    public static final String VALUE_UP_LONG = "UpLong";

    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String CONNECTED = "connected";
    public static final String REMOVED = "removed";
    public static final String ALARM = "alarm";
    public static final String TRIGGERED = "triggered";
    public static final String LOW_BATT = "low";
    public static final String ADD = "add";

    public static final String CMD_LIST = ".list";
    public static final String CMD_L = ".l";
    public static final String CMD_BIND = ".bind";
    public static final String CMD_REMOVE = ".remove";
    public static final String CMD_REPORT = ".report";
    public static final String CMD_EVENT = ".event";
    public static final String CMD_SEND = ".send";
    public static final String CMD_ARM = ".arm";
    public static final String CMD_PARM1 = ".parm1";
    public static final String CMD_PARM2 = ".parm2";
    public static final String CMD_DISARM = ".disarm";

    public static final String RF_ACK = "rf_ack";
    public static final String RF_NOACK = "rf_noack";

    public static final String VOICE_BIND = "bind";
    public static final String VOICE_REGISTER = "register";
    public static final String VOICE_WELCOME = "welcome";
    public static final String VOICE_BIND_ERR1 = "find";
    public static final String VOICE_BIND_ERR2 = "spine";
    public static final String VOICE_BIND_ERR3 = "pine";
    public static final String VOICE_BIND_ERR4 = "fine";
    public static final String VOICE_BIND_ERR5 = "vine";

    public static final String ARGS_ALERT_TIME = "alert_time";
    public static final String ARGS_ALERT_BODY = "alert_msg";

    public static final int INDEX_HOME = 0;
    public static final int INDEX_GUI = INDEX_HOME + 2;
    public static final int INDEX_ALARM = INDEX_GUI + 1;
    public static final int INDEX_CONSOLE = INDEX_ALARM + 1;
    public static final int INDEX_ALERTS = INDEX_CONSOLE + 1;
    public static final int INDEX_WEB = INDEX_ALERTS + 1;
    public static final int INDEX_EXIT = INDEX_WEB + 2;

    public static final int MAX_RECORD_NUM = 20;
}
