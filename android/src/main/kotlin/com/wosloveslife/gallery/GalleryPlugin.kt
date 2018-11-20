package com.wosloveslife.gallery

import android.widget.Toast
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.bean.MediaBean
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageMultipleResultEvent
import cn.finalteam.rxgalleryfinal.ui.RxGalleryListener
import cn.finalteam.rxgalleryfinal.ui.base.IMultiImageCheckedListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONObject


class GalleryPlugin constructor(private val registrar: PluginRegistry.Registrar) : MethodCallHandler {
    companion object {
        private const val CHANNEL_NAME = "flutter_plugin_gallery"
        private const val METHOD_PICK_IMAGES = "pick_images"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            channel.setMethodCallHandler(GalleryPlugin(registrar))
        }
    }

    var toast: Toast? = null

    init {
        RxGalleryListener.getInstance().setMultiImageCheckedListener(object : IMultiImageCheckedListener {
            override fun selectedImg(t: Any?, isChecked: Boolean) {
            }

            override fun selectedImgMax(t: Any?, isChecked: Boolean, maxSize: Int) {
                toast?.cancel()
                toast = Toast.makeText(registrar.activeContext(), "你最多只能选择${maxSize}张图片", Toast.LENGTH_SHORT)
                toast?.show()
            }
        })
    }

    /**
     * call.arguments data format:
     * [
     *  [
     *    {
     *      "id":"253523",
     *      "width":"720",
     *      "height":"1920",
     *      "length":"342123",
     *      "originalPath":"storage://0/images/image_1.jpg",
     *      "thumbnailBigPath":"storage://0/image_big_thumbnails/image_1.jpg"
     *      "thumbnailSmallPath":"storage://0/image_small_thumbnails/image_1.jpg"
     *    },
     *    {
     *      "id":"253524",
     *      "width":"480",
     *      "height":"800",
     *      "length":"142123",
     *      "originalPath":"storage://0/images/image_2.jpg",
     *      "thumbnailBigPath":"storage://0/image_big_thumbnails/image_2.jpg"
     *      "thumbnailSmallPath":"storage://0/image_small_thumbnails/image_2.jpg"
     *    }
     *  ],
     *  9
     * ]
     */
    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == METHOD_PICK_IMAGES) {

            // RxGalleryFinal.with(this).hidePreview();
            val rxGalleryFinal = RxGalleryFinal
                    .with(registrar.activeContext())
                    .image()
                    .multiple()

            val arguments = call.arguments as List<*>?
            val maxLimit: Int = try {
                (arguments?.get(1) as Int?) ?: 0
            } catch (e: Throwable) {
                result.error("Illegal arguments", e.message, null)
                return
            }

            if (arguments?.get(0) is List<*>) {
                val list: List<MediaBean> = (arguments[0] as List<*>).map { arg ->
                    val obj = arg as HashMap<*, *>
                    val mediaBean = MediaBean()
                    mediaBean.id = (obj["id"] as String).toLong()
                    mediaBean.width = (obj["width"] as String).toInt()
                    mediaBean.height = (obj["height"] as String).toInt()
                    mediaBean.length = (obj["length"] as String).toLong()
                    mediaBean.originalPath = obj["originalPath"] as String
                    mediaBean.thumbnailBigPath = obj["thumbnailBigPath"] as String
                    mediaBean.thumbnailSmallPath = obj["thumbnailSmallPath"] as String
                    mediaBean
                }
                if (list.isNotEmpty()) {
                    rxGalleryFinal.selected(list)
                }
            }

            rxGalleryFinal.maxSize(maxLimit)
                    .imageLoader(ImageLoaderType.GLIDE)
                    .subscribe(object : RxBusResultDisposable<ImageMultipleResultEvent>() {

                        override fun onEvent(event: ImageMultipleResultEvent?) {
                            if (event?.result?.isNotEmpty() == true) {
                                val results: List<String> = event.result.map { m ->
                                    val jsonObject = JSONObject()
                                    jsonObject.put("id", m.id.toString())
                                    jsonObject.put("width", m.width)
                                    jsonObject.put("height", m.height)
                                    jsonObject.put("length", m.length)
                                    jsonObject.put("originalPath", m.originalPath)
                                    jsonObject.put("thumbnailBigPath", m.thumbnailBigPath)
                                    jsonObject.put("thumbnailSmallPath", m.thumbnailSmallPath)
                                    jsonObject.toString()
                                }
                                result.success(results)
                            } else {
                                result.success(null)
                            }
                        }
                    })
                    .openGallery()
        } else {
            result.notImplemented()
        }
    }
}
