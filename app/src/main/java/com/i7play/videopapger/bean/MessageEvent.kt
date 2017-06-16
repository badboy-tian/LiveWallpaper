package com.i7play.videopapger.bean

/**
 * Created by Administrator on 2017/6/9.
 */
enum class EventType{
    GOHOME, UPDATEFILES
}
class MessageEvent(var type: EventType){

}