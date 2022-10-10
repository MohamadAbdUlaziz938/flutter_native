import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late StreamSubscription _streamSubscription;

  void _incrementCounter() async {
    int level = await batteryChannel.invokeMethod('getBatteryLevel');
    batteryLevel = '$level';
    if (mounted) {
      setState(() {});
    }
  }

  // method channel
  // use to send message to native receive response
  // send message using invoke method , receive response
  // we can use it vise versa using  setMethodCallHandler listen to invoke coming method from native
  static const batteryChannel = MethodChannel('battery');

  // event channel
  // one direction , stream message
  static const chargingChannel = EventChannel('charging');
  String batteryLevel = 'waiting ...';
  String chargingLevel = 'streaming ...';

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    onListenBattery();
    onBatteryStream();
  }

  void onBatteryStream() {
    _streamSubscription =
        chargingChannel.receiveBroadcastStream().listen((event) {
      chargingLevel = event;
      if (mounted) setState(() {});
    });
  }

  onListenBattery() {
    batteryChannel.setMethodCallHandler((call) async {
      if (call.method == "reportBatteryLevel") {
        batteryLevel = '${call.arguments}';
        if (mounted) setState(() {});
      }
    });
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
    _streamSubscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(batteryLevel),
            SizedBox(
              height: 30,
            ),
            Text(chargingLevel)
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
