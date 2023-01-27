import 'package:device_apps/device_apps.dart';
import 'package:external_app_launcher/external_app_launcher.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter with Google Health Connect'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  MethodChannel channel = const MethodChannel('google-health-connect');

  String hasGoogleHealthConnectPermission = 'hasGoogleHealthConnectPermission';

  // Variable to check if Google Health Connect has allowed permission for this application
  bool? hasPermission;

  // Variable to launch google health connect application
  String googleHealthConnectPath = 'com.google.android.apps.healthdata';

  // Function to check if Google Health Connect has allowed permission for this application by using method channel
  Future<bool> checkGoogleHealthConnectPermission() async {
    hasPermission =
        await channel.invokeMethod(hasGoogleHealthConnectPermission);
    setState(() {
      hasPermission;
    });
    return hasPermission!;
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
                onPressed: () async {
                  // Variable to check if Google Health Connect applicatoin is installed
                  bool isInstalled =
                      await DeviceApps.isAppInstalled(googleHealthConnectPath);
                  if (isInstalled) {
                    hasPermission = await checkGoogleHealthConnectPermission();
                  } else {
                    await LaunchApp.openApp(
                        androidPackageName: googleHealthConnectPath);
                  }
                },
                child: const Text('permission check button')),
            const SizedBox(
              height: 10,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text('permission status : '),
                Text(
                  (hasPermission == null
                      ? 'Unknown'
                      : hasPermission.toString()),
                  style: TextStyle(
                      color: hasPermission == true
                          ? Colors.blue
                          : hasPermission == false
                              ? Colors.red
                              : Colors.grey,
                      fontSize: 15),
                ),
              ],
            ),
            const SizedBox(
              height: 10,
            ),
            const Divider(
              thickness: 5,
            ),
            const SizedBox(
              height: 10,
            ),
            ElevatedButton(
                onPressed: () async {
                  // We need to deep link to navigate google health connect's permission page I guess.
                  // The code below is the next best solution with third part library.
                  LaunchApp.openApp(
                      androidPackageName: googleHealthConnectPath);
                },
                child: const Text('Navigate to HC permission access page')),
            const Icon(IconData(0xe0a0, fontFamily: 'MaterialIcons')),
            const Text(
              "We Want to navigate google health connect's permission access page(Not initial page)",
              textAlign: TextAlign.center,
            ),

            const SizedBox(
              height: 10,
            ),
            const Divider(
              thickness: 5,
            ),
            const SizedBox(
              height: 10,
            ),
            ElevatedButton(
              child: const Text('Insert Steps Record'),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const _SecondRoute()),
                );
              },
            ),
            const Icon(IconData(0xe0a0, fontFamily: 'MaterialIcons')),
            const Text(
              "Click the button to navigate to Steps record page.",
              textAlign: TextAlign.center,
            ),


          ],
        ),
      ),
    );
  }
}

class _SecondRoute extends StatelessWidget {
  const _SecondRoute({super.key});

  @override
  Widget build(BuildContext context) {
    const appTitle = 'Insert Steps Record';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: const Text(appTitle),
        ),
        body: const Center(
          child:
            MyCustomForm(),

        ),
      ),
    );
  }
}

// Create a Form widget.
class MyCustomForm extends StatefulWidget {
  const MyCustomForm({super.key});

  @override
  MyCustomFormState createState() {
    return MyCustomFormState();
  }
}

// Create a corresponding State class.
// This class holds data related to the form.
class MyCustomFormState extends State<MyCustomForm> {
  // Create a global key that uniquely identifies the Form widget
  // and allows validation of the form.
  //
  // Note: This is a GlobalKey<FormState>,
  // not a GlobalKey<MyCustomFormState>.
  final _formKey = GlobalKey<FormState>();

  MethodChannel channel = const MethodChannel('google-health-connect');

  // Variable to launch writeStepDataMethod
  String writeStepDataMethod = 'writeStepDataMethod';

  @override
  Widget build(BuildContext context) {

    // Build a Form widget using the _formKey created above.
    String? stepValue;

    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextFormField(
            onSaved: (String? value) {
              stepValue = value;
              },
            // The validator receives the text that the user has entered.
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter some text';
              }
              return null;
            },
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 16.0),
            child: ElevatedButton(
                onPressed: ()
                async {
                  if (_formKey.currentState!.validate()) {
                    _formKey.currentState?.save();

                    //Check input if its getting recorded in the Form
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text(stepValue!)),
                    );

                    var sendStep = <String, dynamic> {
                      "stepVal" : stepValue!
                    };


                    // Invoke WriteStepDataMethod
                    await channel.invokeMethod(writeStepDataMethod, sendStep);

                  }
                },
                child: const Text('Add Steps record')),
          ),
        ],
      ),
    );
  }
}


