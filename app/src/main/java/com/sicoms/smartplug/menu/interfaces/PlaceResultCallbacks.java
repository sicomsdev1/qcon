package com.sicoms.smartplug.menu.interfaces;

import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.ScheduleVo;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public interface PlaceResultCallbacks {
    void onAddPlaceResult(PlaceVo placeVo);
    void onModPlaceResult(PlaceVo placeVo);
    void onOutPlaceResult(PlaceVo placeVo);
    void onSelecteComplete(PlaceVo placeVo);
}