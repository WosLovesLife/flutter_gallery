import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:gallery/gallery.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<GalleryImage> _pickedImages;

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> _pickImages() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    List<GalleryImage> pickedImages;
    try {
      pickedImages = await Gallery.pickImages(_pickedImages, 9);
    } on PlatformException {
      // Failed to pick images
      pickedImages = null;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _pickedImages = pickedImages;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
          actions: <Widget>[
            FlatButton(
              onPressed: _pickImages,
              child: Text('Pick Images'),
            )
          ],
        ),
        body: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 3),
          itemBuilder: (BuildContext context, int index) {
            var image = _pickedImages[index];
            return Card(
              margin: EdgeInsets.only(
                top: 2,
                bottom: 2,
                left: index % 3 == 0 ? 4 : 2,
                right: index % 3 == 2 ? 4 : 2,
              ),
              child: Image(
                height: double.infinity,
                width: double.infinity,
                fit: BoxFit.cover,
                image: FileImage(File(image.originalPath)),
              ),
            );
          },
          itemCount: _pickedImages?.length ?? 0,
        ),
      ),
    );
  }
}
