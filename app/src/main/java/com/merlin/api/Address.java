package com.merlin.api;

public interface Address {
    //        mUrl=null!=url&&url.length()>=0?url:"http://192.168.0.3:2008";
//    mUrl=null!=url&&url.length()>=0?url:"http://172.16.20.215:2008";
//    String URL="http://172.16.20.215:2008";
    String LOVE_ADDRESS="http://172.16.20.212:2008";
//    String LOVE_ADDRESS="http://192.168.0.6:2008";
//    String HOST="http://192.168.0.6";
    String HOST="http://172.16.20.212";
    int PORT=2008;
    String URL=HOST+":"+PORT;
//    String URL="http://106.12.163.77:2020";
//    String URL="http://172.16.20.210:2008";//GS
//    String URL="http://53971a7b.cpolar.io";
//    String URL="http://792bcd1.cpolar.io";
    String PREFIX_USER="/user";
    String PREFIX_MEDIA="/media";
    String PREFIX_FILE="/file";
    String PREFIX_IMAGE="/image";
    String PREFIX_LOVE="/love";
    String PREFIX_USER_REBOOT=PREFIX_USER+"/reboot";
    String PREFIX_MEDIA_PLAY=PREFIX_MEDIA+"/play";
    String PREFIX_THUMB=PREFIX_IMAGE+"/thumbs";
    String  LABEL_CLOUD_URL_PREFIX="cloudUrlPrefix";
}
