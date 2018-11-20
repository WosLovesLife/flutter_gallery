import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

class Gallery {
  static const MethodChannel _channel = const MethodChannel('flutter_plugin_gallery');

  static Future<List<GalleryImage>> pickImages(List<GalleryImage> pickedImages, int maxLimit) async {
    var jsonList = pickedImages?.map((image){
      return {
        'id': image.id,
        'width': image.width.toString(),
        'height': image.height.toString(),
        'length': image.length.toString(),
        'originalPath': image.originalPath,
        'thumbnailBigPath': image.thumbnailBigPath,
        'thumbnailSmallPath': image.thumbnailSmallPath,
      };
    })?.toList();
    List<dynamic> results = await _channel.invokeMethod('pick_images', [jsonList, maxLimit]);
    return results.map((jsonStr) {
      Map<String, dynamic> map = json.decode(jsonStr);
      return GalleryImage(
        id: map['id'],
        width: map['width'],
        height: map['height'],
        length: map['length'],
        originalPath: map['originalPath'],
        thumbnailBigPath: map['thumbnailBigPath'],
        thumbnailSmallPath: map['thumbnailSmallPath'],
      );
    }).toList();
  }
}

class GalleryImage {
  final String id;
  final int width;
  final int height;
  final int length;
  final String originalPath;
  final String thumbnailBigPath;
  final String thumbnailSmallPath;

  GalleryImage(
      {this.id,
      this.width,
      this.height,
      this.length,
      this.originalPath,
      this.thumbnailBigPath,
      this.thumbnailSmallPath});
}
