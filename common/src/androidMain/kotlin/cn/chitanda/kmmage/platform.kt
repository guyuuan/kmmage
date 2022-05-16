package cn.chitanda.kmmage

/**
 * @author: Chen
 * @createTime: 2022/5/13 09:57
 * @description:
 **/
internal actual fun buildComponentRegistry(componentRegistry: ComponentRegistry): ComponentRegistry {
    return componentRegistry.newBuilder().build()
}