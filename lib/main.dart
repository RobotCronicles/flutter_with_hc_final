import 'package:device_apps/device_apps.dart';
import 'package:external_app_launcher/external_app_launcher.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  static const appTitle = 'Health Connect with Flutter';

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      title: appTitle,
      home: MyHomePage(title: appTitle),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;
  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

//Global Drawer
class MyDrawer extends StatelessWidget {
  const MyDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    return Drawer(
      // Add a ListView to the drawer. This ensures the user can scroll
      // through the options in the drawer if there isn't enough vertical
      // space to fit everything.
      child: ListView(
        // Important: Remove any padding from the ListView.
        padding: EdgeInsets.zero,

        children: [
          const DrawerHeader(
            decoration: BoxDecoration(
              color: Colors.black12,
              image: DecorationImage(
                image: NetworkImage(
                  "https://i.imgur.com/3uNEV4T.png",
                ),

              ),
            ),
            child:
            Text(""
            ),

          ),
          ListTile(
            leading: const Icon(Icons.run_circle_sharp),
            title: const Text('Exercise sessions',
              style: TextStyle(
                fontSize: 18,
              ),
            ),
            onTap: () {
              Navigator.pop(context);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const _ExerciseSessionRoute()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.mode_night),
            title: const Text('Sleep sessions',
              style: TextStyle(
                fontSize: 18,
              ),
            ),
            onTap: () {
              Navigator.pop(context);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const _SleepSessionRoute()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.monitor_weight),
            title: const Text('Record weight',
              style: TextStyle(
                fontSize: 18,
              ),
            ),
            onTap: () {
              Navigator.pop(context);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const _WeightRoute()),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.settings),
            title: const Text('Settings',
              style: TextStyle(
                fontSize: 18,
              ),
            ),
            onTap: () {
              Navigator.pop(context);
              LaunchApp.openApp(
                  androidPackageName: 'com.google.android.apps.healthdata');
            },
          ),
        ],
      ),
    );
  }
}

class _MyHomePageState extends State<MyHomePage> with WidgetsBindingObserver{
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
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    checkHealthConnectApp(context);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if(state == AppLifecycleState.resumed){
      checkHealthConnectApp(context);
    }
  }

  Future checkHealthConnectApp(BuildContext context) async {
    // Variable to check if Google Health Connect applicatoin is installed
    bool isInstalled =
    await DeviceApps.isAppInstalled(googleHealthConnectPath);
    if (isInstalled) {
      hasPermission = await checkGoogleHealthConnectPermission();
    } else {
      await LaunchApp.openApp(
          androidPackageName: googleHealthConnectPath);
    }
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
            Image.network( // <-- SEE HERE
              'https://i.imgur.com/AXhFJzm.png',
            ),

            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: const [
                Text('Welcome to Health Connect using Flutter',
                    style: TextStyle(
                      fontSize: 18,
                    )
                ),
              ],
            ),
            const SizedBox(
              height: 100,
            ),
            ElevatedButton(onPressed: () async{
              LaunchApp.openApp(
                  androidPackageName: googleHealthConnectPath);
            },
                child: const Text(
                    'Navigate to Health Connect'
                )
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
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text('Permission Status : '),
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



          ],
        ),
      ),
      drawer:  hasPermission == true ? MyDrawer() : null,

    );
  }
}

// Start _ExerciseSessionRoute
class _ExerciseSessionRoute extends StatelessWidget {
  const _ExerciseSessionRoute({super.key});

  @override
  Widget build(BuildContext context) {
    const appTitle = 'Generate Exercise Sessions';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: const Text(appTitle),
        ),
        body: const Center(
          child:
          ExerciseStatefulClass(),
        ),
        drawer: const MyDrawer(),
      ),
    );
  }
}

// Create a stateful class for Exercise Session
class ExerciseStatefulClass extends StatefulWidget {
  const ExerciseStatefulClass({super.key});

  @override
  ExerciseStatefulClassState createState() {
    return ExerciseStatefulClassState();
  }
}

// Create a corresponding State class.
class ExerciseStatefulClassState extends State<ExerciseStatefulClass> {
  MethodChannel channel = const MethodChannel('google-health-connect');
  // Variable to launch writeStepDataMethod
  String generateExerciseSessionMethod = 'generateExerciseSessionMethod';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Align(
          alignment: Alignment.topCenter,
          child: ElevatedButton(
              onPressed: ()
              async {
                // Invoke generateExerciseSessionMethod
                await channel.invokeMethod(generateExerciseSessionMethod);
              },
              child: const Text('Generate Exercise Session')),
        ),
      ),
      drawer: MyDrawer(),
    );

  }
}
//End _ExerciseSessionRoute


// Start _SleepSessionRoute
class _SleepSessionRoute extends StatelessWidget {
  const _SleepSessionRoute({super.key});

  @override
  Widget build(BuildContext context) {
    const appTitle = 'Generate Sleep Sessions';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: const Text(appTitle),
        ),
        body: const Center(
          child:
          SleepStatefulClass(),
        ),
        drawer: const MyDrawer(),
      ),
    );
  }
}

// Create a stateful class for Exercise Session
class SleepStatefulClass extends StatefulWidget {
  const SleepStatefulClass({super.key});

  @override
  SleepStatefulClassState createState() {
    return SleepStatefulClassState();
  }
}

// Create a corresponding State class.
class SleepStatefulClassState extends State<SleepStatefulClass> {
  MethodChannel channel = const MethodChannel('google-health-connect');
  // Variable to launch writeStepDataMethod
  String generateSleepSessionMethod = 'generateSleepSessionMethod';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Align(
          alignment: Alignment.topCenter,
          child: ElevatedButton(
              onPressed: ()
              async {
                // Invoke generateExerciseSessionMethod
                await channel.invokeMethod(generateSleepSessionMethod);
              },
              child: const Text('Generate Sleep Session')),
        ),
      ),
      drawer: MyDrawer(),
    );

  }
}
//End _SleepSessionRoute

// Start _WeightRoute
class _WeightRoute extends StatelessWidget {
  const _WeightRoute({super.key});

  @override
  Widget build(BuildContext context) {
    const appTitle = 'Create Weight Record';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: const Text(appTitle),
        ),
        body: const Center(
          child:
          WeightStatefulClass(),
        ),
        drawer: const MyDrawer(),
      ),
    );
  }
}

// Create a stateful class for Exercise Session
class WeightStatefulClass extends StatefulWidget {
  const WeightStatefulClass({super.key});

  @override
  WeightStatefulClassState createState() {
    return WeightStatefulClassState();
  }
}

// Create a corresponding State class.
class WeightStatefulClassState extends State<WeightStatefulClass> {
  // Create a global key that uniquely identifies the Form widget
  // and allows validation of the form.
  //
  // Note: This is a GlobalKey<FormState>,
  // not a GlobalKey<MyCustomFormState>.
  final _formKey = GlobalKey<FormState>();

  MethodChannel channel = const MethodChannel('google-health-connect');
  // Variable to launch generateWeightRecordMethod
  String generateWeightRecordMethod = 'generateWeightRecordMethod';

  @override
  Widget build(BuildContext context) {
    // Build a Form widget using the _formKey created above.
    String? weightValue;

    return Form(
      key: _formKey,
      child: Container(
            alignment: Alignment.center,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                TextFormField(
                  onSaved: (String? value) {
                    weightValue = value;
                  },
                  inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.allow(RegExp(r'^\d+\.?\d{0,2}'))],
                  decoration: const InputDecoration(
                      labelText: 'Enter Weight Record',
                      hintText: "Kilograms"
                  ),
                  style: const TextStyle(fontSize: 20),
                  // The validator receives the text that the user has entered.
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter some text';
                    }
                    return null;
                  },
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16.0),
                  child: ElevatedButton(
                      onPressed: ()
                      async {
                        if (_formKey.currentState!.validate()) {
                          _formKey.currentState?.save();

                          var sendWeight = <String, dynamic> {
                            "weightVal" : weightValue!
                          };

                          // Invoke generateWeightRecordMethod
                          await channel.invokeMethod(generateWeightRecordMethod, sendWeight);

                        }
                      },
                      child: const Text('Insert Weight record')),
                ),
              ],
            ),
          ),
    );

  }
}
//End _WeightRoute



